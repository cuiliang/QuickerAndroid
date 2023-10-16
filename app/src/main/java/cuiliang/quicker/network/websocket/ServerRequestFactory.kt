package cuiliang.quicker.network.websocket

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import cuiliang.quicker.util.KLog
import cuiliang.quicker.util.ToastUtils

/**
 * Created by voidcom on 2023/10/13
 * 用于解析PC请求
 * @see MsgRequestData
 */
object ServerRequestFactory {
    fun decodeRequest(ctx: Context, data: MsgRequestData) {
        when (data.extData) {
            "ShareAndroid" -> {
                when (data.operation) {
                    "copy" -> (ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).let {
                        it.setPrimaryClip(ClipData.newPlainText(data.data, data.data))
                    }

                    "open" -> if (data.data.startsWith("http:") || data.data.startsWith("https:")) {
                        ctx.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(data.data)
                            ).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                        KLog.d("decodeRequest", "打开网页：${data.data}")
                    }
                }
            }
        }
    }
}