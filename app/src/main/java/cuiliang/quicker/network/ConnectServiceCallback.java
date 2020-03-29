package cuiliang.quicker.network;

import android.support.annotation.Nullable;

/**
 * Created by Void on 2020/3/29 14:43
 * 连接服务回调
 */
public interface ConnectServiceCallback {

    /**
     * 连接pc服务回调
     * @param isSuccess 连接状态
     * @param obj 状态相关信息，可为空！
     */
    void connectCallback(boolean isSuccess,@Nullable Object obj);
}
