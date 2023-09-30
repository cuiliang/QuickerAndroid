package cuiliang.quicker.taskManager.event

import android.content.Context
import cuiliang.quicker.R
import cuiliang.quicker.taskManager.BaseEventOrAction
import cuiliang.quicker.taskManager.TaskEventType
import cuiliang.quicker.taskManager.TaskType

/**
 * Created by Voidcom on 2023/9/29 22:48
 */
class EventAdd : Event() {
    override fun getIcon(): Int = R.drawable.ic_add

    override fun getName(): String = "添加条件"
    override fun resultStr(): String = "如：当电量低于20%"

    override fun getType(): TaskType = TaskEventType.EVENT_ADD

    override fun showDialogAndCallback(context: Context, callback: (BaseEventOrAction) -> Unit) {
    }

    override fun init(context: Context) {
    }

    override fun release(context: Context) {
    }
}