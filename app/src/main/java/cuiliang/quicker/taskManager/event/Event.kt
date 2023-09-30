package cuiliang.quicker.taskManager.event

import cuiliang.quicker.taskManager.BaseEventOrAction

/**
 * Created by Silent on 2023/9/18 16:12
 * 事件接口
 */
abstract class Event : BaseEventOrAction {
    /*
    * 事件状态，当该事件达到完成条件时为true
    * 默认false；
    * 举例：
    * 电量状态设置为低于20%触发某个操作，则该值在电量低于20%应该为 true
    * */
    var eventState = false

    var description: String = ""

    var listener: EventRunningListener? = null

    override fun resultStr(): String = description
}