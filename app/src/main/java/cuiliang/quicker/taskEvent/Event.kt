package cuiliang.quicker.taskEvent

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.app.DialogCompat
import cuiliang.quicker.R

/**
 * Created by Silent on 2023/9/18 16:12
 * 事件接口
 */
abstract class Event {
    lateinit var mDialog: Dialog

    abstract fun getIcon(): Int

    abstract fun getName(): String

    /**
     * 事件详情弹窗
     * 在添加事件时，事件被点击弹出的窗口
     * 用于显示该事件支持什么操作
     * 举例：通知弹窗——被点击时有输入框可以输入通知的内容
     * @param callback 弹窗内容操作完成的回调，数据用bundle携带
     */
    abstract fun getDialog(callback: (Bundle) -> Unit): Dialog

    fun initDialog(context: Context, @LayoutRes layout: Int) {
        mDialog = Dialog(context)
        mDialog.setContentView(R.layout.dialog_event_root_view)
        val contentView = DialogCompat.requireViewById(mDialog, R.id.contentView)
        contentView.
        //todo callback
    }
}