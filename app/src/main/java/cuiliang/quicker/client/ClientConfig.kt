package cuiliang.quicker.client

import android.text.TextUtils
import cuiliang.quicker.util.KLog.d
import cuiliang.quicker.util.SPUtils

// 客户端设置
class ClientConfig private constructor() {
    // 服务器主机名
    var mServerHost = "192.168.1.1"

    // 服务器端口
    var mServerPort = "666"

    // 设备名称
    var DeviceName: String? = null

    // 连接验证码
    var ConnectionCode = "quicker"

    //推送用户名，用于推送服务
    var userName: String? = null

    //推送验证码
    var pushAuthCode: String? = null

    //WebSocket 端口
    var webSocketPort = "668"
    var webSocketCode = "quicker"

    //true启用wss安全连接
    var enableHttps = true

    fun hasCache(): Boolean {
        return SPUtils.contains("mServerHost") && SPUtils.contains("mServerPort") && SPUtils.contains(
            "ConnectionCode"
        )
    }

    fun saveConfig() {
        SPUtils.putString("mServerHost", mServerHost)
        SPUtils.putString("mServerPort", mServerPort)
        SPUtils.putString("ConnectionCode", ConnectionCode)
        SPUtils.putBoolean("enableHttps", enableHttps)
        SPUtils.putString("webSocketPort", webSocketPort)
        SPUtils.putString("webSocketCode", webSocketCode)
    }

    fun readConfig() {
        mServerHost = SPUtils.getString("mServerHost", "192.168.1.1")
        mServerPort = SPUtils.getString("mServerPort", "666")
        ConnectionCode = SPUtils.getString("ConnectionCode", "quicker")
        enableHttps = SPUtils.getBoolean("enableHttps", true)
        webSocketPort = SPUtils.getString("webSocketPort", "668")
        webSocketCode = SPUtils.getString("webSocketCode", "quicker")
    }

    /**
     * 生成合适的WebSocket连接地址
     * wss://192-168-1-1.lan.quicker.cc:668/ws
     */
    fun applyAddress(): String {
        if (TextUtils.isEmpty(mServerHost)) return ""
        val sb: StringBuilder
        if (enableHttps) {
            sb = StringBuilder("wss://")
            sb.append(mServerHost.replace(".", "-"))
            sb.append(".lan.quicker.cc")
        } else {
            sb = StringBuilder("ws://")
            sb.append(mServerHost)
        }
        sb.append(":")
        sb.append(webSocketPort)
        sb.append("/ws")
        d("ClientConfig", "请求连接地址：$sb")
        return sb.toString()
    }

    companion object {
        val instance: ClientConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { ClientConfig() }
    }
}