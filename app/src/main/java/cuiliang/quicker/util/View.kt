package cuiliang.quicker.util

import android.os.Looper
import android.util.TypedValue
import android.view.View
import cuiliang.quicker.QuickerApplication

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

internal fun View.setVisible(visible: Boolean) {
    visibility = if (visible)
        View.VISIBLE
    else
        View.GONE
}

fun dp2px(dpValue: Float): Float = QuickerApplication.displayMetrics?.let {
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dpValue,
        it
    )
} ?: 0f

/**
 * 判断当前线程是否是主线程
 */
fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()
