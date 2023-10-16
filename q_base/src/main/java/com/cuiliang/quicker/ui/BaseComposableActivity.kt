package com.cuiliang.quicker.ui

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * Created by voidcom on 2023/10/7
 *
 */
abstract class BaseComposableActivity<VM : BaseViewModel> : BaseActivity<VM>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.onInit(this)
        mViewModel.onInitData()
    }

    override fun onInit() {
    }
}