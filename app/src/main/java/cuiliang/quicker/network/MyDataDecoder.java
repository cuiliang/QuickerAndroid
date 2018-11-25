package cuiliang.quicker.network;

import android.util.Log;

import cuiliang.quicker.messages.recv.LoginStateMessage;
import cuiliang.quicker.messages.send.ButtonClickedMessage;
import cuiliang.quicker.messages.MessageBase;
import cuiliang.quicker.messages.recv.UpdateButtonsMessage;
import cuiliang.quicker.messages.recv.VolumeStateMessage;
import cuiliang.quicker.messages.send.CommandMessage;

import com.google.gson.Gson;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.nio.charset.Charset;

public class MyDataDecoder extends CumulativeProtocolDecoder {
    private static final String TAG = MyDataDecoder.class.getSimpleName();
    /**
     * 返回值含义:
     * 1、当内容刚好时，返回false，告知父类接收下一批内容
     * 2、内容不够时需要下一批发过来的内容，此时返回false，这样父类 CumulativeProtocolDecoder
     * 会将内容放进IoSession中，等下次来数据后就自动拼装再交给本类的doDecode
     * 3、当内容多时，返回true，因为需要再将本批数据进行读取，父类会将剩余的数据再次推送本类的doDecode方法
     */
    @Override
    public boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out)
            throws Exception {
        Log.d(TAG, "解码消息... len = " + in.remaining());

        /**
         * 假定消息格式为：消息头（int类型：表示消息体的长度、short类型：表示事件号）+消息体
         */
        if (in.remaining() < 12)
        {
            return false;
        }

            //以便后继的reset操作能恢复position位置
            in.mark();

            int headFlag = in.getInt();
            int msgType = in.getInt();
            int msgLength = in.getInt();

            if (  in.remaining() >= (msgLength + 4) ) {
                String msgJson = in.getString(msgLength, Charset.forName("UTF-8").newDecoder());
                int end = in.getInt();

                MessageBase msg = deserializeMsg(msgType, msgJson);
                out.write(msg);

                if (in.hasRemaining()) {
                    return true;
                }else {
                    return false;
                }
            }else {
                in.reset();

                // 消息不完整
                return false;
            }


    }

    private MessageBase deserializeMsg(int msgType, String content){
        Gson gson = new Gson();
        switch(msgType) {
            case UpdateButtonsMessage.MessageType:
                // update buttons
                return gson.fromJson(content, UpdateButtonsMessage.class);
            case ButtonClickedMessage.MessageType:
                return gson.fromJson(content, ButtonClickedMessage.class);
            case VolumeStateMessage.MessageType:
                return gson.fromJson(content, VolumeStateMessage.class);
            case LoginStateMessage.MessageType:
                return gson.fromJson(content, LoginStateMessage.class);
            case CommandMessage.MessageType:
                return gson.fromJson(content, CommandMessage.class);
                default:
                    Log.e(TAG, "不支持的消息类型：" + msgType);

        }
        return null;
    }
}
