package cuiliang.quicker.messages.recv;

import cuiliang.quicker.messages.MessageBase;

public class UpdateButtonsMessage implements MessageBase {
    public static final int MessageType = 1;

    public String ProfileName;

    /// 全局面板数量
    public int GlobalPageCount;

    /// <summary>
    /// 当前全局面板编号
    /// </summary>
    public int GlobalPageIndex;

    /// <summary>
    /// 上下文面板数量
    /// </summary>
    public int ContextPageCount;

    /// <summary>
    /// 当前上下文面板编号
    /// </summary>
    public int ContextPageIndex;

    /// <summary>
    /// 上下文面板切换是否锁定
    /// </summary>
    public boolean IsContextPanelLocked;

    //
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
