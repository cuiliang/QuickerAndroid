package cuiliang.quicker.ui.taskManager

import cuiliang.quicker.util.GsonUtils

/**
 * Created by Voidcom on 2023/9/13 16:54
 * 定义任务数据结构
 */
data class TaskData(
    //任务名
    val name: String,
    val description: String,
    //任务状态：true启动
    val state: Boolean,

    val ifCondition: List<TaskEditItemData> = arrayListOf(),
    val runContent: List<TaskEditItemData> = arrayListOf()
) {
    override fun toString(): String = GsonUtils.toString(this)
}

data class TaskEditItemData(val icon: Int, val title: String, val subTitle: String) {
    override fun toString(): String = "icon:$icon, title:$title, subTitle:$subTitle"
}
