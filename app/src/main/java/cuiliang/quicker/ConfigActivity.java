package cuiliang.quicker;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.cuiliang.quicker.ui.BaseVBActivity;
import com.cuiliang.quicker.ui.EmptyViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cuiliang.quicker.client.ClientConfig;
import cuiliang.quicker.client.ClientService;
import cuiliang.quicker.client.ConnectionStatus;
import cuiliang.quicker.databinding.ActivityConfigBinding;
import cuiliang.quicker.events.ConnectionStatusChangedEvent;
import cuiliang.quicker.util.ToastUtils;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 连接逻辑：
 * 检查配置文件是否存在网络配置。
 * 没有配置：直接开始配置网络IP、端口等信息流程
 * 有配置：读取最近使用的网络配置缓存，在MainActivity直接连接。
 * 避免已经连接了又返回ConfigActivity界面提示连接
 */
public class ConfigActivity extends BaseVBActivity<ActivityConfigBinding, EmptyViewModel> implements EasyPermissions.PermissionCallbacks, ActivityResultCallback<ActivityResult>, View.OnClickListener {
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;


    private final static String TAG = ConfigActivity.class.getSimpleName();

    private ClientService clientService;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

    @NonNull
    @Override
    protected EmptyViewModel getMViewModel() {
        return new EmptyViewModel();
    }

    @Override
    public void onInit() {
        getMBinding().txtIp.setText(ClientConfig.getInstance().mServerHost);
        getMBinding().txtPort.setText(ClientConfig.getInstance().mServerPort);
        getMBinding().txtConnectionCode.setText(ClientConfig.getInstance().ConnectionCode);
        getMBinding().etWebsocketPort.setText(ClientConfig.getInstance().webSocketPort);
        getMBinding().etWebsocketCode.setText(ClientConfig.getInstance().webSocketCode);
        getMBinding().btnSave.setOnClickListener(this);
        getMBinding().btnPc.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //自 ADT 版本 14 以来，资源 ID 不是库项目中的常量，因此在 switch 语句中使用它们会报错
        if (v.getId() == R.id.btnSave) {
            //连接按钮被点击后应该设为不可点击，直到连接结果返回取消该状态
            v.setClickable(false);
            v.setEnabled(false);
            getMHandler().postDelayed(() -> {
                v.setClickable(true);
                v.setEnabled(true);
            }, 3000);
            save();
            if (clientService != null) {
                clientService.getClientManager().connect(1, null);
            }
        } else if (v.getId() == R.id.btnPc) {

            String[] perms;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                perms = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.POST_NOTIFICATIONS};
            else
                perms = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
            if (!EasyPermissions.hasPermissions(ConfigActivity.this, perms)) {
                requestCodeQRCodePermissions();
            } else {
                beginScan();
            }
        }
    }

    /**
     * 重置用户的分享信息
     */
    public void reSetShareUserData(View view) {
    }

    public void sharePushCodeHelp(View view) {
        Uri content_url = Uri.parse("https://www.getquicker.net/KC/Help/Doc/connection");
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(content_url);
        startActivity(intent);
    }

    // 开始扫描二维码
    private void beginScan() {
        launcher.launch(new Intent(this, QrcodeScanActivity.class));
    }

    private void save() {
        ClientConfig.getInstance().mServerPort = getMBinding().txtPort.getText().toString();
        ClientConfig.getInstance().mServerHost = getMBinding().txtIp.getText().toString();
        ClientConfig.getInstance().ConnectionCode = getMBinding().txtConnectionCode.getText().toString();
        ClientConfig.getInstance().webSocketPort = getMBinding().etWebsocketPort.getText().toString();
        ClientConfig.getInstance().webSocketCode = getMBinding().etWebsocketCode.getText().toString();
        ClientConfig.getInstance().saveConfig();
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        // 绑定到后台服务
        Intent clientServiceIntent = new Intent(this, ClientService.class);
        bindService(clientServiceIntent, conn, Service.BIND_AUTO_CREATE);

        // 请求二维码权限
        requestCodeQRCodePermissions();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (clientService != null) {
            clientService = null;
            unbindService(conn);
        }
    }

    @Override
    public void onActivityResult(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
            Log.d(TAG, "扫描失败！");
            return;
        }
        String qrcode = result.getData().getStringExtra("barcode");
        Log.d(TAG, "扫描结果：" + qrcode);
        if (qrcode.startsWith("PB:")) {
            String[] parts = qrcode.split("\n");
            getMBinding().txtIp.setText(parts[1]);
            getMBinding().txtPort.setText(parts[2]);
            if (parts.length > 3) {
                getMBinding().txtConnectionCode.setText(parts[3]);
            }
        }
    }

    /**
     * 更新连接状态显示
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ConnectionStatusChangedEvent event) {
        updateConnectionStatus(event.status, event.message);

        if (event.status == ConnectionStatus.LoggedIn) {
            ToastUtils.showShort(this, "连接成功！");
            Intent goMainActivity = new Intent(this, MainActivity.class);
            goMainActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(goMainActivity);
        }
    }

    /**
     * 更新连接状态显示
     *
     * @param status
     * @param message 额外的错误消息
     */
    private void updateConnectionStatus(ConnectionStatus status, String message) {
        getMHandler().removeCallbacks(null);
        getMBinding().btnSave.setClickable(status != ConnectionStatus.Connecting);
        getMBinding().btnSave.setEnabled(status != ConnectionStatus.Connecting);
        String tmp = switch (status) {
            case Connected -> "已连接";
            case Disconnected -> "未连接";
            case Connecting -> "连接中...";
            case LoggedIn -> "已登录";
            default -> "";
        };

        if (message != null && !message.isEmpty()) {
            tmp = tmp + "-" + message;
        }
        getMBinding().txtConnectionStatus.setText(tmp);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
            perms = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.POST_NOTIFICATIONS};
        else
            perms = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }

    private final ServiceConnection conn = new ServiceConnection() {
        /**
         * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
         * 通过这个IBinder对象，实现宿主和Service的交互。
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "绑定成功调用：onServiceConnected");
            ClientService.LocalBinder binder = (ClientService.LocalBinder) service;
            clientService = binder.getService();

            if (clientService.getClientManager() != null) {
                updateConnectionStatus(clientService.getClientManager().getConnectionStatus(), "");

//                    if (clientService.getClientManager().isConnected() && isAutoReturn){
//                        NavUtils.navigateUpFromSameTask(ConfigActivity.this);
//                    }
            }
        }

        /**
         * 当取消绑定的时候被回调。但正常情况下是不被调用的，它的调用时机是当Service服务被意外销毁时，
         * 例如内存的资源不足时这个方法才被自动调用。
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            clientService = null;
        }
    };
}
