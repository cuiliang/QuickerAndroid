package cuiliang.quicker.taskManager

import android.os.Parcel

/**
 * Created by Voidcom on 2023/9/19 22:16
 * 抽象类，抽象了任务满足条件后执行的结果。
 */
abstract class Action() : BaseTaskData {
    var resultStr: String = ""

    constructor(parcel: Parcel) : this() {
        resultStr = parcel.readString()
    }

    override fun resultStr(): String = resultStr

    abstract fun actionRunnable(): Runnable

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(resultStr)
    }
}