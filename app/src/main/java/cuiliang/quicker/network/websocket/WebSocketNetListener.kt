package cuiliang.quicker.network.websocket

open class WebSocketNetListener {
    open fun onRequest(data: MsgRequestData): MsgRequestData = data

    open fun onResponse(data: MsgResponseData) {}

    open fun onFail(error: String) {}
}