package cuiliang.quicker.network;

import cuiliang.quicker.messages.MessageBase;
import com.google.gson.Gson;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 *  编码器将数据直接发出去(不做处理)
 */
public class MyDataEncoder extends ProtocolEncoderAdapter {

    @Override
    public void encode(IoSession session, Object message,
                       ProtocolEncoderOutput out) throws Exception {
        if (message instanceof MessageBase) {
            Gson gson = new Gson();
            String msgJson = gson.toJson(message);
            byte[] msgBytes = msgJson.getBytes("utf-8");

            IoBuffer buffer = IoBuffer.allocate(msgBytes.length + 16);
            buffer.putInt(0xFFFFFFFF);
            buffer.putInt(((MessageBase) message).getMessageType());
            buffer.putInt(msgBytes.length);
            buffer.put(msgBytes);
            buffer.putInt(0);
            buffer.flip();

            out.write(buffer);
        }

    }
}
