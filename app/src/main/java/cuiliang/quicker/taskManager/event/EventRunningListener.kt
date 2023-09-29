package cuiliang.quicker.taskManager.event

/**
 * Created by Voidcom on 2023/9/29 13:55
 * 一个事件运行监听
 * 当一个事件条件达成时会执行这个接口
 */
interface EventRunningListener {
    fun runningListener()
}