package cuiliang.quicker.client;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import cuiliang.quicker.events.ServerMessageEvent;
import cuiliang.quicker.events.WifiStatusChangeEvent;
import cuiliang.quicker.messages.MessageBase;
import cuiliang.quicker.messages.recv.UpdateButtonsMessage;
import cuiliang.quicker.messages.recv.VolumeStateMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ClientService extends Service {

    private static final String TAG = ClientService.class.getSimpleName();

    private LocalBinder binder = new LocalBinder();

    private ClientManager clientManager;

    private NetworkStatusChangeReceiver wifiStatusChangeReceiver;

    /**
     * 创建Binder对象，返回给客户端activity使用，提供数据交换的接口
     */
    public class LocalBinder extends Binder {

        /**
         * 返回当前service对象
         * @return
         */
        public ClientService getService() {
            return ClientService.this;
        }


    }

    /**
     * 最后收到消息的记录
     */
    private MessageCache messageCache = new MessageCache();

    public MessageCache getMessageCache() {
        return messageCache;
    }

    public ClientService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        //
        // wifi 监控
        IntentFilter filter = new IntentFilter();
        //filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        wifiStatusChangeReceiver = new NetworkStatusChangeReceiver();
        registerReceiver(wifiStatusChangeReceiver, filter);

        //
        EventBus.getDefault().register(this);


        //
        // 启动网络连接

        ClientConfig config = new ClientConfig();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        config.mServerHost = preferences.getString("pc_ip", "192.168.1.148");
        config.mServerPort = Integer.parseInt(preferences.getString("pc_port", "666"));
        config.ConnectionCode = preferences.getString("connection_code", "quicker");

        Log.d(TAG, "连接服务器：" + config.mServerHost + " : " + String.valueOf(config.mServerPort));
        clientManager = new ClientManager(config);
        clientManager.connect(1);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        EventBus.getDefault().unregister(this);

        if (wifiStatusChangeReceiver != null){
            unregisterReceiver(wifiStatusChangeReceiver);
        }


        clientManager.shutdown();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 返回ClientManager
     * @return
     */
    public ClientManager getClientManager() {
        return this.clientManager;
    }


    /**
     * wifi链接状态改变了, 尝试自动连接
     * @param event
     */
    @Subscribe(threadMode =  ThreadMode.MAIN)
    public void onEventMainThread(WifiStatusChangeEvent event) {
        Log.w(TAG, "收到wifi连接状态变更：" + event.isConnected);
        if (event.isConnected && getClientManager().getConnectionStatus() == ConnectionStatus.Disconnected){
            clientManager.connect(3);
        }
    }


    /**
     * 处理收到的pc消息
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ServerMessageEvent event){

        MessageBase originMessage = event.serverMessage;
        if (originMessage instanceof UpdateButtonsMessage) {
            messageCache.lastUpdateButtonsMessage = (UpdateButtonsMessage) originMessage;
        } else if (originMessage instanceof VolumeStateMessage) {
            messageCache.lastVolumeStateMessage = (VolumeStateMessage)originMessage;
        }
    }
}
