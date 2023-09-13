package cuiliang.quicker.network.websocket

import androidx.lifecycle.MutableLiveData
import cuiliang.quicker.client.ClientConfig
import cuiliang.quicker.client.ConnectionStatus
import cuiliang.quicker.ui.taskManager.TaskConfig
import cuiliang.quicker.util.GsonUtils
import cuiliang.quicker.util.KLog
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.ScheduledThreadPoolExecutor
import kotlin.random.Random

/**
 * Created by Voidcom on 2023/9/11 16:13
 * 连接流程：建立连接 -> 身份验证(没有设置验证码这一步跳过) -> 连接成功
 * Description: websocket处理类，负责发起连接、处理消息等
 */
class WebSocketClient private constructor() : WebSocketListener() {
    private val okhttp: OkHttpClient by lazy { OkHttpClient() }
    private val executor = ScheduledThreadPoolExecutor(2)
    private val listeners = hashMapOf<Int, WebSocketNetListener>()

    //一个PC的动作ID，通过这个动作获取可运行动作列表。它是固定的
    private val actionListID = "85b45597-1cb3-4f76-94ca-07c19356884c"
    private var webSocketObj: WebSocket? = null

    val connResult: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //消息编号
    private var serialNum = -1
        set(value) {
            field = if (value > 65535) 0 else value
        }

    @Volatile
    private var connState: ConnectionStatus = ConnectionStatus.Disconnected
    private var isDebug = true

    //连接结果回调，一个函数引用
    private lateinit var resultCallback: ((Boolean, String) -> Unit)
    private lateinit var requestAuthJob: Job

    init {
        //随机生成一个请求编号，用于后面消息辨别
        serialNum = Random.nextInt(65535)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        outputDebugLog("建立连接 ---")
        connState = ConnectionStatus.Connected
        //建立连接后如果没有设置验证码，服务会直接下发身份验证成功消息。如果有验证码，需要自己主动发起身份验证。
        requestAuthJob = GlobalScope.launch {
            delay(200)
            //身份验证消息不能用newCall(),因为返回的replyTo字段固定是0 。。。。。
            listeners[-1] = requestAuth
            sendMsg(requestAuth.onRequest(MsgRequestData(0)).toString())
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        outputDebugLog("WebSocket-onMessage:$text")
        val data = GsonUtils.toBean(text, MsgResponseData::class.java)
        //满足存在replyTo字段和messageType=4的消息判定为客户端请求消息的响应回复
        when {
            data.messageType == MessageType.RESPONSE_AUTH.getValue() -> {
                listeners[-1]?.onResponse(data)
                listeners.remove(-1)
            }

            data.replyTo != -1 && data.messageType == MessageType.RESPONSE_COMMAND.getValue() -> {
                listeners[data.replyTo]?.onResponse(data)
                listeners.remove(data.replyTo)
            }

            data.messageType == MessageType.REQUEST_COMMAND.getValue() -> {
                //如果messageType=2属于服务端主动向客户端发送消息 todo
                outputDebugLog(text)
            }
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        connState = ConnectionStatus.CONNECT_FAIL
        resultCallback(false, response.toString())
        outputDebugLog("连接失败 --- reason:${t.printStackTrace()}")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        connState = ConnectionStatus.CLOSE
        webSocketObj = null
        outputDebugLog("关闭连接 --- reason:$reason")
    }

    @Synchronized
    fun connectRequest(callback: (result: Boolean, msg: String) -> Unit) {
        if (webSocketObj != null) return
        if (connState == ConnectionStatus.LoggedIn ||
            connState == ConnectionStatus.Connected ||
            connState == ConnectionStatus.Connecting
        ) return
        this.resultCallback = callback
        val url = ClientConfig.getInstance().applyAddress()
        if (url.isEmpty()) return
        connState = ConnectionStatus.Connecting
        val request = Request.Builder().get().url(url).build()
        outputDebugLog("connectRequest- 请求建立WebSocket连接")
        executor.execute {
            webSocketObj = okhttp.newWebSocket(request, this)
        }
    }

    fun closeRequest() {
        outputDebugLog("断开连接")
        webSocketObj?.close(1000, "bye!")
        webSocketObj = null
    }


    fun release() {
        //执行这段代码后，除非创建新的okhttp，否则无法发起新的请求
        okhttp.dispatcher.executorService.shutdown()
    }

    fun newCall(listener: WebSocketNetListener) {
        if (listeners.containsValue(listener)) {
            KLog.w("WebSocket", "接口不能重复添加")
            return
        }
        listeners[++serialNum] = listener
        sendMsg(listener.onRequest(MsgRequestData(serialNum)).toString())
    }

    private fun sendMsg(data: String) {
        if (connState != ConnectionStatus.Connected && connState != ConnectionStatus.LoggedIn) return
        outputDebugLog("发送消息：$data")
        webSocketObj?.let {
            executor.execute {
                it.send(data)
            }
        }
    }

    private fun outputDebugLog(msg: String) {
        if (!isDebug) return
        KLog.d("WebSocket", msg)
//        println(msg)
    }

    //请求身份验证
    private val requestAuth = object : WebSocketNetListener() {
        override fun onRequest(data: MsgRequestData): MsgRequestData {
            return data.setData(
                type = MessageType.REQUEST_AUTH.getValue(),
                data = ClientConfig.getInstance().webSocketCode
            )
        }

        override fun onResponse(data: MsgResponseData) {
            connState = ConnectionStatus.LoggedIn
            //身份验证成功，移除请求身份验证的消息
            if (requestAuthJob.isActive) {
                requestAuthJob.cancel()
            }
            outputDebugLog("身份验证成功")
            newCall(requestActionList)
        }
    }

    //请求动作列表
    private val requestActionList = object : WebSocketNetListener() {
        override fun onRequest(data: MsgRequestData): MsgRequestData {
            return data.setData(
                MessageType.REQUEST_COMMAND.getValue(),
                operation = "action",
                data = "-1",
                action = actionListID,
                wait = true
            )
        }

        override fun onResponse(data: MsgResponseData) {
            TaskConfig.decodeActionMsg(data.data)
            resultCallback(true, "")
            connResult.postValue(connState == ConnectionStatus.LoggedIn || connState == ConnectionStatus.Connected)
            outputDebugLog(data.toString())
        }
    }

    companion object {
        private val client: WebSocketClient by lazy { WebSocketClient() }
        fun instance(): WebSocketClient = client
    }
}