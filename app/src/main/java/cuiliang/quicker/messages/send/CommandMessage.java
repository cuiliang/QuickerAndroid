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

    public static final String  LOCK_PANEL = "LOCK_PANEL"; //锁定/解锁面板

    public static final String  CHANGE_PAGE = "CHANGE_PAGE"; //面板翻页;


    public static final String  DATA_PAGE_GLOBAL_LEFT = "DATA_GLOBAL_LEFT"; //全局面板向左翻页;
    public static final String  DATA_PAGE_GLOBAL_RIGHT = "DATA_GLOBAL_RIGHT"; //全局面板向右翻页;
    public static final String  DATA_PAGE_CONTEXT_LEFT = "DATA_CONTEXT_LEFT"; //上下文面板向左翻页;
    public static final String  DATA_PAGE_CONTEXT_RIGHT = "DATA_CONTEXT_RIGHT"; //上下文面板向右翻页;

    public String Command;

    public String Data;

    @Override
    public int getMessageType() {
        return MessageType;
    }
}
