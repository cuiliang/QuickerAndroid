package cuiliang.quicker.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
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
//        WebSocketClient.instance().connectListeners.add(this)
        taskList = TaskList(applicationContext)
        taskList.readFileConfig()
    }

    override fun onDestroy() {
        super.onDestroy()
        taskList.clear()
    }

    override fun onBind(intent: Intent?): IBinder {
        intent?.let {
            KLog.d(TAG, "onBind: " + it.component.className)
        }
        return mBinder
    }

    inner class TaskManagerBinder : Binder() {
        fun getTaskList(): TaskList = taskList
    }

    companion object {
        const val TAG = "TaskManagerService"
        val threadPool: ExecutorService = Executors.newScheduledThreadPool(2)
    }
}