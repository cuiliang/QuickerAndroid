package cuiliang.quicker.util

import android.content.Context
import android.content.Intent

import android.content.IntentFilter


/**
 * Created by Voidcom on 2023/9/13 19:18
 * TODO
 */
object SystemUtils {
    private const val TAG = "SystemUtils"
    fun getSystemBattery(context: Context) :Int{
        val batteryInfoIntent = context.applicationContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = batteryInfoIntent.getIntExtra("level", 0)
        val batterySum = batteryInfoIntent.getIntExtra("scale", 100)
        val percentBattery = 100 * level / batterySum
        KLog.i(TAG, "手机当前电量------$percentBattery")
        return percentBattery
    }
}