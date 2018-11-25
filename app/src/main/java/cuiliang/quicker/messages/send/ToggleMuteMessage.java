package cuiliang.quicker.messages.send;

import cuiliang.quicker.messages.MessageBase;

public class ToggleMuteMessage implements MessageBase {
    public static final int MessageType = 102;

    @Override
    public int getMessageType() {
        return MessageType;
    }
}
