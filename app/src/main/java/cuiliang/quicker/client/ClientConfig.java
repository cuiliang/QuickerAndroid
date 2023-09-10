package cuiliang.quicker.client;

import cuiliang.quicker.util.SPUtils;

// 客户端设置
public class ClientConfig {

    // 服务器主机名
    public String mServerHost;

    // 服务器端口
    public String mServerPort;

    // 设备名称
    public String DeviceName;

    // 连接码
    public String ConnectionCode;
    public String hostName = "pc_ip";
    public String portName = "pc_port";
    public String codeName = "connection_code";

    private ClientConfig() {
    }

    private static class ClientConfigHolder {
        public final static ClientConfig INSTANCE = new ClientConfig();
    }

    public static ClientConfig getInstance() {
        return ClientConfigHolder.INSTANCE;
    }

    public boolean hasCache() {
        return SPUtils.contains(hostName) && SPUtils.contains(portName) && SPUtils.contains(codeName);
    }

    public void saveConfig() {
        SPUtils.putString("pc_ip", mServerHost);
        SPUtils.putString("pc_port", mServerPort);
        SPUtils.putString("connection_code", ConnectionCode);
    }

    public void readConfig() {
        mServerHost = SPUtils.getString("pc_ip", "192.168.1.1");
        mServerPort = SPUtils.getString("pc_port", "666");
        ConnectionCode = SPUtils.getString("connection_code", "quicker");
    }
}
