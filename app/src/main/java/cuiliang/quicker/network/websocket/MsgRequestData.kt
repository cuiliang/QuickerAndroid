package cuiliang.quicker.network.websocket

import cuiliang.quicker.util.GsonUtils

/**
 * Created by Voidcom on 2023/9/11 17:57
 * {
 * 	 messageType: 消息类型常量,
 *   serial: 消息编号,
 *   operation: '操作类型,如copy将data参数内容写入剪贴板',
 *   data: '数据，为文本，也可能为对象',
 *   action: '操作类型为action时指定执行的动作名称或id',
 *   extData: '可选的额外数据，文本或对象',
 *   wait: 是否等待操作返回结果
 * }

 * operation（可选）：操作类型，默认为 copy 。可选值：
 * copy  将内容复制到剪贴板
 * paste  将内容粘贴到当前窗口
 * action 运行动作。此时通过“action”参数传入动作名称或ID，通过“data”参数传入动作参数（可选）。
 * open 打开网址。此时通过data参数传入要打开的网址。
 * input sendkeys 模拟输入内容。此时通过data传入“模拟按键B”语法格式的内容。(1.27.3+版本请使用sendkeys)
 * inputtext 模拟输入文本（原样输入）。
 * inputscript 多步骤输入。组合多个键盘和鼠标输入步骤，参考文档。 （1.28.16+）
 * downloadfile 下载文件。下载data参数中给定的文件网址（单个）。 （1.28.16+）
 * 支持sendfile（传送文件）、pasteimage（粘贴图片到当前窗口）
 *
 * 对应于消息中messageType参数的取值。
 * 2：命令请求消息，用于发送操作指令和内容。
 * 4：命令响应消息，返回指令操作结果。
 * 5：身份验证请求，客户端发送验证码。
 * 6：身份验证响应，返回密码是否正确。
 *
 * Description: 常规请求消息
 */
class MsgRequestData(v: Int) {
    val serial:Int = v
    var messageType: Int = 0
    var operation: String = ""
    var data: String = ""
    var action: String = ""
    var extData: String = ""
    var wait: Boolean = false

    fun setData(
        type: Int,
        operation: String = "",
        data: String = "",
        action: String = "",
        extData: String = "",
        wait: Boolean = false
    ): MsgRequestData {
        this.messageType = type
        this.operation = operation
        this.data = data
        this.action = action
        this.extData = extData
        this.wait = wait
        return this
    }


    override fun toString(): String = GsonUtils.toString(this)
}