package cuiliang.quicker.taskManager

import cuiliang.quicker.taskManager.TaskEventType.*
import cuiliang.quicker.taskManager.TaskActionType.*

/**
 * Created by voidcom on ${DATE} ${TIME}
 * @see TaskEventType
 *
 */
class TaskDataFactory {

    fun getEvent(key: TaskEventType): Event {
        return when (key) {
            EVENT_WEBSOCKET_CONNECTING -> EventWebSocket()
            EVENT_BATTERY_STATUS -> EventBatteryStatus()
        }
    }

    fun getAction(key: TaskActionType): Action {
        return when (key) {
            ACTION_WEBSOCKET_MESSAGE -> ActionWebSocketMsg()
        }
    }

    /**
     * 获取所有的事件
     */
    fun getEvents(): List<Event> {
        return arrayListOf<Event>().apply {
            TaskEventType.values().forEach {
                this.add(getEvent(it))
            }
        }
    }
    fun getActions(): List<Action> {
        return arrayListOf<Action>().apply {
            TaskActionType.values().forEach {
                this.add(getAction(it))
            }
        }
    }
}