package com.cuiliang.quicker.ui

import android.content.Context

/**
 * Created by voidcom on 2023/10/7
 *
 */
class EmptyViewModel : BaseViewModel() {
    override val model: BaseModel? = null

    override fun onInit(context: Context) {
    }

    override fun onInitData() {
    }
}