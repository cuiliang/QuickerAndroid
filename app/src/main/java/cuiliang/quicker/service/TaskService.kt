package cuiliang.quicker.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import cuiliang.quicker.client.ConnectionStatus
import cuiliang.quicker.network.websocket.ConnectListener
import cuiliang.quicker.network.websocket.WebSocketClient

/**
 * Created by Voidcom on 2023/9/20 22:51
 * 用于执行任务的服务
 */
class TaskService: Service() , ConnectListener {
//    private val taskList = TaskList()
//    private val  executor:ExecutorService=ExecutorService

    override fun onCreate() {
        super.onCreate()
        WebSocketClient.instance().connectListeners.add(this)



    }

//    override fun onBind(intent: Intent?): IBinder {
//        return null
//    }

    override fun onStatus(status: ConnectionStatus) {
        if (status==ConnectionStatus.LoggedIn||status==ConnectionStatus.Connected){

        }
    }
}