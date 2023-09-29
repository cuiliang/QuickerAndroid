package cuiliang.quicker.taskManager

import android.content.Context
import cuiliang.quicker.service.TaskManagerService
import cuiliang.quicker.taskManager.action.Action
import cuiliang.quicker.taskManager.event.Event
import cuiliang.quicker.taskManager.event.EventRunningListener

/**
 * Created by Voidcom on 2023/9/13 16:54
 * 定义任务数据结构。
 * 一个TaskData代表一个任务
 */
class Task(state: Boolean) : EventRunningListener {
    //任务名
    var name: String = ""

    var icon: Int = 0

    //是否启用
    private var isEnable: Boolean = state

    //事件列表
    val events: ArrayList<Event> = arrayListOf()

    //动作列表，当条件达成是执行的操作
    val taskActions: ArrayList<Action> = arrayListOf()


    /**
     * 事件运行监听；
     * 当一个事件条件达成时会执行这个接口，然后会检查事件列表其余事件，如果都达成了就执行动作列表
     */
    override fun runningListener() {
        //判断是否全部触发条件都达成；如果是，tmp应该是一个空list
        val tmp = events.filter {
            !it.eventState
        }
//        KLog.d("Task:", "taskName:$name; tmpSize:${tmp.size}")
        if (tmp.isEmpty()) {
            taskActions.forEach {
                it.actionRunnable()?.run {
                    TaskManagerService.threadPool.execute(this)
                }
            }
        }
    }

    /**
     * 任务描述
     */
    fun toDescription(): String {
        val builder = StringBuilder()
        events.forEach {
            builder.append(it.getName())
            if (it.description.isNotEmpty())
                builder.append(":").append(it.description)
            builder.append(", ")
        }
        builder.delete(builder.length - 2, builder.length).append(" | ")
        taskActions.forEach {
            builder.append(it.getName())
            if (it.description.isNotEmpty())
                builder.append(":").append(it.description)
            builder.append(", ")
        }
        builder.deleteCharAt(builder.length - 1)
        return builder.toString()
    }

    fun updateState(context: Context, b: Boolean) {
        if (isEnable == b) return
        if (b) {
            taskInit(context)
        } else {
            release(context)
        }
        isEnable = b
    }

    fun getIsEnable() = isEnable

    fun taskInit(context: Context) {
        events.forEach {
            it.listener = this
            it.init(context)
        }
        taskActions.forEach { it.init(context) }
    }

    fun release(context: Context) {
        events.forEach { it.release(context) }
        taskActions.forEach { it.release(context) }
    }
}
