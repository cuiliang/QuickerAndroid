package cuiliang.quicker.client

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import cuiliang.quicker.R
import cuiliang.quicker.client.QuickerServiceHandler.Companion.instant
import cuiliang.quicker.messages.MessageBase
import cuiliang.quicker.messages.recv.UpdateButtonsMessage
import cuiliang.quicker.messages.recv.VolumeStateMessage
import cuiliang.quicker.network.ConnectServiceCallback
import cuiliang.quicker.network.ScanDeviceUtils
import cuiliang.quicker.network.websocket.WebSocketClient.Companion.instance
import java.util.LinkedList
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ClientService : Service(), ConnectServiceCallback {
    private val binder = LocalBinder()
    private val mExecutor = ThreadPoolExecutor(
        1,
        10,
        2,
        TimeUnit.SECONDS,
        ArrayBlockingQueue(1)
    )

    /**
     * 返回ClientManager
     */
    var clientManager: ClientManager? = null
        private set
    private var wifiStatusChangeReceiver: NetworkStatusChangeReceiver? = null
    private val ipItems: MutableList<String> = LinkedList()
    private var ipIndex = 0

    /**
     * 创建Binder对象，返回给客户端activity使用，提供数据交换的接口
     */
    inner class LocalBinder : Binder() {
        val service: ClientService
            /**
             * 返回当前service对象
             */
            get() = this@ClientService
    }

    /**
     * 最后收到消息的记录
     */
    @JvmField
    val messageCache = MessageCache()
    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        createNotification()
        //
        // wifi 监控
        val filter = IntentFilter()
        //filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        //filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        wifiStatusChangeReceiver = NetworkStatusChangeReceiver(netChangeCallback)
        registerReceiver(wifiStatusChangeReceiver, filter)
        instant.addListener(listener)

        // 启动网络连接
        Log.d(
            TAG,
            "连接服务器：" + ClientConfig.getInstance().mServerHost + ":" + ClientConfig.getInstance().mServerPort
        )
        clientManager = ClientManager()
        mExecutor.execute {
            ipItems.clear()
            ipItems.addAll(ScanDeviceUtils.getInstant().scan())
            clientManager!!.connect(1, this@ClientService)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBind")
        return binder
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        instant.removeListener(listener)
        if (wifiStatusChangeReceiver != null) {
            unregisterReceiver(wifiStatusChangeReceiver)
        }
        clientManager!!.shutdown()
        instance().closeRequest()
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun connectCallback(isSuccess: Boolean, obj: Any?) {
        if (isSuccess) {
            Log.i(TAG, "自动连接尝试连接成功")
            ClientConfig.getInstance().saveConfig()
        } else {
            Log.e(TAG, "尝试自动连接失败")
            if (!ipItems.isEmpty() && ipIndex < ipItems.size) {
                val tmp = ipItems[ipIndex++]
                /*
                 * 这里检测IP是否是192.168开头，不是这个开头的ip不进行自动登录。
                 * 这是为了防止用户使用的不是WiFi，而是移动数据而导致连接异常。
                 * 不排除用户路由设置的网关不是192.168开头的。暂时不支持这种IP。
                 * (测试时使用移动数据出现过几十个10开头的IP，这种是不可能连上的，严重影响使用体验)
                 * 后续应该增加一个取消自动连接按钮。因为局域网内有255个设备，那么会连接244次。
                 * 这个时间非常长。当然很难遇到
                 * */if (tmp.startsWith("192.168")) {
                    ClientConfig.getInstance().mServerHost = tmp
                    clientManager!!.connect(1, this)
                } else {
                    connectCallback(false, null)
                }
            } else {
                Log.e(TAG, "自动连接结束，没有扫描到有效ip;ipItems.size:" + ipItems.size)
            }
        }
    }

    private fun createNotification() {
        val notification: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel =
                NotificationChannel(packageName, "Quicker", NotificationManager.IMPORTANCE_HIGH)
            channel.enableLights(true)
            channel.description = "Quicker 连接服务"
            channel.setShowBadge(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            manager.createNotificationChannel(channel)
            notification = NotificationCompat.Builder(this, packageName)
        } else {
            notification = NotificationCompat.Builder(this)
        }
        val notification1 = notification.setContentTitle("Quicker")
            .setContentText("Quicker 连接服务")
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 7.0 设置优先级
            .build()
        startForeground(1, notification1)
    }

    private val netChangeCallback: NetChangeCallback = object : NetChangeCallback {
        override fun onChange(connected: Boolean) {
            Log.w(TAG, "收到wifi连接状态变更：$connected")
            if (connected && clientManager!!.connectionStatus == ConnectionStatus.Disconnected) {
                clientManager!!.connect(3, null)
            }
        }
    }

    private val listener: QuickerServiceListener = object : QuickerServiceListener() {
        override fun onMessage(msg: MessageBase) {
            super.onMessage(msg)
            when (msg) {
                is UpdateButtonsMessage -> messageCache.lastUpdateButtonsMessage = msg
                is VolumeStateMessage -> messageCache.lastVolumeStateMessage = msg
            }
        }
    }

    companion object {
        private val TAG = ClientService::class.java.simpleName
    }
}