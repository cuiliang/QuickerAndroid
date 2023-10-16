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
import androidx.compose.runtime.remember
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
import com.cuiliang.quicker.ui.BaseComposableActivity
import com.cuiliang.quicker.ui.EmptyViewModel
import cuiliang.quicker.R
import cuiliang.quicker.ui.share.ui.theme.QuickerAndroidTheme
import cuiliang.quicker.util.KLog
import cuiliang.quicker.util.ShareDataToPCManager
import cuiliang.quicker.util.ToastUtils

class ShareActivity : BaseComposableActivity<EmptyViewModel>() {
    val showLoading: MutableState<Boolean> by lazy { mutableStateOf(true) }
    var startActivityShowShareConfig: Boolean = false


    override val mViewModel: EmptyViewModel by lazy { EmptyViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShareDataToPCManager.instant.readShareConfig()
        startActivityShowShareConfig = intent.getBooleanExtra("showShareConfig", false)
        if (startActivityShowShareConfig) {
            showLoading.value = false
        } else
            showLoading.value = ShareDataToPCManager.instant.isHaveUserInfo

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            QuickerAndroidTheme {
                MainContext()
            }
        }
    }

    fun shareText() {
        when {
            intent?.action == Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Handle text being sent
                } else if (intent.type?.startsWith("image/") == true) {
//                    handleSendImage(intent) // Handle single image being sent
                }
            }

//            intent?.action == Intent.ACTION_SEND_MULTIPLE
//                    && intent.type?.startsWith("image/") == true -> {
//                handleSendMultipleImages(intent) // Handle multiple images being sent
//            }
        }
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            KLog.d("ShareActivity", "it:$it")
            ShareDataToPCManager.instant.sendShareText(it) { result ->
                ToastUtils.showShort(this@ShareActivity, if (result) "发送成功" else "分享失败")
                finish()
            }
        }
    }

    //    private fun handleSendImage(intent: Intent) {
//        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
//            // Update UI to reflect image being shared
//        }
//    }
//
//    private fun handleSendMultipleImages(intent: Intent) {
//        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
//            // Update UI to reflect multiple images being shared
//        }
//    }

    val onClick = object : (Int) -> Unit {
        override fun invoke(p1: Int) {
            if (startActivityShowShareConfig) {
                finish()
            } else {
                showLoading.value = true
            }

            if (p1 == 1) {
                ShareDataToPCManager.instant.saveShareConfig()
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
fun MainContext() {
    val ctx = LocalContext.current
    Surface(color = if (!(ctx as ShareActivity).showLoading.value) Color.White else Color.Transparent) {
        if (!ctx.showLoading.value) FirstUse() else DisplayLoading()
    }
}

/**
 * 第一次使用要配置使用信息
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun FirstUse() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)
    ) {
        val ctx = LocalContext.current
        CreateText(R.string.shareConfig_Title, fontSize = 25.sp)
        CreateText(R.string.shareConfig_subTitle)
        CreateText(R.string.shareConfig_Hint)
        Column(horizontalAlignment = Alignment.Start) {
            CreateInput(v = ShareDataToPCManager.instant.userName, resId = R.string.inputUserName)
            CreateInput(v = ShareDataToPCManager.instant.authCode, resId = R.string.inputPushCode)
        }
        ShowAuthCodeHelpPic(Modifier.padding(vertical = 20.dp))
        Row(modifier = Modifier, Arrangement.Start, Alignment.CenterVertically) {
            CreateButton(Modifier.padding(end = 20.dp), resId = R.string.btnNextTime) {
                (ctx as ShareActivity).onClick(0)
            }
            CreateButton(
                Modifier.padding(start = 20.dp),
                resId = R.string.btnAccept,
                isEnable = ShareDataToPCManager.instant.let {
                    it.userName.value.isNotEmpty() && it.authCode.value.isNotEmpty()
                }
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

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Column(
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Row(
//            horizontalArrangement = Arrangement.Center,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Hello $name!",
//                color = MaterialTheme.colorScheme.primary,
//                fontSize = 30.sp,
//                modifier = modifier.wrapContentHeight()
//            )
//            Text(text = "12121",
//                style = MaterialTheme.typography.titleLarge,
//                color = MaterialTheme.colorScheme.primary,
//                onTextLayout = {
//                })
//        }
//        Text(
//            text = "Hello $name!",
//            modifier = modifier
//
//        )
//        Text(text = "12121", onTextLayout = {
//
//        })
//    }
//}

//@Preview(
//    name = "Dark Mode"
//)
//@Preview(
//    uiMode = Configuration.UI_MODE_NIGHT_YES,
//    showBackground = true,
//    name = "Light Mode"
//)
//@Composable
//fun GreetingPreview() {
//
////    QuickerAndroidTheme {
////        Greeting("Android")
//////        Surface(
//////            modifier = Modifier.fillMaxSize(),
//////            color = MaterialTheme.colorScheme.background
//////        ) {
//////            Greeting("Android")
//////        }
////    }
//    val array = arrayListOf<Message>()
//    (0..10).forEach {
//        array.add(Message().apply {
//            what = it
//        })
//
//    }
////    canList(msg = array)
//    val a = arrayListOf<String>()
//    (0..1).forEach {
//        a.add("---------------------$it")
//    }
//    aaa(a)
//}

//@Composable
//fun aaa(names: List<String>) {
//    names.forEach {
//        Text("hi $it")
//    }
//}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun canList(msg: List<Message>) {
//    LazyColumn(modifier = Modifier.fillMaxWidth()) {
//        items(items = msg) {
//            Text(text = it.what.toString())
////            ListItem(headlineText = {  })
//        }
//    }
//}

