package cuiliang.quicker.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.cuiliang.quicker.ui.BaseVBActivity
import com.cuiliang.quicker.ui.EmptyViewModel
import cuiliang.quicker.MainActivity
import cuiliang.quicker.QrcodeScanActivity
import cuiliang.quicker.client.ClientConfig.Companion.instance
import cuiliang.quicker.client.ClientManager
import cuiliang.quicker.client.ClientManager.QuickerConnectListener
import cuiliang.quicker.client.ClientService
import cuiliang.quicker.client.ClientService.LocalBinder
import cuiliang.quicker.client.ConnectionStatus
import cuiliang.quicker.databinding.ActivityConfigBinding
import cuiliang.quicker.util.ToastUtils
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks

/**
 * 连接逻辑：
 * 检查配置文件是否存在网络配置。
 * 没有配置：直接开始配置网络IP、端口等信息流程
 * 有配置：读取最近使用的网络配置缓存，在MainActivity直接连接。
 * 避免已经连接了又返回ConfigActivity界面提示连接
 */
class ConfigActivity : BaseVBActivity<ActivityConfigBinding, EmptyViewModel>(),
        PermissionCallbacks, ActivityResultCallback<ActivityResult> {
    private var mBinder: LocalBinder? = null
    private val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            this
    )
    private lateinit var permissions: Array<String>

    override val mViewModel: EmptyViewModel by lazy { ViewModelProvider(this)[EmptyViewModel::class.java] }

    override fun onInit() {
        mBinding.txtIp.setText(instance.mServerHost)
        mBinding.txtPort.setText(instance.mServerPort)
        mBinding.txtConnectionCode.setText(instance.ConnectionCode)
        mBinding.etWebsocketPort.setText(instance.webSocketPort)
        mBinding.etWebsocketCode.setText(instance.webSocketCode)
        mBinding.btnPc.setOnClickListener { beginScan() }
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        mBinding.btnSave.setOnClickListener {
            //连接按钮被点击后应该设为不可点击，直到连接结果返回取消该状态
            it.isClickable = false
            it.isEnabled = false
            mHandler.postDelayed({
                it.isClickable = true
                it.isEnabled = true
            }, 3000)
            save()
            ClientManager.getInstance().connect(1, null)
        }
    }

    override fun onStart() {
        super.onStart()
        ClientManager.getInstance().addConnectListener(listener)
        // 绑定到后台服务
        bindService(Intent(this, ClientService::class.java), conn, BIND_AUTO_CREATE)
        // 请求二维码权限
        if (!EasyPermissions.hasPermissions(this, *permissions)) {
            EasyPermissions.requestPermissions(
                    this,
                    "扫描二维码需要打开相机和散光灯的权限",
                    REQUEST_CODE_QRCODE_PERMISSIONS,
                    *permissions
            )
        }
    }

    override fun onStop() {
        super.onStop()
        ClientManager.getInstance().removeConnectListener(listener)
        if (mBinder != null) unbindService(conn)
    }

    override fun onActivityResult(result: ActivityResult) {
        if (result.resultCode != RESULT_OK || result.data == null) {
            Log.d(TAG, "扫描失败！")
            return
        }
        val qrcode = result.data?.getStringExtra("barcode") ?: return
        Log.d(TAG, "扫描结果：$qrcode")
        if (qrcode.startsWith("PB:")) {
            val parts = qrcode.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            mBinding.txtIp.setText(parts[1])
            mBinding.txtPort.setText(parts[2])
            if (parts.size > 3) {
                mBinding.txtConnectionCode.setText(parts[3])
            }
        }
    }

    // 开始扫描二维码
    private fun beginScan() {
        if (!EasyPermissions.hasPermissions(this@ConfigActivity, *permissions)) {
            EasyPermissions.requestPermissions(
                    this,
                    "扫描二维码需要打开相机和散光灯的权限",
                    REQUEST_CODE_QRCODE_PERMISSIONS,
                    *permissions
            )
        } else {
            launcher.launch(Intent(this, QrcodeScanActivity::class.java))
        }
    }

    private fun save() {
        instance.mServerPort = mBinding.txtPort.text.toString()
        instance.mServerHost = mBinding.txtIp.text.toString()
        instance.ConnectionCode = mBinding.txtConnectionCode.text.toString()
        instance.webSocketPort = mBinding.etWebsocketPort.text.toString()
        instance.webSocketCode = mBinding.etWebsocketCode.text.toString()
        instance.saveConfig()
    }

    /**
     * 更新连接状态显示
     *
     * @param status
     * @param message 额外的错误消息
     */
    private fun updateConnectionStatus(status: ConnectionStatus, message: String?) {
        mBinding.btnSave.isClickable = status != ConnectionStatus.Connecting
        mBinding.btnSave.isEnabled = status != ConnectionStatus.Connecting
        var tmp = when (status) {
            ConnectionStatus.Connected -> "已连接"
            ConnectionStatus.Disconnected -> "未连接"
            ConnectionStatus.Connecting -> "连接中..."
            ConnectionStatus.LoggedIn -> "已登录"
            else -> ""
        }
        if (!message.isNullOrEmpty()) {
            tmp = "$tmp-$message"
        }
        mBinding.txtConnectionStatus.text = tmp
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {}
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {}

    private val conn: ServiceConnection = object : ServiceConnection {
        /**
         * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
         * 通过这个IBinder对象，实现宿主和Service的交互。
         */
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "绑定成功调用：onServiceConnected")
            mBinder = service as LocalBinder
        }

        /**
         * 当取消绑定的时候被回调。但正常情况下是不被调用的，它的调用时机是当Service服务被意外销毁时，
         * 例如内存的资源不足时这个方法才被自动调用。
         */
        override fun onServiceDisconnected(name: ComponentName) {
            mBinder = null
        }
    }
    private val listener = QuickerConnectListener { status: ConnectionStatus, message: String? ->
        mHandler.post {
            updateConnectionStatus(status, message)
            if (status != ConnectionStatus.LoggedIn) return@post
            ToastUtils.showShort(this, "连接成功！")
            val goMainActivity = Intent(this, MainActivity::class.java)
            goMainActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(goMainActivity)
        }
    }

    companion object {
        private const val REQUEST_CODE_QRCODE_PERMISSIONS = 1
        private val TAG = ConfigActivity::class.java.simpleName
    }
}