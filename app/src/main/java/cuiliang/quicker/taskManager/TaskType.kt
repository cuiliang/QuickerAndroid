package cuiliang.quicker.taskManager

/**
 * Created by Silent on 2023/9/18 16:49
 *
 *
 */
interface TaskType

enum class TaskEventType(private var v: String) : TaskType {
    EVENT_ADD("EVENT_ADD"),
    EVENT_WEBSOCKET_CONNECTING("EVENT_WEBSOCKET_CONNECTING"),
    EVENT_BATTERY_STATUS("EVENT_BATTERY_STATUS");

    override fun toString(): String = v
}

enum class TaskActionType(private val v: String) : TaskType {
    ACTION_ADD("ACTION_ADD"),
    ACTION_WEBSOCKET_MESSAGE("ACTION_WEBSOCKET_MESSAGE");

    override fun toString(): String = v
}