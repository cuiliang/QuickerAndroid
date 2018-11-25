package cuiliang.quicker.client;

import android.util.Log;

import cuiliang.quicker.events.ServerMessageEvent;
import cuiliang.quicker.events.SessionClosedEvent;
import cuiliang.quicker.messages.MessageBase;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.greenrobot.eventbus.EventBus;


public class MinaClientHandler extends IoHandlerAdapter {


    private static final String TAG = MinaClientHandler.class.getSimpleName();

    public static final String BROADCAST_ACTION = "com.commonlibrary.mina.broadcast";
    public static final String MESSAGE = "message";

    public MinaClientHandler() {

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        Log.e(TAG, " : 客户端调用exceptionCaught");

        // 异常，重连网络
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
        Log.e(TAG, "接收到服务器端消息：" + message.toString());

//        Message msg = new Message();
//        msg.what = 100;
//        msg.arg1 = ((MessageBase)message).getMessageType();
//        msg.obj = message;
//        _uiHandler.sendMessage(msg);

        EventBus.getDefault().post(new ServerMessageEvent((MessageBase) message));
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        Log.d(TAG, " : 客户端调用messageSent");
        //        session.close(true);//加上这句话实现短连接的效果，向客户端成功发送数据后断开连接
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        Log.d(TAG, " : 客户端调用sessionClosed  isClosing=" + session.isClosing());

        EventBus.getDefault().post(new SessionClosedEvent());


    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        Log.d(TAG, " : 客户端调用sessionCreated");


    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        Log.d(TAG, " : 客户端调用sessionIdle");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        Log.d(TAG, " : 客户端调用sessionOpened");


    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        super.inputClosed(session);

        Log.d(TAG, " : inputClosed");

        EventBus.getDefault().post(new SessionClosedEvent());

    }
}
