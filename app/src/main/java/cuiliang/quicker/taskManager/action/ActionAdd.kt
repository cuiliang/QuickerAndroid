package cuiliang.quicker.taskManager.action

import android.content.Context
import cuiliang.quicker.R
import cuiliang.quicker.taskManager.BaseEventOrAction
import cuiliang.quicker.taskManager.TaskActionType
import cuiliang.quicker.taskManager.TaskType

/**
 * Created by Voidcom on 2023/9/29 22:51
 * TODO
 */
class ActionAdd : Action() {
    override fun actionRunnable(): Runnable? = null

    override fun getIcon(): Int = R.drawable.ic_add

    override fun getName(): String = "添加结果"
    override fun resultStr(): String = "例如：发送充电提示通知"

    override fun getType(): TaskType = TaskActionType.ACTION_ADD

    override fun showDialogAndCallback(context: Context, callback: (BaseEventOrAction) -> Unit) {
    }

    override fun init(context: Context) {
    }

    override fun release(context: Context) {
    }
}