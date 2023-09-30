package cuiliang.quicker.taskManager.event

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import cuiliang.quicker.R
import cuiliang.quicker.taskManager.BaseEventOrAction
import cuiliang.quicker.taskManager.TaskEventType
import cuiliang.quicker.util.KLog

/**
 * Created by Silent on 2023/9/18 17:23
 *
 */
class EventBatteryStatus : Event {
    private var receiver: BatteryBroadcast? = null

    private var targetBattery: Int = 20

    constructor() : super()

    constructor(s: String) {
        this.description = s
    }

    override fun getIcon(): Int = R.drawable.ic_battery

    override fun getName(): String = "电池状态"

    override fun getType(): TaskEventType = TaskEventType.EVENT_BATTERY_STATUS

    override fun showDialogAndCallback(context: Context, callback: (BaseEventOrAction) -> Unit) {
        getDialogBuilder(context)
            .setTitle(getName())
            .setItems(R.array.dialog_battery_status_list) { a, b ->
                description = context.resources.getStringArray(R.array.dialog_battery_status_list)[b]
                callback(this)
                a.dismiss()
            }.create().show()
    }

    override fun init(context: Context) {
        targetBattery = description.substring(4, 6).toInt()
        if (receiver != null) return
        receiver = BatteryBroadcast()
        context.applicationContext.registerReceiver(
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    override fun release(context: Context) {
        if (receiver == null) return
        context.applicationContext.unregisterReceiver(receiver)
        receiver = null
    }

    inner class BatteryBroadcast : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //在这里接受电量信息
            val batteryPct: Int = intent?.let {
                val level: Int = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                level * 100 / scale
            } ?: -1
            KLog.d(
                "Event",
                "EventBatteryStatus-batteryPct:$batteryPct; targetBattery:$targetBattery"
            )
            eventState = batteryPct in 1..targetBattery
            if (batteryPct <= targetBattery) listener?.runningListener()
        }
    }
}