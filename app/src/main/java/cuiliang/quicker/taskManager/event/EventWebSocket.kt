package cuiliang.quicker.taskManager.event

import android.content.Context
import cuiliang.quicker.R
import cuiliang.quicker.client.ConnectionStatus
import cuiliang.quicker.network.websocket.ConnectListener
import cuiliang.quicker.network.websocket.WebSocketClient
import cuiliang.quicker.taskManager.BaseEventOrAction
import cuiliang.quicker.taskManager.TaskEventType

/**
 * Created by Silent on 2023/9/18 16:49
 *
 */
class EventWebSocket : Event(), ConnectListener {

    override fun getIcon(): Int = R.drawable.ic_computer_black_24dp

    override fun getName(): String = "WebSocket连接成功"

    override fun showDialogAndCallback(context: Context, callback: (BaseEventOrAction) -> Unit) {
        callback(this)
    }

    override fun release(context: Context) {
        WebSocketClient.instance().connectListeners.remove(this)
    }

    override fun getType(): TaskEventType = TaskEventType.EVENT_WEBSOCKET_CONNECTING

    override fun init(context: Context) {
        WebSocketClient.instance().connectListeners.add(this)
    }

    override fun onStatus(status: ConnectionStatus) {
        eventState = status == ConnectionStatus.LoggedIn || status == ConnectionStatus.Connected
        if (status == ConnectionStatus.LoggedIn || status == ConnectionStatus.Connected) {
            listener?.runningListener()
        }
    }
}