package cuiliang.quicker.taskEvent

import android.app.Dialog
import android.os.Bundle
import cuiliang.quicker.R

/**
 * Created by Silent on 2023/9/18 17:23
 *
 *
 */
class BatteryStatusEvent: Event() {
    override fun getIcon(): Int = R.drawable.ic_battery

    override fun getName(): String ="电池状态"

    override fun getDialog(callback: (Bundle) -> Unit): Dialog {
        return mDialog
    }
}