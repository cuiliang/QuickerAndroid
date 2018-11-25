package cuiliang.quicker.messages.send;

import cuiliang.quicker.messages.MessageBase;



public class TextDataMessage implements MessageBase {

    // 文本内容：二维码
    public static final int TYPE_QRCODE = 1;

    // 文本内容：语音识别
    public static final int TYPE_VOICE_RECOGNITION = 2;

    public static final int MessageType = 104;

    public int DataType ;

    public String Data;

    @Override
    public int getMessageType() {
        return MessageType;
    }

}
