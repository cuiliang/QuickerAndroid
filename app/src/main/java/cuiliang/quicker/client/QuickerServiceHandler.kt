package cuiliang.quicker.client

import androidx.collection.arraySetOf
import cuiliang.quicker.messages.MessageBase
import cuiliang.quicker.util.KLog
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IdleStatus
import org.apache.mina.core.session.IoSession

/**
 * Created by voidcom on 2023/10/23
 *
 */
class QuickerServiceHandler private constructor() : IoHandlerAdapter() {
    private val listeners = arraySetOf<QuickerServiceListener>()

    override fun exceptionCaught(session: IoSession?, cause: Throwable?) {
        super.exceptionCaught(session, cause)
        KLog.e(TAG, "异常，重连网络")
    }

    override fun messageReceived(session: IoSession, message: Any) {
        super.messageReceived(session, message)
        KLog.e(TAG, "接收到服务器端消息：$message")
        listeners.forEach { it.onMessage(message as MessageBase) }
    }

    override fun messageSent(session: IoSession?, message: Any?) {
        super.messageSent(session, message)
        KLog.d(TAG, "客户端调用messageSent")
//        session?.close(true);//加上这句话实现短连接的效果，向客户端成功发送数据后断开连接
    }

    override fun sessionCreated(session: IoSession?) {
        super.sessionCreated(session)
        KLog.d(TAG,"客户端调用sessionCreated")
    }

    override fun sessionIdle(session: IoSession?, status: IdleStatus?) {
        super.sessionIdle(session, status)
        KLog.d(TAG,"客户端调用sessionIdle")
    }

    override fun sessionOpened(session: IoSession?) {
        super.sessionOpened(session)
        KLog.d(TAG,"客户端调用sessionOpened")
    }

    override fun sessionClosed(session: IoSession?) {
        super.sessionClosed(session)
        KLog.d(TAG, "客户端调用sessionClosed-isClosing=${session?.isClosing}")
        listeners.forEach { it.onClose() }
    }

    override fun inputClosed(session: IoSession?) {
        super.inputClosed(session)
        KLog.d(TAG, ":inputClosed")
        listeners.forEach { it.onClose() }
    }

    fun addListener(listener: QuickerServiceListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: QuickerServiceListener) {
        listeners.remove(listener)
    }

    companion object {
        const val TAG = "QuickerServiceHandler"
        val instant: QuickerServiceHandler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { QuickerServiceHandler() }
    }
}

open class QuickerServiceListener {
    open fun onMessage(msg: MessageBase){}
    open fun onClose(){}
}