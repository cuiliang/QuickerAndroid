package cuiliang.quicker;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cuiliang.quicker.client.ClientConfig;
import cuiliang.quicker.client.ClientService;
import cuiliang.quicker.client.ConnectionStatus;
import cuiliang.quicker.events.ConnectionStatusChangedEvent;
import cuiliang.quicker.util.ShareDataToPCManager;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 连接逻辑：
 * 检查配置文件是否存在网络配置。
 * 没有配置：直接开始配置网络IP、端口等信息流程
 * 有配置：读取最近使用的网络配置缓存，在MainActivity直接连接。
 * 避免已经连接了又返回ConfigActivity界面提示连接
 */
public class ConfigActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private final Handler mHandler=new Handler(Looper.getMainLooper());

    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;


    private final static String TAG = ConfigActivity.class.getSimpleName();
    private final static int REQUESTCODE = 1;// 表示返回的结果码

    private EditText txtIp;

    private EditText txtPort;

    private EditText txtConnectionCode;
    private EditText etWebSocketPort;
    private EditText etWebSocketCode;
    private Button btnConnect;

    private ClientService clientService;
    private ServiceConnection conn;

    private TextView txtConnectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        txtIp = (EditText) findViewById(R.id.txtIp);
        txtPort = (EditText) findViewById(R.id.txtPort);
        txtConnectionCode = (EditText) findViewById(R.id.txtConnectionCode);
        txtConnectionStatus = (TextView) findViewById(R.id.txtConnectionStatus);
        etWebSocketPort = (EditText) findViewById(R.id.et_websocket_port);
        etWebSocketCode = (EditText) findViewById(R.id.et_websocket_code);

        txtIp.setText(ClientConfig.getInstance().mServerHost);
        txtPort.setText(ClientConfig.getInstance().mServerPort);
        txtConnectionCode.setText(ClientConfig.getInstance().ConnectionCode);
        etWebSocketPort.setText(ClientConfig.getInstance().webSocketPort);
        etWebSocketCode.setText(ClientConfig.getInstance().webSocketCode);

        btnConnect = (Button) findViewById(R.id.btnSave);
        final Activity me = this;

        btnConnect.setOnClickListener(v -> {
            //连接按钮被点击后应该设为不可点击，直到连接结果返回取消该状态
            v.setClickable(false);
            v.setEnabled(false);
            mHandler.postDelayed(() -> {
                v.setClickable(true);
                v.setEnabled(true);
            },3000);

            save();
            if (clientService != null) {
                clientService.getClientManager().connect(1, null);
            }
        });

        ImageButton btnQrscan = (ImageButton) findViewById(R.id.btnPc);
        btnQrscan.setOnClickListener(v -> {
            String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
            if (!EasyPermissions.hasPermissions(ConfigActivity.this, perms)) {
                requestCodeQRCodePermissions();
            } else {
                beginScan();
            }
        });


        // region 建立与ClientService的链接
        conn = new ServiceConnection() {
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

    /**
     * 重置用户的分享信息
     */
    public void reSetShareUserData(View view) {
        ShareDataToPCManager.getInstant().clearUserInfo();
        ShareDataToPCManager.getInstant().shareExamine(this, true);
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
        Intent intent = new Intent(ConfigActivity.this, QrcodeScanActivity.class);
        startActivityForResult(intent, REQUESTCODE);
    }

    private void save() {
        ClientConfig.getInstance().mServerPort = txtPort.getText().toString();
        ClientConfig.getInstance().mServerHost = txtIp.getText().toString();
        ClientConfig.getInstance().ConnectionCode = txtConnectionCode.getText().toString();
        ClientConfig.getInstance().webSocketPort = etWebSocketPort.getText().toString();
        ClientConfig.getInstance().webSocketCode = etWebSocketCode.getText().toString();
        ClientConfig.getInstance().saveConfig();
    }


    // 再重写一个onActivityResult方法，作用是将当前Activity中的数据传递到另一个Activity的意图中后，实现跳转，再回传回来。
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUESTCODE) {
            if (resultCode == RESULT_OK) {

                String qrcode = data.getStringExtra("barcode");

                Log.d(TAG, "扫描结果：" + qrcode);
                if (qrcode.startsWith("PB:")) {
                    String[] parts = qrcode.split("\n");
                    txtIp.setText(parts[1]);
                    txtPort.setText(parts[2]);
                    if (parts.length > 3) {
                        txtConnectionCode.setText(parts[3]);
                    }

                }
            } else {
                Log.d(TAG, "扫描失败！" + resultCode);
            }

        }


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

    /**
     * 更新连接状态显示
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ConnectionStatusChangedEvent event) {
        updateConnectionStatus(event.status, event.message);

        if (event.status == ConnectionStatus.LoggedIn) {

            showToast("连接成功！");
            Intent goMainActivity = new Intent(this, MainActivity.class);
            goMainActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(goMainActivity);
        }
    }

    private void showToast(String message) {
        Toast t = Toast.makeText(getApplicationContext(),
                message,
                Toast.LENGTH_SHORT);
        t.show();
    }


    /**
     * 更新连接状态显示
     *
     * @param status
     * @param message 额外的错误消息
     */
    private void updateConnectionStatus(ConnectionStatus status, String message) {
        mHandler.removeCallbacks(null);
        btnConnect.setClickable(status != ConnectionStatus.Connecting);
        btnConnect.setEnabled(status != ConnectionStatus.Connecting);
        String tmp;
        switch (status) {
            case Connected:
                tmp = "已连接";
                break;
            case Disconnected:
                tmp = "未连接";
                break;
            case Connecting:
                tmp = "连接中...";
                break;
            case LoggedIn:
                tmp = "已登录";
                break;
            default:
                tmp = "";
                break;
        }

        if (message != null && !message.isEmpty()) {
            tmp = tmp + "-" + message;
        }
        txtConnectionStatus.setText(tmp);
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
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }

}
