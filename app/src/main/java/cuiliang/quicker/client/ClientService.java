package cuiliang.quicker.client;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cuiliang.quicker.events.ServerMessageEvent;
import cuiliang.quicker.events.WifiStatusChangeEvent;
import cuiliang.quicker.messages.MessageBase;
import cuiliang.quicker.messages.recv.UpdateButtonsMessage;
import cuiliang.quicker.messages.recv.VolumeStateMessage;
import cuiliang.quicker.network.ConnectServiceCallback;
import cuiliang.quicker.network.ScanDeviceUtils;
import cuiliang.quicker.network.websocket.WebSocketClient;

public class ClientService extends Service implements ConnectServiceCallback {

    private static final String TAG = ClientService.class.getSimpleName();

    private LocalBinder binder = new LocalBinder();
    private ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(
            1,
            10,
            2,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(1)
    );
    private ClientManager clientManager;

    private NetworkStatusChangeReceiver wifiStatusChangeReceiver;

    private List<String> ipItems = new LinkedList<>();
    private int ipIndex = 0;

    /**
     * 创建Binder对象，返回给客户端activity使用，提供数据交换的接口
     */
    public class LocalBinder extends Binder {

        /**
         * 返回当前service对象
         *
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

        // 启动网络连接
        Log.d(TAG, "连接服务器：" + ClientConfig.getInstance().mServerHost + ":" + ClientConfig.getInstance().mServerPort);
        clientManager = new ClientManager();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ipItems.clear();
                ipItems.addAll(ScanDeviceUtils.getInstant().scan());
                clientManager.connect(1, ClientService.this);
            }
        });
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

        if (wifiStatusChangeReceiver != null) {
            unregisterReceiver(wifiStatusChangeReceiver);
        }


        clientManager.shutdown();
        WebSocketClient.Companion.instance().closeRequest();
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
     *
     * @return
     */
    public ClientManager getClientManager() {
        return this.clientManager;
    }


    /**
     * wifi链接状态改变了, 尝试自动连接
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(WifiStatusChangeEvent event) {
        Log.w(TAG, "收到wifi连接状态变更：" + event.isConnected);
        if (event.isConnected && getClientManager().getConnectionStatus() == ConnectionStatus.Disconnected) {
            clientManager.connect(3, null);
        }
    }


    /**
     * 处理收到的pc消息
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ServerMessageEvent event) {

        MessageBase originMessage = event.serverMessage;
        if (originMessage instanceof UpdateButtonsMessage) {
            messageCache.lastUpdateButtonsMessage = (UpdateButtonsMessage) originMessage;
        } else if (originMessage instanceof VolumeStateMessage) {
            messageCache.lastVolumeStateMessage = (VolumeStateMessage) originMessage;
        }
    }

    @Override
    public void connectCallback(boolean isSuccess, @Nullable Object obj) {
        if (isSuccess) {
            Log.i(TAG, "自动连接尝试连接成功");
            ClientConfig.getInstance().saveConfig();
        } else {
            Log.e(TAG, "尝试自动连接失败");
            if (!ipItems.isEmpty() && ipIndex < ipItems.size()) {
                String tmp = ipItems.get(ipIndex++);
                /*
                 * 这里检测IP是否是192.168开头，不是这个开头的ip不进行自动登录。
                 * 这是为了防止用户使用的不是WiFi，而是移动数据而导致连接异常。
                 * 不排除用户路由设置的网关不是192.168开头的。暂时不支持这种IP。
                 * (测试时使用移动数据出现过几十个10开头的IP，这种是不可能连上的，严重影响使用体验)
                 * 后续应该增加一个取消自动连接按钮。因为局域网内有255个设备，那么会连接244次。
                 * 这个时间非常长。当然很难遇到
                 * */
                if (tmp.startsWith("192.168")) {
                    ClientConfig.getInstance().mServerHost = tmp;
                    clientManager.connect(1, this);
                } else {
                    connectCallback(false, null);
                }
            } else {
                Log.e(TAG, "自动连接结束，没有扫描到有效ip;ipItems.size:" + ipItems.size());
            }
        }
    }
}
