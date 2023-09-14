package cuiliang.quicker.client;

import android.text.TextUtils;

import cuiliang.quicker.util.KLog;
import cuiliang.quicker.util.SPUtils;

// 客户端设置
public class ClientConfig {

    // 服务器主机名
    public String mServerHost = "192.168.1.1";

    // 服务器端口
    public String mServerPort = "666";

    // 设备名称
    public String DeviceName;

    // 连接验证码
    public String ConnectionCode = "quicker";

    //推送用户名，用于推送服务
    public String userName;
    //推送验证码
    public String pushAuthCode;

    //WebSocket 端口
    public String webSocketPort = "668";
    public String webSocketCode = "quicker";

    //true启用wss安全连接
    public boolean enableHttps = true;

    private ClientConfig() {
    }

    private static class ClientConfigHolder {
        public final static ClientConfig INSTANCE = new ClientConfig();
    }

    public static ClientConfig getInstance() {
        return ClientConfigHolder.INSTANCE;
    }

    public boolean hasCache() {
        return SPUtils.contains("mServerHost") && SPUtils.contains("mServerPort") && SPUtils.contains("ConnectionCode");
    }

    public void saveConfig() {
        SPUtils.putString("mServerHost", mServerHost);
        SPUtils.putString("mServerPort", mServerPort);
        SPUtils.putString("ConnectionCode", ConnectionCode);
        SPUtils.putBoolean("enableHttps", enableHttps);
        SPUtils.putString("webSocketPort", webSocketPort);
        SPUtils.putString("webSocketCode", webSocketCode);
    }

    public void readConfig() {
        mServerHost = SPUtils.getString("mServerHost", "192.168.1.1");
        mServerPort = SPUtils.getString("mServerPort", "666");
        ConnectionCode = SPUtils.getString("ConnectionCode", "quicker");
        enableHttps = SPUtils.getBoolean("enableHttps", true);
        webSocketPort = SPUtils.getString("webSocketPort", "668");
        webSocketCode = SPUtils.getString("webSocketCode", "quicker");
    }

    /**
     * 生成合适的WebSocket连接地址
     * wss://192-168-1-1.lan.quicker.cc:668/ws
     */
    public String applyAddress() {
        if (TextUtils.isEmpty(mServerHost)) return "";
        StringBuilder sb;
        if (enableHttps) {
            sb = new StringBuilder("wss://");
            sb.append(mServerHost.replace(".", "-"));
            sb.append(".lan.quicker.cc");
        } else {
            sb = new StringBuilder("ws://");
            sb.append(mServerHost);
        }
        sb.append(":");
        sb.append(webSocketPort);
        sb.append("/ws");
        KLog.d("ClientConfig", "请求连接地址：" + sb);
        return sb.toString();
    }
}
