package cuiliang.quicker.client

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Parcelable
import android.util.Log

@Suppress("DEPRECATION")
class NetworkStatusChangeReceiver(private val callback: NetChangeCallback) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // ref:https://blog.csdn.net/qq_20785431/article/details/51520459
        // 监听wifi的连接状态即是否连上了一个有效无线路由
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION != intent.action) return
        val parcelableExtra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO, NetworkInfo::class.java)
        } else {
            intent.getParcelableExtra<Parcelable>(WifiManager.EXTRA_NETWORK_INFO)
        }
        val isConnected = parcelableExtra?.let {
            //判断网络是否已经连接
            (it as NetworkInfo).state == NetworkInfo.State.CONNECTED
        } ?: false
        Log.e("NetworkStatusChangeReceiver", "wifi连接:$isConnected")
        callback.onChange(isConnected)
    }
}

interface NetChangeCallback {
    fun onChange(connected: Boolean)
}