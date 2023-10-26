package com.cuiliang.quicker.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.cuiliang.quicker.utils.BindingReflex

/**
 * Created by voidcom on 2023/10/7
 * ViewBinding
 */
abstract class BaseVBActivity<VB : ViewBinding, VM : BaseViewModel> : BaseActivity<VM>() {

    protected val mBinding: VB by lazy(LazyThreadSafetyMode.NONE) {
        BindingReflex.reflexViewBinding(javaClass, layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mViewModel.vmBinding = mBinding
        onInit()
        mViewModel.onInit(this)
        mViewModel.onInitData()
        lifecycle.addObserver(object :LifecycleEventObserver{
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                Log.d(this@BaseVBActivity.localClassName,"--lifecycle--${event.name}")
            }
        })
    }

    /**
     * 隐藏虚拟按键
     */
    protected fun hideBottomUIMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.hide(WindowInsets.Type.navigationBars())
        }else{
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE)
        }
    }
}