package cuiliang.quicker.ui.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cuiliang.quicker.ui.BaseComposableActivity
import cuiliang.quicker.R
import cuiliang.quicker.ui.share.theme.QuickerAndroidTheme
import cuiliang.quicker.util.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShareActivity : BaseComposableActivity<ShareViewModel>() {
    var startActivityShowShareConfig: Boolean = false

    override val mViewModel: ShareViewModel by lazy { ViewModelProvider(this)[ShareViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            mViewModel.readShareConfig(applicationContext)
        }
        startActivityShowShareConfig = intent.getBooleanExtra("showShareConfig", false)
        if (startActivityShowShareConfig) {
            mViewModel.showLoading.value = false
        } else
            mViewModel.showLoading.value = mViewModel.haveUserConfig()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            QuickerAndroidTheme {
                MainContext(mViewModel.userName, mViewModel.authCode, mViewModel.showLoading)
            }
        }
    }

    fun shareText() {
        when {
            intent?.action == Intent.ACTION_SEND ||
                    intent?.action == Intent.ACTION_PROCESS_TEXT -> {
                if (intent.type?.startsWith("text/") == true) {
                    val txt = when (intent?.action) {
                        Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
                        Intent.ACTION_PROCESS_TEXT -> intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
                        else -> return
                    } ?: return
                    mViewModel.handleSendText(txt) {
                        lifecycleScope.launch {
                            withContext(Dispatchers.Main.immediate) {
                                ToastUtils.showShort(
                                    applicationContext,
                                    if (it) "发送成功" else "分享失败"
                                )
                                finish()
                            }
                        }
                    }
                } else if (intent.type?.startsWith("image/") == true) {
                    mViewModel.handleSendImage(intent) {}
                }
            }

            intent?.action == Intent.ACTION_SEND_MULTIPLE
                    && intent.type?.startsWith("image/") == true -> {
                mViewModel.handleSendMultipleImages(intent) {}
            }
        }
    }

    val onClick = object : (Int) -> Unit {
        override fun invoke(p1: Int) {
            if (startActivityShowShareConfig) {
                finish()
            } else {
                mViewModel.showLoading.value = true
            }

            if (p1 == 1) {
                lifecycleScope.launch {
                    mViewModel.saveShareConfig(applicationContext)
                }
            }
        }
    }

    companion object {

        fun getIntent(context: Context, showConfig: Boolean = false): Intent =
            Intent(context, ShareActivity::class.java).apply {
                putExtra("showShareConfig", showConfig)
            }
    }
}

@Composable
fun MainContext(
    name: MutableState<String> = mutableStateOf(""),
    code: MutableState<String> = mutableStateOf(""),
    loading: MutableState<Boolean> = mutableStateOf(false)
) {
    Surface(color = Color.Transparent) {
        DisplayLoading()
    }
    //判断是否显示第一次使用时的推送配置页面，因为离线场景没坐，所以推送这块先去掉了
//    Surface(color = if (!loading.value) Color.White else Color.Transparent) {
//        if (!loading.value) FirstUse(name, code) else DisplayLoading()
//    }
}

/**
 * 第一次使用要配置使用信息
 */
@Preview(showBackground = true)
@Composable
fun FirstUse(
    name: MutableState<String> = mutableStateOf(""),
    code: MutableState<String> = mutableStateOf("")
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)
    ) {
        val ctx = LocalContext.current
        CreateText(R.string.shareConfig_Title, fontSize = 25.sp)
        CreateText(R.string.shareConfig_subTitle)
        CreateText(R.string.shareConfig_Hint)
        Column(horizontalAlignment = Alignment.Start) {
            CreateInput(v = name, resId = R.string.inputUserName)
            CreateInput(v = code, resId = R.string.inputPushCode)
        }
        ShowAuthCodeHelpPic(Modifier.padding(vertical = 20.dp))
        Row(modifier = Modifier, Arrangement.Start, Alignment.CenterVertically) {
            CreateButton(
                Modifier.padding(end = 20.dp),
                resId = R.string.btnNextTime
            ) {
                (ctx as ShareActivity).onClick(0)
            }
            CreateButton(
                Modifier.padding(start = 20.dp),
                resId = R.string.btnAccept,
                isEnable = name.value.isNotEmpty() && code.value.isNotEmpty()
            ) {
                (ctx as ShareActivity).onClick(1)
            }
        }
    }
}

@Composable
fun ShowAuthCodeHelpPic(modifier: Modifier) {
    val ctx = LocalContext.current
    val styleText = buildAnnotatedString {
        append("不知道")
        withStyle(SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
            pushStringAnnotation(
                "openAuthCodePic", annotation = stringResource(id = R.string.inputPushCode)
            )

            append(stringResource(id = R.string.inputPushCode))
        }
        append("是什么？")
    }
    ClickableText(text = styleText, modifier = modifier, onClick = {
        //点击事件一
        styleText.getStringAnnotations(tag = "openAuthCodePic", start = it, end = it).firstOrNull()
            ?.let {
                ctx.startActivity(Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://getquicker.net/KC/Manual/Doc/connection#i7xjp")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
    })
}

@Composable
fun CreateText(@StringRes resId: Int, fontSize: TextUnit = TextUnit.Unspecified) {
    Text(
        text = stringResource(id = resId),
        fontSize = fontSize,
        modifier = Modifier.padding(10.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInput(v: MutableState<String>, @StringRes resId: Int) {
    OutlinedTextField(value = v.value,
        label = { Text(text = stringResource(id = resId)) },
        modifier = Modifier.padding(10.dp),
        onValueChange = {
            v.value = it
        })
}

@Composable
fun CreateButton(
    modifier: Modifier, @StringRes resId: Int, isEnable: Boolean = true, click: () -> Unit
) {
    Button(
        modifier = modifier, onClick = click, enabled = isEnable
    ) {
        Text(text = stringResource(id = resId))
    }
}

@Composable
        /**
         * 显示加载动画
         */
fun DisplayLoading() {
    Box(
        modifier = Modifier
            .background(Color.Transparent)
            .wrapContentSize(Alignment.Center)
    ) {
        CircularProgressIndicator(modifier = Modifier.size(100.dp))
    }
    (LocalContext.current as ShareActivity).shareText()
}