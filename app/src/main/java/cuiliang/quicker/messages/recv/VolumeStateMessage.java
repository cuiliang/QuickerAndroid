package cuiliang.quicker.messages.recv;

import cuiliang.quicker.messages.MessageBase;

public class VolumeStateMessage implements MessageBase {
    public static final int MessageType = 2;

    public boolean Mute;

    public int MasterVolume;

    @Override
    public int getMessageType() {
        return MessageType;
    }
}
