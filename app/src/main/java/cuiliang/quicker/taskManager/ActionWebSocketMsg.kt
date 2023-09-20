package cuiliang.quicker.taskManager

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.appcompat.widget.AppCompatEditText
import cuiliang.quicker.R

/**
 * Created by Voidcom on 2023/9/19 22:21
 * TODO
 */
class ActionWebSocketMsg : Action {

    constructor() : super()

    constructor(parcel: Parcel) : super(parcel)

    constructor(s: String) {
        this.resultStr = s
    }

    override fun getIcon(): Int = R.drawable.ic_notifications_black_24dp

    override fun getName(): String = "WebSocket消息通知"

    override fun actionRunnable(): Runnable {
        TODO("Not yet implemented")
    }

    override fun showDialogAndCallback(context: Context, callback: (BaseTaskData) -> Unit) {
        val et = AppCompatEditText(context)
        et.hint = "例：WebSocket连接成功！"
        getDialogBuilder(context).setTitle(getName())
            .setView(et)
            .setNegativeButton(R.string.btnCancel, null)
            .setPositiveButton(R.string.btnAccept) { a, _ ->
                a.dismiss()
                resultStr = et.text.toString()
                callback(this)
            }.create().show()
    }

    companion object CREATOR : Parcelable.Creator<ActionWebSocketMsg> {
        override fun createFromParcel(parcel: Parcel): ActionWebSocketMsg {
            return ActionWebSocketMsg(parcel)
        }

        override fun newArray(size: Int): Array<ActionWebSocketMsg?> {
            return arrayOfNulls(size)
        }
    }
}