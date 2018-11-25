package cuiliang.quicker;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cuiliang.quicker.client.ClientConfig;
import cuiliang.quicker.client.ClientService;
import cuiliang.quicker.client.ConnectionStatus;
import cuiliang.quicker.events.ConnectionStatusChangedEvent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ConfigActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;


    private final static String TAG = ConfigActivity.class.getSimpleName();
    private final static int REQUESTCODE = 1;// 表示返回的结果码

    private EditText txtIp;

    private EditText txtPort;

    private EditText txtConnectionCode;

    private ClientService clientService;
    private ServiceConnection conn;

    private TextView txtConnectionStatus;

    private boolean isAutoReturn = false;

    private boolean isGoogleServiceOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        isAutoReturn = intent.getBooleanExtra("autoReturn", false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        txtIp = (EditText) findViewById(R.id.txtIp);
        txtPort = (EditText) findViewById(R.id.txtPort);
        txtConnectionCode = (EditText) findViewById(R.id.txtConnectionCode);
        txtConnectionStatus = (TextView) findViewById(R.id.txtConnectionStatus);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        txtIp.setText(preferences.getString("pc_ip", "192.168.1.148"));
        txtPort.setText(preferences.getString("pc_port", "666"));
        txtConnectionCode.setText(preferences.getString("connection_code", "quicker"));

        Button btnConnect = (Button) findViewById(R.id.btnSave);
        final Activity me = this;

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();


                if (clientService != null) {
                    clientService.getClientManager().connect(1);
                }
            }
        });

        ImageButton btnQrscan = (ImageButton) findViewById(R.id.btnPc);
        btnQrscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
                if (!EasyPermissions.hasPermissions(ConfigActivity.this, perms)) {
                    EasyPermissions.requestPermissions(ConfigActivity.this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
                } else {
                    beginScan();
                }


//                // 创建意图
//                Intent intent = new Intent(ConfigActivity.this, QrcodeScanActivity.class);
//                startActivityForResult(intent, REQUESTCODE);// 表示可以返回结果


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

        // 隐藏向左的箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //
        isGoogleServiceOk = ConnectionResult.SUCCESS == GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        Log.e(TAG, "GOOGLE 服务可用性：" + isGoogleServiceOk);

    }

    // 开始扫描二维码
    private void beginScan(){

        if (isGoogleServiceOk){
            Intent intent = new Intent(ConfigActivity.this, ScanBarcodeActivity.class);
            startActivityForResult(intent, REQUESTCODE);
        }else{
            Intent intent = new Intent(ConfigActivity.this, QrcodeScanActivity.class);
            startActivityForResult(intent, REQUESTCODE);// 表示可以返回结果
        }
    }

    private void save() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("pc_ip", txtIp.getText().toString());
        editor.putString("pc_port", txtPort.getText().toString());
        editor.putString("connection_code", txtConnectionCode.getText().toString());
        editor.commit();

        ClientConfig config = new ClientConfig();
        config.mServerPort = Integer.parseInt(txtPort.getText().toString());
        config.mServerHost = txtIp.getText().toString();
        config.ConnectionCode = txtConnectionCode.getText().toString();

        clientService.getClientManager().setConfig(config);
    }


    // 再重写一个onActivityResult方法，作用是将当前Activity中的数据传递到另一个Activity的意图中后，实现跳转，再回传回来。
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUESTCODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {

                String qrcode = data.getStringExtra("barcode");

                Log.d(TAG, "扫描结果：" + qrcode);
                if (qrcode.startsWith("PB:")) {
                    String[] parts = qrcode.split("\n");
                    txtIp.setText(parts[1]);
                    txtPort.setText(parts[2]);
                    if (parts.length > 3){
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

            NavUtils.navigateUpFromSameTask(this);
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
        switch (status) {
            case Connected:
                txtConnectionStatus.setText("已连接");
                break;
            case Disconnected:
                txtConnectionStatus.setText("未连接");
                break;
            case Connecting:
                txtConnectionStatus.setText("连接中...");
                break;
            case LoggedIn:
                txtConnectionStatus.setText("已登录");
                break;
        }

        if (message != null && !message.isEmpty()) {
            txtConnectionStatus.setText(txtConnectionStatus.getText() + "-" + message);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
