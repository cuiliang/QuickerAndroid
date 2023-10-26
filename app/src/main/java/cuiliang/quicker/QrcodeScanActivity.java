package cuiliang.quicker;

import android.Manifest;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.cuiliang.quicker.ui.BaseVBActivity;
import com.cuiliang.quicker.ui.EmptyViewModel;

import java.util.List;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cuiliang.quicker.databinding.ActivityQrcodeScanBinding;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

// https://github.com/bingoogolapple/BGAQRCode-Android
public class QrcodeScanActivity extends BaseVBActivity<ActivityQrcodeScanBinding, EmptyViewModel> implements QRCodeView.Delegate, EasyPermissions.PermissionCallbacks {
    private static final String TAG = QrcodeScanActivity.class.getSimpleName();
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;

    @NonNull
    @Override
    protected EmptyViewModel getMViewModel() {
        return new ViewModelProvider(this).get(EmptyViewModel.class);
    }

    @Override
    public void onInit() {
        getMBinding().zbarview.setDelegate(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestCodeQRCodePermissions();
        getMBinding().zbarview.startSpot();
    }

    @Override
    protected void onStop() {
        getMBinding().zbarview.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        getMBinding().zbarview.onDestroy();
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator == null) return;
        vibrator.vibrate(200);
    }


    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.i(TAG, "result:" + result);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        vibrate();
        // getMBinding().zbarview.startSpot();

        Intent intent = new Intent();// 重新声明一个意图。
        intent.putExtra("barcode", result); // 将three回传到意图中。
        // 通过Intent对象返回结果，调用setResult方法。
        setResult(RESULT_OK, intent);
        finish();// 结束当前Activity的生命周期。
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }else {
            startScan();
        }
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "权限授权!");
        startScan();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "权限被拒绝!");
        finish();
    }

    private void startScan() {
        getMBinding().zbarview.startCamera();
        getMBinding().zbarview.showScanRect();
    }
}
