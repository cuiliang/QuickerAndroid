package com.cuiliang.quicker.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding

/**
 * Created by voidcom on 2023/10/7
 *
 */
abstract class BaseViewModel : ViewModel() {
    lateinit var vmBinding: ViewBinding

    abstract val model: BaseModel?

    abstract fun onInit(context: Context)

    abstract fun onInitData()
}