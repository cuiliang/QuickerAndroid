package cuiliang.quicker.taskManager

import android.content.Context
import androidx.appcompat.app.AlertDialog

/**
 * Created by Voidcom on 2023/9/19 23:04
 * 任务接口
 */
interface BaseEventOrAction {

    fun getIcon(): Int

    fun getName(): String
    fun getType(): TaskType

    /**
     * 点这个接口的实体类被选择后应该显示的文本内容。
     */
    fun resultStr():String

    /**
     * 事件弹窗
     * 在添加事件时，事件被点击弹出的窗口
     * 用于显示该事件支持内容
     * 举例：
     * 电量状态——显示列表，电量低于xx会触发这个事件
     * @param callback 弹窗内容操作完成的回调，数据用bundle携带。
     */
    fun showDialogAndCallback(context: Context, callback: (BaseEventOrAction) -> Unit)

    fun init(context: Context)

    fun release(context: Context)

    fun getDialogBuilder(context: Context): AlertDialog.Builder =
        AlertDialog.Builder(context)
}