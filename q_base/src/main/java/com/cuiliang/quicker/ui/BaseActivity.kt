package com.cuiliang.quicker.ui

import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by voidcom on 2023/10/7
 *
 */
abstract class BaseActivity<VM : BaseViewModel> : AppCompatActivity() {
    protected val mHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

    protected abstract val mViewModel: VM

    abstract fun onInit()
}