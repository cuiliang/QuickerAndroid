package cuiliang.quicker.events;

import cuiliang.quicker.messages.MessageBase;

/**
 * pc接收到消息的处理
 */
public class ServerMessageEvent {
    public ServerMessageEvent(MessageBase msg) {
        this.serverMessage = msg;
    }

    // 从pc接收到的消息
    public MessageBase serverMessage;
}
