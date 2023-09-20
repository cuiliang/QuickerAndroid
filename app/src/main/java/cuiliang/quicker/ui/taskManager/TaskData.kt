package cuiliang.quicker.ui.taskManager

import cuiliang.quicker.taskManager.BaseTaskData
import cuiliang.quicker.util.GsonUtils

/**
 * Created by Voidcom on 2023/9/13 16:54
 * 定义任务数据结构
 */
class TaskData {
    //任务名
    var name: String = ""

    //任务状态：true启动
    var state: Boolean = false

    val events: ArrayList<TaskEditItemData> = arrayListOf()
    val taskActions: ArrayList<TaskEditItemData> = arrayListOf()

    constructor()

    constructor(state: Boolean) : this() {
        this.state = state
    }

    /**
     * 将任务数据转为json，主要用于本地缓存
     */
    override fun toString(): String = GsonUtils.toString(this)

    /**
     * 将ifCondition和runContent的内容经过格式转为str
     */
    fun toDescription(): String {
        val builder = StringBuilder()
        events.forEach {
            builder.append(it.title)
            if (it.subTitle.isNotEmpty())
                builder.append(":").append(it.subTitle)
            builder.append(", ")
        }
        builder.delete(builder.length - 2, builder.length).append(" | ")
        taskActions.forEach {
            builder.append(it.title)
            if (it.subTitle.isNotEmpty())
                builder.append(":").append(it.subTitle)
            builder.append(", ")
        }
        builder.deleteCharAt(builder.length - 1)
        return builder.toString()
    }

    companion object {
        fun jsonToTaskData(json: String): TaskData {
            return GsonUtils.toBean(json, TaskData::class.java)
        }
    }
}

class TaskEditItemData {
    var icon: Int = 0
    var title: String = ""
    var subTitle: String = ""

    constructor(icon: Int, title: String, subTitle: String) {
        this.icon = icon
        this.title = title
        this.subTitle = subTitle
    }

    constructor(data: BaseTaskData) :this(data.getIcon(),data.getName(),data.resultStr())

    override fun toString(): String = "icon:$icon, title:$title, subTitle:$subTitle"
}
