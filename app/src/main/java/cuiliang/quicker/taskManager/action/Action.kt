package cuiliang.quicker.taskManager.action

import cuiliang.quicker.taskManager.BaseEventOrAction

/**
 * Created by Voidcom on 2023/9/19 22:16
 * 抽象类，抽象了任务满足条件后执行的结果。
 * @see cuiliang.quicker.taskManager.event.Event
 */
abstract class Action : BaseEventOrAction {
    var description: String = ""

    override fun resultStr(): String = description

    /**
     * 条件达成后执行的操作
     */
    abstract fun actionRunnable(): Runnable?
}