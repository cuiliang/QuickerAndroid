package cuiliang.quicker.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

import cuiliang.quicker.events.WifiStatusChangeEvent;

import org.greenrobot.eventbus.EventBus;

public class NetworkStatusChangeReceiver extends BroadcastReceiver {
    private final String TAG = NetworkStatusChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        // ref:https://blog.csdn.net/qq_20785431/article/details/51520459
        // 监听wifi的连接状态即是否连上了一个有效无线路由
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                // 获取联网状态的NetWorkInfo对象
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                //获取的State对象则代表着连接成功与否等状态
                NetworkInfo.State state = networkInfo.getState();
                //判断网络是否已经连接
                boolean isConnected = state == NetworkInfo.State.CONNECTED;
                Log.e("TAG", "wifi连接:" + isConnected);
                EventBus.getDefault().post(new WifiStatusChangeEvent(isConnected));
            }
        }


//        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
//            ConnectivityManager manager = (ConnectivityManager) context
//                    .getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
//
//
//            if (activeNetwork != null) { // connected to the internet
//                if (activeNetwork.isConnected()) {
//                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
//                        Log.d(TAG,"当前WiFi连接可用 ");
//                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
//                        Log.d(TAG,"当前移动网络连接可用 ");
//                    }
//                    //发送广播通知服务重新启动
//                    Bus.post(new ConnectClosedEvent(Contants.CONNECT_CLOSE_TYPE));
//                } else {
//                    LogUtil.w("当前没有网络连接，请确保你已经打开网络 ");
//                    //无网络连接,通知socket,取消每三秒重连接
//                    Bus.post(new ConnectCloseAllEvent(true));
//                }
//            } else {   // not connected to the internet
//                LogUtil.w("当前没有网络连接，请确保你已经打开网络 ");
//                //无网络连接,通知socket,取消每三秒重连接
//                Bus.post(new ConnectCloseAllEvent(true));
//            }
//        }
    }
}
