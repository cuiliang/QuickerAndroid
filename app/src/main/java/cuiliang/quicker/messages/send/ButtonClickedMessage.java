package cuiliang.quicker.messages.send;

import cuiliang.quicker.messages.MessageBase;

public class ButtonClickedMessage implements MessageBase {
    public static final int MessageType = 101;
    public int ButtonIndex;

    @Override
    public int getMessageType() {
        return MessageType;
    }

}
