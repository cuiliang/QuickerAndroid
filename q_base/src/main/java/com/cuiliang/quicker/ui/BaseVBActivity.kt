package com.cuiliang.quicker.ui

import android.os.Bundle
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
    }
}