package cuiliang.quicker.network.websocket

import cuiliang.quicker.util.GsonUtils


/**
 * Created by Voidcom on 2023/9/11 17:57
 * https://getquicker.net/KC/Manual/Doc/websocketservice
 * 常规响应消息:
 * {
 * 	 messageType: 消息类型常量,
 *   serial: 消息编号,
 *   replyTo: 响应的请求消息编号,
 *   isSuccess: 操作是否成功,
 *   message: 操作失败时的提示消息,
 *   data: 可选的返回数据(文本或对象),
 *   extData: 可选的额外返回数据(文本或对象)
 * }
 *
 * 对应于消息中messageType参数的取值。
 * 2：命令请求消息，用于发送操作指令和内容。
 * 4：命令响应消息，返回指令操作结果。
 * 5：身份验证请求，客户端发送验证码。
 * 6：身份验证响应，返回密码是否正确。
 *
 * Description: 常规响应消息
 */
data class MsgResponseData(
    val messageType: Int,
    val replyTo: Int = -1,
    val isSuccess: Boolean,
    val serial: Int = 0,
    val message: String = "",
    val data: String = "",
    val extData: String = ""
) {
    override fun toString(): String = GsonUtils.toString(this)
}