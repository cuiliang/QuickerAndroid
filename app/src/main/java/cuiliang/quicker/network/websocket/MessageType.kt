package cuiliang.quicker.network.websocket

/**
 * Created by Voidcom on 2023/9/11 21:35
 *
 * Description: TODO
 */
enum class MessageType(private val i: Int) {
    //2：命令请求消息，用于发送操作指令和内容。
    REQUEST_COMMAND(2),
    //5：身份验证请求，客户端发送验证码。
    REQUEST_AUTH(5),

    //4：命令响应消息，返回指令操作结果。
    RESPONSE_COMMAND(4),
    //6：身份验证响应，返回密码是否正确。
    RESPONSE_AUTH(6);

    fun getValue(): Int = i
}