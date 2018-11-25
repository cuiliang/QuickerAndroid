package cuiliang.quicker.messages.send;

import cuiliang.quicker.messages.MessageBase;

/**
 * 通用命令消息
 */
public class CommandMessage implements MessageBase {
    public static final int MessageType = 110;

    public static final String OPEN_MAINWIN = "OPEN_MAINWIN";
    public static final String START_VOICE_INPUT = "START_VOICE_INPUT";
    public static final String RESEND_STATE = "RESEND_STATE"; // 客户端请求pc重新发送状态（按钮、音量等）

    public String Command;

    public String Data;

    @Override
    public int getMessageType() {
        return MessageType;
    }
}
