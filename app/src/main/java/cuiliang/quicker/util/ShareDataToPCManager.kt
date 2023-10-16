package cuiliang.quicker.util

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import cuiliang.quicker.network.NetRequestObj
import cuiliang.quicker.network.NetWorkManager.Companion.getInstant
import cuiliang.quicker.network.NetWorkManager.RequestCallback
import cuiliang.quicker.network.shareToPc.ShareApi
import cuiliang.quicker.network.websocket.MessageType
import cuiliang.quicker.network.websocket.MsgRequestData
import cuiliang.quicker.network.websocket.MsgResponseData
import cuiliang.quicker.network.websocket.WebSocketClient
import cuiliang.quicker.network.websocket.WebSocketNetListener
import cuiliang.quicker.ui.taskManager.TaskConstant
import okhttp3.Response
import java.io.IOException

/**
 * Created by Void on 2020/4/12 12:12
 * 分享数据到PC端。
 * 分享可以分两种，一种是通过WebSocket，另一种是推送服务
 * 默认情况使用WebSocket，当WebSocket没连上才用推送
 * [文档](https://getquicker.net/kc/manual/doc/connection)
 */
class ShareDataToPCManager private constructor() {
    //用户名，用于分享操作
    val userName: MutableState<String> by lazy { mutableStateOf("") }

    //用户推送验证码，用于分享操作
    val authCode: MutableState<String> by lazy { mutableStateOf("") }

    /**
     * 是否有用户信息，这是分享操作成功的必要条件
     * @return true有
     */
    val isHaveUserInfo: Boolean = userName.value.isNotEmpty() && authCode.value.isNotEmpty()

    fun readShareConfig() {
        userName.value = SPUtils.getString(SHARE_USER_NAME, "")
        authCode.value = SPUtils.getString(SHARE_AUTH_CODE, "")
    }

    fun saveShareConfig() {
        KLog.d(TAG, "用户名:$userName; 推送验证码:$authCode")
        SPUtils.putString(SHARE_USER_NAME, userName.value)
        SPUtils.putString(SHARE_AUTH_CODE, authCode.value)
    }

    fun sendShareText(text: String, callback: ((Boolean) -> Unit)) {
        if (WebSocketClient.instance().isConnected()) {
            WebSocketClient.instance().newCall(object : WebSocketNetListener() {
                override fun onRequest(data: MsgRequestData): MsgRequestData {
                    return data.setData(
                        MessageType.REQUEST_COMMAND.getValue(),
                        operation = "action",
                        data = text,
                        action = TaskConstant.ACTION_SHARE_ID,
                        wait = false
                    )
                }

                override fun onResponse(data: MsgResponseData) {
                    super.onResponse(data)
                    callback(true)
                }

                override fun onFail(error: String) {
                    super.onFail(error)
                    //WebSocket失败后尝试使用推送服务发送分享数据
                    pushShareToPC(text, callback)
                }
            })
        } else {
            pushShareToPC(text, callback)
        }
    }

    fun pushShareToPC(data: String, callback: ((Boolean) -> Unit)) {
        pushShareToPC(data, object : RequestCallback {
            override fun onSuccess(response: Response) {
                callback(true)
            }

            override fun onError(e: IOException, errorMessage: String?) {
                callback(false)
            }
        })
    }

    /**
     * 使用推送服务分享内容到PC
     * 注：该请求为异步操作
     * [文档](https://getquicker.net/kc/manual/doc/connection)
     */
    fun pushShareToPC(data: String, callback: RequestCallback?) {
        if (!isHaveUserInfo) {
            Log.e(TAG, "用户名或推送验证码为空！userName:$userName;userCode:$authCode")
            callback?.onError(IOException(""))
            return
        }
        val requestObj = NetRequestObj(ShareApi.shareUrl, callback)
        requestObj.addBody("toUser", userName.value)
        requestObj.addBody("code", authCode.value)
        requestObj.addBody("operation", "action")
        requestObj.addBody("data", data)
        requestObj.isEncode = true
        getInstant().executeRequest1(requestObj)
    }

    companion object {
        private val TAG = ShareDataToPCManager::class.java.simpleName
        const val SHARE_USER_NAME = "SHARE_USER_NAME"
        const val SHARE_AUTH_CODE = "SHARE_AUTH_CODE"

        val instant: ShareDataToPCManager by lazy { ShareDataToPCManager() }
    }
}
