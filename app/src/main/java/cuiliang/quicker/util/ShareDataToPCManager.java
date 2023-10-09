package cuiliang.quicker.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.kongzue.dialogx.dialogs.InputDialog;

import cuiliang.quicker.R;
import cuiliang.quicker.network.NetRequestObj;
import cuiliang.quicker.network.NetWorkManager;
import cuiliang.quicker.network.shareToPc.ShareApi;

/**
 * Created by Void on 2020/4/12 12:12
 * 共享数据管理
 * 文档：https://www.getquicker.net/KC/Help/Doc/connection
 * 用于把Android端数据发送到服务器，服务器转发给pc端
 */
public class ShareDataToPCManager {
    private static String TAG = ShareDataToPCManager.class.getSimpleName();
    private static ShareDataToPCManager shareManager;
    //用户名，用于分享操作
    private String userName;
    //用户推送验证码，用于分享操作
    private String userCode;

    public static ShareDataToPCManager getInstant() {
        if (shareManager == null) shareManager = new ShareDataToPCManager();
        return shareManager;
    }

    private ShareDataToPCManager() {
        initUserInfo();
    }

    /**
     * 是否有用户信息，这是分享操作成功的必要条件
     *
     * @return true有
     */
    public boolean isHaveUserInfo() {
        return !TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userCode);
    }

    public void initUserInfo() {
        userName = SPUtils.getString("ShareDataToPC.userName", null);
        userCode = SPUtils.getString("ShareDataToPC.userCode", null);
    }

    public void clearUserInfo() {
        SPUtils.putString("ShareDataToPC.userName", null);
        SPUtils.putString("ShareDataToPC.userCode", null);
        userName = null;
        userCode = null;
    }

    /**
     * 分享文字
     */
    public void shareTextToPc(String data, NetWorkManager.RequestCallback callback) {
        shareDataToPc("copy", data, callback);
    }

    public void shareTextToPc_(String data, NetWorkManager.RequestCallback callback) {
        shareDataToPc("paste", data, callback);
    }

    /**
     * 执行动作
     *
     * @param data 动作名或动作ID。注：使用动作名必须保证没有重复的动作名，推荐使用ID
     */
    public void shareActionToPc(String data, NetWorkManager.RequestCallback callback) {
        shareDataToPc("action", data, callback);
    }


    /**
     * 分享内容到PC
     * 注：该请求为异步操作
     *
     * @param operation 分享类型
     *                  copy  将内容复制到剪贴板
     *                  paste  将内容粘贴到当前窗口
     *                  action  执行动作
     * @param data      分享的数据
     *                  详情请查看文档(https://www.getquicker.net/KC/Help/Doc/connection)
     * @param callback  结果回调，可为空
     */
    public void shareDataToPc(String operation, String data,
                              NetWorkManager.RequestCallback callback) {
        if (!isHaveUserInfo()) {
            Log.e(TAG, "用户名或推送验证码为空！userName:" + userName + ";userCode:" + userCode);
            return;
        }
        NetRequestObj requestObj = new NetRequestObj(ShareApi.shareUrl, callback);
        requestObj.addBody("toUser", userName);
        requestObj.addBody("code", userCode);
        requestObj.addBody("operation", operation);
        requestObj.addBody("data", data);
        requestObj.setEncode(true);
        NetWorkManager.Companion.getInstant().executeRequest1(requestObj);
    }

    /**
     * 分享操作检查，
     * 主要是判断有没有分享必要的用户信息，没有要求用户提供
     *
     * @return true通过检查
     */
    public boolean shareExamine(Context context, boolean isUser) {
        if (!ShareDataToPCManager.getInstant().isHaveUserInfo()) {
            //没有用户的信息，要求用户输入
            String content;
            if (isUser) {
                content = context.getString(R.string.shareInputHint) + context.getString(R.string.inputUserName);
            } else {
                content = context.getString(R.string.inputPushCode);
            }
            InputDialog.build()
                    .setTitle(R.string.shareInputTitle)
                    .setMessage(content)
                    .setCancelable(true)
                    .setOkButton(R.string.btnAccept)
                    .setCancelButton(R.string.btnCancel)
                    .setOkButton((dialog, v, inputStr) -> {
                        if (TextUtils.isEmpty(inputStr)) {
                            ToastUtils.showShort(context, "操作无效，你输入了空内容！");
                        } else {
                            if (isUser) {
                                SPUtils.putString("ShareDataToPC.userName", inputStr);
                            } else {
                                SPUtils.putString("ShareDataToPC.userCode", inputStr);
                                ShareDataToPCManager.getInstant().initUserInfo();
                            }
                            shareExamine(context, !isUser);
                        }
                        dialog.dismiss();
                        return false;
                    })
                    .show();
            return false;
        } else {
            return true;
        }
    }
}
