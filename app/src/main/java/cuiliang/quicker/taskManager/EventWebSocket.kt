package cuiliang.quicker.taskManager

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import cuiliang.quicker.R

/**
 * Created by Silent on 2023/9/18 16:49
 *
 *
 */
class EventWebSocket : Event {
    constructor():super()

    constructor(parcel: Parcel) : super(parcel)

    override fun getIcon(): Int = R.drawable.ic_computer_black_24dp

    override fun getName(): String = "WebSocket连接成功"

    override fun showDialogAndCallback(context: Context, callback: (BaseTaskData) -> Unit) {
        callback(this)
    }

    override fun eventRunnable(): Runnable {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<EventWebSocket> {
        override fun createFromParcel(parcel: Parcel): EventWebSocket {
            return EventWebSocket(parcel)
        }

        override fun newArray(size: Int): Array<EventWebSocket?> {
            return arrayOfNulls(size)
        }
    }

}