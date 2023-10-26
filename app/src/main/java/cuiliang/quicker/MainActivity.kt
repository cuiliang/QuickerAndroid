package cuiliang.quicker

import android.app.ProgressDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.PictureDrawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognizerIntent
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cuiliang.quicker.ui.BaseVBActivity
import com.cuiliang.quicker.ui.EmptyViewModel
import cuiliang.quicker.client.ClientConfig.Companion.instance
import cuiliang.quicker.client.ClientManager
import cuiliang.quicker.client.ClientManager.QuickerConnectListener
import cuiliang.quicker.client.ClientService
import cuiliang.quicker.client.ClientService.LocalBinder
import cuiliang.quicker.client.ConnectionStatus
import cuiliang.quicker.client.MessageCache
import cuiliang.quicker.client.QuickerServiceHandler.Companion.instant
import cuiliang.quicker.client.QuickerServiceListener
import cuiliang.quicker.databinding.ActivityMainBinding
import cuiliang.quicker.messages.MessageBase
import cuiliang.quicker.messages.recv.UpdateButtonsMessage
import cuiliang.quicker.messages.recv.UpdateButtonsMessage.ButtonItem
import cuiliang.quicker.messages.recv.VolumeStateMessage
import cuiliang.quicker.messages.send.CommandMessage
import cuiliang.quicker.messages.send.TextDataMessage
import cuiliang.quicker.network.websocket.WebSocketClient.Companion.instance
import cuiliang.quicker.svg.SvgSoftwareLayerSetter
import cuiliang.quicker.ui.ConfigActivity
import cuiliang.quicker.ui.share.ShareActivity.Companion.getIntent
import cuiliang.quicker.ui.taskManager.TaskListActivity
import cuiliang.quicker.util.DataPageValues
import cuiliang.quicker.util.ImagePicker
import cuiliang.quicker.util.ToastUtils
import cuiliang.quicker.util.setVisible
import java.io.ByteArrayOutputStream
import java.util.Locale

class MainActivity : BaseVBActivity<ActivityMainBinding, EmptyViewModel>(), QuickerConnectListener {
    private var oldVolumeState: VolumeStateMessage? = null
    private var mBinder: LocalBinder? = null

    // 最后处理的消息，防止重复处理
    private val _lastProcessedMessages = MessageCache()

    // fontawesome的svg图标存储服务器
    private val svgServer = "https://files.getquicker.net/fa/5.15.3/svgs/{style}/{name}.svg"

    // 用于显示svg图标
    private lateinit var requestBuilder: RequestBuilder<PictureDrawable>

    private lateinit var wakeLock: WakeLock
    private lateinit var launchTakePhoto: ActivityResultLauncher<Intent>
    private lateinit var launchVoiceRecognition: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        // 禁止屏幕关闭
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        // 隐藏底部的导航按钮
        hideBottomUIMenu()
        super.onCreate(savedInstanceState)
        //检查程序中是否缓存了网络配置，如果有，跳过配置步骤直接连接服务器
        if (!instance.hasCache()) {
            goConfigActivity(true)
            //在onCreate执行finish()后，后续其他声明周期的方法不会被执行
            finish()
            return
        } else {
            instance.readConfig()
        }

        //
        // 数据初始化
        //
        requestBuilder = Glide.with(this)
            .`as`(PictureDrawable::class.java)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .listener(SvgSoftwareLayerSetter())
        // 给图标增加加载动画
        val anim = AnimatedVectorDrawableCompat.create(this, R.drawable.anim_load)
        if (anim != null) {
            anim.start()
            requestBuilder = requestBuilder.placeholder(anim)
        }

        // 依据屏幕方向加载
//        int orientation = getResources().getConfiguration().orientation;
//        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            setContentView(R.layout.activity_main);
//        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//            setContentView(R.layout.activity_main_portrait);
//        }
    }

    override val mViewModel: EmptyViewModel by lazy { ViewModelProvider(this)[EmptyViewModel::class.java] }

    override fun onInit() {
        launchTakePhoto = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            takePhotoCallback
        )
        launchVoiceRecognition = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            voiceRecognitionCallback
        )
        requestBuilder = Glide.with(this)
            .`as`(PictureDrawable::class.java)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .listener(SvgSoftwareLayerSetter())
        createActionButtons()
        setupUiListeners()

        mBinding.linearLayout2.taskManager.setOnClickListener {
            startActivity(Intent(this, TaskListActivity::class.java))
        }
    }

    private fun recreateView() {
        // 创建action按钮
        createActionButtons()
        setupUiListeners()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreateView()
        ClientManager.getInstance().requestReSendState()
        processPcMessage(mBinder?.getMsgCache()?.lastVolumeStateMessage)
        processPcMessage(mBinder?.getMsgCache()?.lastUpdateButtonsMessage)
    }
    //region 依据服务器消息更新界面
    /**
     * 更新按钮状态
     *
     * @param item
     */
    private fun updateButton(item: ButtonItem, globalPageIndex: Int, contextPageIndex: Int) {
        val button = getButtonByIndex(item.Index, intArrayOf(globalPageIndex, contextPageIndex))
            ?: return
        button.button.isEnabled = item.IsEnabled

        //无论是否禁用，都加载文字和图片
        if (item.Label != null && !item.Label.isEmpty()) {
            item.Label = item.Label.replace("\\n", "\n")
            button.textView.text = item.Label
            button.textView.visibility = View.VISIBLE
        } else {
            button.textView.text = ""
            button.textView.visibility = View.INVISIBLE
        }
        if (item.IconFileContent != null && !item.IconFileContent.isEmpty()) {
            val imgContent = Base64.decode(item.IconFileContent, Base64.DEFAULT)
            Log.d(TAG, "图标文件长度：" + imgContent.size)
            //                Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imgContent, 0, imgContent.length));
//                button.setCompoundDrawables(image, null, null, null);
            Glide.with(this)
                .asBitmap()
                .load(imgContent)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(button.imageView)
            button.imageView.visibility = View.VISIBLE
        } else if (item.IconFileName != null && !item.IconFileName.isEmpty()) {
            if (item.IconFileName.startsWith("fa:")) {
                val split = item.IconFileName.substring(3).split("_|:".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val style = split[0].lowercase(Locale.getDefault())
                val name = split[1]
                    .replace("([a-z])([A-Z]+)".toRegex(), "$1-$2")
                    .lowercase(Locale.getDefault())
                requestBuilder
                    .load(svgServer.replace("{style}", style).replace("{name}", name))
                    .into(button.imageView)
            } else if (item.IconFileName.endsWith(".svg")) {
                requestBuilder
                    .load(item.IconFileName)
                    .into(button.imageView)
            } else {
                Glide.with(this)
                    .load(item.IconFileName)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(button.imageView)
            }
            button.imageView.visibility = View.VISIBLE
        } else {
            button.imageView.setImageBitmap(null)
            button.imageView.visibility = View.GONE
        }
    }

    // 更新声音状态显示
    private fun updateVolumeState(message: VolumeStateMessage) {
        if (message.Mute) {
            mBinding.btnMute.setImageResource(R.drawable.ic_volume_off_black_24dp)
        } else {
            if (message.MasterVolume > 50) {
                mBinding.btnMute.setImageResource(R.drawable.ic_volume_up_black_24dp)
            } else if (message.MasterVolume > 5) {
                mBinding.btnMute.setImageResource(R.drawable.ic_volume_down_black_24dp)
            } else {
                mBinding.btnMute.setImageResource(R.drawable.ic_volume_mute_black_24dp)
            }
        }
        mBinding.seekbarVolume.progress = message.MasterVolume
        mBinding.seekbarVolume.setVisible(!message.Mute)
        oldVolumeState = message
    }

    //endregion

    private fun beginTakePhoto() {
        if (mDialog != null) {
            ToastUtils.showShort(applicationContext, "照片正在传输中，暂时无法拍照。")
            return
        }
        launchTakePhoto.launch(ImagePicker.getPickImageIntent(this))
    }

    var mDialog: ProgressDialog? = null
    private fun sendImage(bitmap: Bitmap) {
        Log.d(TAG, "开始发送图片")
        mDialog = ProgressDialog.show(this@MainActivity, "Quicker", "正在发送图片，请稍候……")

//        mDialog = new ProgressDialog(MainActivity.this);
//        mDialog.setMessage("正在发送图片...");
//        mDialog.setCancelable(false);
//        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        mDialog.show();
        val thread = Thread {

//            spandTimeMethod();// 耗时的方
//                 handler.sendEmptyMessage(0);// 执行耗时的方法之后发送消给handler
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
            val byteArray = stream.toByteArray()
            ClientManager.getInstance().sendPhotoMsg("image.png", byteArray)
            mHandler.post {
                mDialog!!.dismiss() // 关闭ProgressDialog
                mDialog = null
                ToastUtils.showShort(applicationContext, "已发送图片")
            }
        }
        thread.start()

//        try {
//            thread.join();
//        } catch (InterruptedException e) {
//            // ...
//        }
    }

    //endregion
    override fun onStart() {
        super.onStart()
        instant.addListener(listener)

        // 绑定到后台服务
        bindService(Intent(this, ClientService::class.java), conn, BIND_AUTO_CREATE)

        //
        //if (clientService.getClientManager().isConnected() == false) {
        // 未连接，进入配置界面
        //showToast("尚未连接电脑");
        //goConfigActivity();
        //}
        setupScreenLight()
    }

    override fun onResume() {
        super.onResume()
        //websocket连接
        instance().connectRequest { _: Boolean?, _: String? -> }
        ClientManager.getInstance().requestReSendState()
    }

    override fun onStop() {
        super.onStop()
        wakeLock.release()
        ClientManager.getInstance().removeConnectListener(this)
        instant.removeListener(listener)
        if (mBinder != null) {
            unbindService(conn)
        }
    }

    override fun statusUpdate(status: ConnectionStatus?, message: String?) {
        // 如果连接断开，进入配置页面
        if (status == ConnectionStatus.Disconnected || status == ConnectionStatus.LoginFailed) {
            mHandler.post { goConfigActivity(true) }
        }
    }

    /**
     * 根据序号获取按钮对象
     *
     * @param index            按钮编号
     * @param currentPageIndex 当前页面索引数组，currentPageIndex[0]是全局页面索引，currentPageIndex[1]是上下文页面索引
     * @return 如果存在返回按钮对象
     */
    private fun getButtonByIndex(index: Int, currentPageIndex: IntArray): UiButtonItem {
        //TODO: 按钮不存在的情况
        return if (index < 1000000) {
            mBinding.globalView.getActionBtnObject(index, currentPageIndex[0])
        } else {
            mBinding.contextView.getActionBtnObject(index, currentPageIndex[1])
        }
    }
    // region 生成按钮
    /**
     * 生成界面按钮
     */
    private fun createActionButtons() {
        mBinding.globalView.initView(mBinding.linearLayout2.viewpagerCuePoint, true)
        mBinding.contextView.initView(mBinding.linearLayout2.viewpagerCuePoint, false)
    }

    // endregion
    // region Utility辅助代码
    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(25, 50))
        } else {
            vibrator.vibrate(25)
        }
    }

    /**
     * 根据给定的dp单位数值，计算pix数值
     *
     * @param dp dp数值
     * @return pix数值
     */
    private fun pxFromDp(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale).toInt()
    }

    //endregion
    /**
     * 进入配置页面，autoReturn指示了联网成功后，是否自动返回主activity
     *
     * @param autoReturn
     */
    private fun goConfigActivity(autoReturn: Boolean) {
        startActivity(Intent(this, ConfigActivity::class.java))
    }

    /**
     * 设置界面按钮的事件处理
     */
    private fun setupUiListeners() {
        mBinding.btnConfig.setOnClickListener { goConfigActivity(false) }
        mBinding.btnMute.setOnClickListener { ClientManager.getInstance().sendToggleMuteMsg() }
        mBinding.seekbarVolume.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                ClientManager.getInstance().sendUpdateVolumeMsg(seekBar.progress)
            }
        })
        //        ImageButton btnScanQrcode = (ImageButton) findViewById(R.id.btnPc);
//        btnScanQrcode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, ScanBarcodeActivity.class);
//                startActivityForResult(intent, REQ_CODE_SCAN_BRCODE);// 表示可以返回结果
//            }
//        });
        mBinding.btnPc.setOnClickListener {
            ClientManager.getInstance().sendCommandMsg(CommandMessage.OPEN_MAINWIN, "")
        }
        mBinding.btnPhoto.setOnClickListener { beginTakePhoto() }
        mBinding.btnVoice.setOnClickListener { startVoiceInput() }
        mBinding.linearLayout2.shareIv.setOnClickListener { startActivity(getIntent(this, true)) }
    }

    /**
     * 开启语音输入
     */
    private fun startVoiceInput() {
        //开始识别，这里检测手机是否支持语音识别并且捕获异常
        launchVoiceRecognition.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            //设置模式，目前设置的是自由识别模式
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            //提示语言开始文字，就是效果图上面的文字
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说")
        })
    }

    /**
     * 处理pc消息
     */
    private fun processPcMessage(originMessage: MessageBase?) {
        if (originMessage == null) return
        if (originMessage is UpdateButtonsMessage) {
            //更新page通用数据
            DataPageValues.contextPageName = originMessage.ProfileName
            DataPageValues.contextDataPageCount = originMessage.ContextPageCount
            DataPageValues.currentContextPageIndex = originMessage.ContextPageIndex
            DataPageValues.globalDataPageCount = originMessage.GlobalPageCount
            DataPageValues.currentGlobalPageIndex = originMessage.GlobalPageIndex
            DataPageValues.IsContextPanelLocked = originMessage.IsContextPanelLocked
            mBinding.globalView.updatePage(
                originMessage.GlobalPageCount,
                originMessage.GlobalPageIndex
            )
            mBinding.contextView.updatePage(
                originMessage.ContextPageCount,
                originMessage.ContextPageIndex
            )

            //if (serverMsg != _lastProcessedMessages.lastUpdateButtonsMessage) {
            _lastProcessedMessages.lastUpdateButtonsMessage = originMessage

//            Log.d(TAG, "更新" + serverMsg.Buttons.length + "个按钮！" +
//                    "\nuserName:" + DataPageValues.contextPageName +
//                    "\nglobalDataPageCount:" + DataPageValues.globalDataPageCount +
//                    "\ncurrentGlobalPageIndex:" + DataPageValues.currentGlobalPageIndex +
//                    "\ncontextDataPageCount:" + DataPageValues.contextDataPageCount +
//                    "\ncurrentContextPageIndex:" + DataPageValues.currentContextPageIndex);
            mBinding.linearLayout2.txtProfileName.text = originMessage.ProfileName
            for (btn in originMessage.Buttons) {

                //Button button = getButtonByIndex(btn.Index);
//                            button.setText(btn.Label);
                updateButton(btn, originMessage.GlobalPageIndex, originMessage.ContextPageIndex)
            }
            //            } else {
//                Log.d(TAG, "已经处理过这个消息了。");
//            }
        } else if (originMessage is VolumeStateMessage) {

            //if (volumeStateMessage != _lastProcessedMessages.lastVolumeStateMessage) {
            _lastProcessedMessages.lastVolumeStateMessage = originMessage
            updateVolumeState(originMessage)
            //            }else {
//                Log.d(TAG, "已经处理过这个消息了。");
//            }
        } else if (originMessage is CommandMessage) {
            Log.d(TAG, "收到启动语音输入消息。" + originMessage.Command)
            if (originMessage.Command == CommandMessage.START_VOICE_INPUT) {
                Log.d(TAG, "启动语音输入。")
                startVoiceInput()
            }
        }
    }

    /**
     * 设置背光亮一段时间
     */
    private fun setupScreenLight() {
        val pm = this.getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "quicker:MyWakeLock")
        wakeLock.acquire((60 * 60 * 1000).toLong())
    }

    private val listener: QuickerServiceListener = object : QuickerServiceListener() {
        override fun onMessage(msg: MessageBase) {
            super.onMessage(msg)
            mHandler.post { processPcMessage(msg) }
        }
    }

    private val conn = object : ServiceConnection {
        /**
         * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
         * 通过这个IBinder对象，实现宿主和Service的交互。
         */
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "绑定成功调用：onServiceConnected")
            mBinder = service as LocalBinder
            ClientManager.getInstance().addConnectListener(this@MainActivity)
            processPcMessage(mBinder?.getMsgCache()?.lastVolumeStateMessage)
            processPcMessage(mBinder?.getMsgCache()?.lastUpdateButtonsMessage)
        }

        /**
         * 当取消绑定的时候被回调。但正常情况下是不被调用的，它的调用时机是当Service服务被意外销毁时，
         * 例如内存的资源不足时这个方法才被自动调用。
         */
        override fun onServiceDisconnected(name: ComponentName) {
            mBinder = null
        }
    }

    private val takePhotoCallback = ActivityResultCallback<ActivityResult> {
        if (it.resultCode != RESULT_OK) return@ActivityResultCallback
        // readPic();
        val bitmap = ImagePicker.getImageFromResult(this, it.resultCode, it.data)
        sendImage(bitmap)
    }

    private val voiceRecognitionCallback = ActivityResultCallback<ActivityResult> {
        if (it.resultCode != RESULT_OK) {
            ToastUtils.showShort(
                applicationContext,
                "抱歉，您的设备当前不支持此功能。请安装Google语音搜索。"
            )
            return@ActivityResultCallback
        }
        //返回结果是一个list，我们一般取的是第一个最匹配的结果
        val text = it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: arrayListOf()
        ClientManager.getInstance().sendTextMsg(TextDataMessage.TYPE_VOICE_RECOGNITION, text[0])
    }

    companion object {
        private const val TAG = "MainActivity"
        fun rotateImage(source: Bitmap, angle: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(
                source, 0, 0, source.width, source.height,
                matrix, true
            )
        }
    }
}