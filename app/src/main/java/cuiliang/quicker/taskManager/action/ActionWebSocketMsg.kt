package cuiliang.quicker.taskManager.action

import android.content.Context
import androidx.appcompat.widget.AppCompatEditText
import cuiliang.quicker.R
import cuiliang.quicker.network.websocket.MessageType
import cuiliang.quicker.network.websocket.MsgRequestData
import cuiliang.quicker.network.websocket.WebSocketClient
import cuiliang.quicker.network.websocket.WebSocketNetListener
import cuiliang.quicker.taskManager.BaseEventOrAction
import cuiliang.quicker.taskManager.TaskActionType
import cuiliang.quicker.ui.taskManager.TaskConstant

/**
 * Created by Voidcom on 2023/9/19 22:21
 * TODO
 */
class ActionWebSocketMsg : Action {

    constructor() : super()

    constructor(s: String) {
        this.description = s
    }

    override fun getIcon(): Int = R.drawable.ic_notifications_black_24dp

    override fun getName(): String = "WebSocket消息通知"

    override fun getType(): TaskActionType = TaskActionType.ACTION_WEBSOCKET_MESSAGE

    override fun actionRunnable(): Runnable? {
        //给PC发送一条连接成功通知
        WebSocketClient.instance().newCall(ConnectedHint())
        return null
    }

    override fun showDialogAndCallback(context: Context, callback: (BaseEventOrAction) -> Unit) {
        val et = AppCompatEditText(context)
        et.hint = "例：WebSocket连接成功！"
        getDialogBuilder(context).setTitle(getName())
            .setView(et)
            .setNegativeButton(R.string.btnCancel, null)
            .setPositiveButton(R.string.btnAccept) { a, _ ->
                a.dismiss()
                description = et.text.toString()
                callback(this)
            }.create().show()
    }

    override fun init(context: Context) {
    }

    override fun release(context: Context) {
    }

    private inner class ConnectedHint : WebSocketNetListener() {
        override fun onRequest(data: MsgRequestData): MsgRequestData {
            return data.setData(
                MessageType.REQUEST_COMMAND.getValue(),
                "action",
                description,
                TaskConstant.ANDROID_NOTIFICATION,
                "",
                false
            )
        }
    }
}