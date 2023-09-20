package cuiliang.quicker.taskManager

import android.os.Parcel

/**
 * Created by Silent on 2023/9/18 16:12
 * 事件接口
 */
abstract class Event() : BaseTaskData {
    var resultStr: String = ""

    constructor(parcel: Parcel) : this() {
        resultStr = parcel.readString()
    }

    override fun resultStr(): String = resultStr

    /**
     * 事件执行的内容
     * 举例：
     * 电池状态事件——实体类重写该方法实现一个获取电池状态的任务；这个任务会被放进线程池队列中，根据设置的时间定时执行。
     */
    abstract fun eventRunnable(): Runnable

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(resultStr)
    }
}