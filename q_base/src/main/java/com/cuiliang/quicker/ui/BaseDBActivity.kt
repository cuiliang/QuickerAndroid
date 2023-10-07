package com.cuiliang.quicker.ui

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * Created by voidcom on 2023/10/7
 * DataBinding
 */
abstract class BaseDBActivity<DB : ViewDataBinding, VM : BaseViewModel> : BaseActivity<VM>() {
    protected val mBinding: DB by lazy { DataBindingUtil.setContentView<DB>(this, getLayoutID()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mBinding.lifecycleOwner = this
        mViewModel.vmBinding = mBinding
        onInit()
        mViewModel.onInit(this)
        mViewModel.onInitData()
    }

    abstract fun getLayoutID(): Int
}