package cuiliang.quicker.messages.recv;

import cuiliang.quicker.messages.MessageBase;

public class UpdateButtonsMessage implements MessageBase {
    public static final int MessageType = 1;

    public String ProfileName;

    public ButtonItem[] Buttons;

    @Override
    public int getMessageType() {
        return MessageType;
    }

    /// <summary>
    /// 要更新的每个按钮的信息
    /// </summary>
    public class ButtonItem
    {


        /// <summary>
        /// 编号
        /// </summary>
        public int Index;

        /// <summary>
        /// 是否启用
        /// </summary>
        public boolean IsEnabled;

        /// <summary>
        /// 按钮文字标签
        /// </summary>
        public String Label;

        /// <summary>
        /// 图标文件名
        /// </summary>
        public String IconFileName;

        /// <summary>
        /// base64编码的图标文件内容
        /// </summary>
        public String IconFileContent;
    }
}
