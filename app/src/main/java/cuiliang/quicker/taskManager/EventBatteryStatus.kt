package cuiliang.quicker.taskManager

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import cuiliang.quicker.R

/**
 * Created by Silent on 2023/9/18 17:23
 *
 *
 */
class EventBatteryStatus : Event {
    constructor() : super()

    constructor(parcel: Parcel) : super(parcel)

    constructor(s: String) {
        this.resultStr = s
    }

    override fun getIcon(): Int = R.drawable.ic_battery

    override fun getName(): String = "电池状态"

    override fun showDialogAndCallback(context: Context, callback: (BaseTaskData) -> Unit) {
        getDialogBuilder(context)
            .setTitle(getName())
            .setItems(R.array.dialog_battery_status_list) { a, b ->
                a.dismiss()
                resultStr = context.resources.getStringArray(R.array.dialog_battery_status_list)[b]
                callback(this)
            }.create().show()
    }

    override fun eventRunnable(): Runnable {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<EventBatteryStatus> {
        override fun createFromParcel(parcel: Parcel): EventBatteryStatus {
            return EventBatteryStatus(parcel)
        }

        override fun newArray(size: Int): Array<EventBatteryStatus?> {
            return arrayOfNulls(size)
        }
    }
}