package cuiliang.quicker.taskManager

import cuiliang.quicker.taskManager.action.Action
import cuiliang.quicker.taskManager.event.Event
import cuiliang.quicker.util.GsonUtils

/**
 * Created by Voidcom on 2023/9/29 20:20
 * 描述如何将 @see [Task] 序列化和反序列化
 *
 * json:{"events":[{"eventState":true,"resultStr":"电量低于20%"}],"icon":0,"isEnable":true,"name":"充电通知","taskActions":[{"resultStr":"快没电了！"}]}
 */
class JsonTask(task: Task) {
    var icon: Int = 0
    var isEnable: Boolean = false
    var name: String = ""
    val events: ArrayList<JsonEvent> = arrayListOf()
    val actions: ArrayList<JsonAction> = arrayListOf()

    init {
        icon = task.icon
        isEnable = task.getIsEnable()
        name = task.name
        task.events.forEach {
            events.add(JsonEvent(it))
        }
        task.taskActions.forEach {
            actions.add(JsonAction(it))
        }
    }

    override fun toString(): String = GsonUtils.toString(this)

    fun toTask(): Task {
        val task = Task(isEnable)
        task.icon = icon
        task.name = name
        events.forEach {
            task.events.add(it.toEvent())
        }
        actions.forEach {
            task.taskActions.add(it.toAction())
        }
        return task
    }

    companion object {
        fun jsonToTask(json: String): JsonTask = GsonUtils.toBean(json, JsonTask::class.java)
    }
}

/**
 * @see cuiliang.quicker.taskManager.event.Event
 */
data class JsonEvent(val type: TaskEventType, val eventState: Boolean, val resultStr: String) {
    constructor(event: Event) : this(
        event.getType() as TaskEventType,
        event.eventState,
        event.description
    )

    override fun toString(): String = GsonUtils.toString(this)

    fun toEvent(): Event {
        val factory = TaskDataFactory()
        return factory.getEvent(type).let {
            it.eventState = eventState
            it.description = resultStr
            it
        }
    }
    companion object{
        fun jsonToEvent(json: String): JsonEvent = GsonUtils.toBean(json, JsonEvent::class.java)
    }
}

/**
 * @see cuiliang.quicker.taskManager.action.Action
 */
data class JsonAction(val type: TaskActionType, val resultStr: String) {
    constructor(action: Action) : this(action.getType() as TaskActionType, action.description)

    override fun toString(): String = GsonUtils.toString(this)

    fun toAction(): Action {
        val factory = TaskDataFactory()
        return factory.getAction(type).let {
            it.description = resultStr
            it
        }
    }

    companion object{
        fun jsonToAction(json: String): JsonAction = GsonUtils.toBean(json, JsonAction::class.java)
    }
}

