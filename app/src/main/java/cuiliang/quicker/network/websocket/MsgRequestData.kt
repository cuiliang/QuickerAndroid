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
 *   extraData: '可选的额外数据，文本或对象',
 *   wait: 是否等待操作返回结果
 * }
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
    private val serial:Int = v
    private var messageType: Int = 0
    private var operation: String = ""
    private var data: String = ""
    private var action: String = ""
    private var extraData: String = ""
    private var wait: Boolean = false

    fun setData(
        type: Int,
        operation: String = "",
        data: String = "",
        action: String = "",
        extraData: String = "",
        wait: Boolean = false
    ): MsgRequestData {
        this.messageType = type
        this.operation = operation
        this.data = data
        this.action = action
        this.extraData = extraData
        this.wait = wait
        return this
    }


    override fun toString(): String = GsonUtils.toString(this)
}