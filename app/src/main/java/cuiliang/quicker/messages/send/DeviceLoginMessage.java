package cuiliang.quicker.messages.send;

import cuiliang.quicker.messages.MessageBase;

/**
 * 通用命令消息
 */
public class DeviceLoginMessage implements MessageBase {
    public static final int MessageType = 200;

    public static final String OPEN_MAINWIN = "OPEN_MAINWIN";


    /// <summary>
    /// 连接验证码，防止误连接
    /// </summary>
    public String ConnectionCode;

    /// <summary>
    /// 客户端版本
    /// </summary>
    public String Version;

    /// <summary>
    /// 设备名称
    /// </summary>
    public String DeviceName;

    @Override
    public int getMessageType() {
        return MessageType;
    }
}
