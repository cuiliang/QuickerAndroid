package cuiliang.quicker.messages.send;

import cuiliang.quicker.messages.MessageBase;

public class PhotoMessage  implements MessageBase {
    public static final int MessageType = 105;

    public String FileName ;

    public String Data;

    @Override
    public int getMessageType() {
        return MessageType;
    }

}