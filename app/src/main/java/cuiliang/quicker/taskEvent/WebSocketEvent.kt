package cuiliang.quicker.taskEvent

import android.app.Dialog
import android.os.Bundle
import cuiliang.quicker.R

/**
 * Created by Silent on 2023/9/18 16:49
 *
 *
 */
class WebSocketEvent: Event() {
    override fun getIcon(): Int = R.drawable.ic_battery

    override fun getName(): String ="WebSocket通知"

    override fun getDialog(callback: (Bundle) -> Unit): Dialog {
//        return initDialog()
        return mDialog
    }

}