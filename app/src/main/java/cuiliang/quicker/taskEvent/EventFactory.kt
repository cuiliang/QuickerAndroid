package cuiliang.quicker.taskEvent

import cuiliang.quicker.taskEvent.EventType.*

/**
 * Created by voidcom on ${DATE} ${TIME}
 * @see EventType
 *
 */
class EventFactory {

    fun getEvent(key: EventType): Event {
        return when (key) {
            EVENT_WEBSOCKET_MESSAGE -> WebSocketEvent()
            EVENT_BATTERY_STATUS -> BatteryStatusEvent()
        }
    }

    /**
     * 获取所有的事件
     */
    fun getEvents(): List<Event> {
        return arrayListOf<Event>().apply {
            EventType.values().forEach {
                if (it.startWith("EVENT_"))
                    this.add(getEvent(it))
            }
        }
    }
}