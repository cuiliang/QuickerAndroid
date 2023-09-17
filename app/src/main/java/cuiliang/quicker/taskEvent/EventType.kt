package cuiliang.quicker.taskEvent

/**
 * Created by Silent on 2023/9/18 16:49
 *
 *
 */
enum class EventType(private var v: String) {
    EVENT_WEBSOCKET_MESSAGE("EVENT_WEBSOCKET_MESSAGE"),
    EVENT_BATTERY_STATUS("EVENT_BATTERY_STATUS");

    override fun toString(): String = v

    fun startWith(str: String) = v.startsWith(str)
}