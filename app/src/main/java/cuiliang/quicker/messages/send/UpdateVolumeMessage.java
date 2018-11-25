package cuiliang.quicker.messages.send;

import cuiliang.quicker.messages.MessageBase;

public class UpdateVolumeMessage implements MessageBase {
    public static final int MessageType = 103;

    public int MasterVolume;

    @Override
    public int getMessageType() {
        return MessageType;
    }
}
