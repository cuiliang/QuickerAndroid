package cuiliang.quicker.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import cuiliang.quicker.network.websocket.MsgRequestData
import cuiliang.quicker.network.websocket.ServiceRequestFactory
import cuiliang.quicker.network.websocket.WebSocketClient
import cuiliang.quicker.ui.taskManager.TaskList
import cuiliang.quicker.util.KLog
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by Voidcom on 2023/9/20 22:51
 * 用于执行任务的服务
 */
class TaskManagerService : Service() {
    private lateinit var taskList: TaskList
    private val mBinder: TaskManagerBinder by lazy { TaskManagerBinder() }

    override fun onCreate() {
        super.onCreate()
        taskList = TaskList(applicationContext)
        taskList.readFileConfig()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        WebSocketClient.instance().readyExecuteAction = executeAction
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        taskList.clear()

        WebSocketClient.instance().readyExecuteAction = null
    }

    override fun onBind(intent: Intent?): IBinder {
        intent?.let {
            KLog.d(TAG, "onBind: " + it.component?.className)
        }
        return mBinder
    }

    private val executeAction = object : (MsgRequestData) -> Unit {
        override fun invoke(p1: MsgRequestData) {
            ServiceRequestFactory.decodeRequest(applicationContext, p1)
        }
    }

    inner class TaskManagerBinder : Binder() {
        fun getTaskList(): TaskList = taskList
    }

    companion object {
        const val TAG = "TaskManagerService"
        val threadPool: ExecutorService = Executors.newScheduledThreadPool(2)
//        val aa
    }
}