package cuiliang.quicker.taskManager

import cuiliang.quicker.taskManager.action.Action
import cuiliang.quicker.taskManager.action.ActionAdd
import cuiliang.quicker.taskManager.action.ActionWebSocketMsg
import cuiliang.quicker.taskManager.event.Event
import cuiliang.quicker.taskManager.event.EventAdd
import cuiliang.quicker.taskManager.event.EventBatteryStatus
import cuiliang.quicker.taskManager.event.EventWebSocket

/**
 * Created by voidcom on ${DATE} ${TIME}
 * @see TaskType
 *
 */
class TaskDataFactory {

    fun getEvent(key: TaskEventType): Event {
        return when (key) {
            TaskEventType.EVENT_ADD -> EventAdd()
            TaskEventType.EVENT_WEBSOCKET_CONNECTING -> EventWebSocket()
            TaskEventType.EVENT_BATTERY_STATUS -> EventBatteryStatus()
        }
    }

    fun getEvent(key: String): Event? {
        return when (key) {
            TaskEventType.EVENT_ADD.toString() -> EventAdd()
            TaskEventType.EVENT_WEBSOCKET_CONNECTING.toString() -> EventWebSocket()
            TaskEventType.EVENT_BATTERY_STATUS.toString() -> EventBatteryStatus()
            else -> null
        }
    }

    fun getAction(key: TaskActionType): Action {
        return when (key) {
            TaskActionType.ACTION_ADD -> ActionAdd()
            TaskActionType.ACTION_WEBSOCKET_MESSAGE -> ActionWebSocketMsg()
        }
    }

    fun getAction(key: String): Action? {
        return when (key) {
            TaskActionType.ACTION_ADD.toString() -> ActionAdd()
            TaskActionType.ACTION_WEBSOCKET_MESSAGE.toString() -> ActionWebSocketMsg()
            else -> null
        }
    }

    /**
     * 获取所有的事件
     */
    fun getEvents(): List<Event> {
        return arrayListOf<Event>().apply {
            TaskEventType.values().forEach {
                if (it != TaskEventType.EVENT_ADD) this.add(getEvent(it))
            }
        }
    }

    fun getActions(): List<Action> {
        return arrayListOf<Action>().apply {
            TaskActionType.values().forEach {
                if (it != TaskActionType.ACTION_ADD)this.add(getAction(it))
            }
        }
    }

    fun defaultTaskConfig(): List<Task> {
        val list = arrayListOf<Task>()
        list.add(Task(true).apply {
            name = "WebSocket连接通知"
            events.add(EventWebSocket())
            taskActions.add(ActionWebSocketMsg("WebSocket连接成功!"))
        })

        list.add(Task(true).apply {
            name = "充电通知"
            events.add(EventBatteryStatus("电量低于20%"))
            taskActions.add(ActionWebSocketMsg("快没电了！"))
        })
        return list
    }
}