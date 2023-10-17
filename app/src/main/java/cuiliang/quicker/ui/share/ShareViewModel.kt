package cuiliang.quicker.ui.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cuiliang.quicker.ui.BaseModel
import com.cuiliang.quicker.ui.BaseViewModel
import cuiliang.quicker.util.KLog
import cuiliang.quicker.util.ShareDataToPCManager
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Created by voidcom on 2023/10/10
 *
 */
class ShareViewModel : BaseViewModel() {
    //用户名，用于分享操作
    val userName: MutableState<String> by lazy { mutableStateOf("") }

    //用户推送验证码，用于分享操作
    val authCode: MutableState<String> by lazy { mutableStateOf("") }

    val showLoading: MutableState<Boolean> by lazy { mutableStateOf(true) }

    override val model: BaseModel? = null

    override fun onInit(context: Context) {
    }

    override fun onInitData() {
    }

    fun haveUserConfig(): Boolean = userName.value.isNotEmpty() && authCode.value.isNotEmpty()

    suspend fun readShareConfig(context: Context) {
        context.dataStore.data.map {
            return@map arrayListOf<String>().apply {
                add(it[SHARE_USER_NAME] ?: "")
                add(it[SHARE_AUTH_CODE] ?: "")
            }
        }.collect {
            userName.value = it[0]
            authCode.value = it[1]
        }
    }

    suspend fun saveShareConfig(context: Context) {
        context.dataStore.edit {
            it[SHARE_USER_NAME] = userName.value
            it[SHARE_AUTH_CODE] = authCode.value
        }
    }

    /**
     * @param callback 执行在IO线程
     */
    fun handleSendText(txt: String, callback: ((Boolean) -> Unit)) {
        txt?.let {
            KLog.d("ShareActivity", "it:$it")
            ShareDataToPCManager.instant.sendShareText(
                userName.value,
                authCode.value,
                it,
                callback
            )
        }
    }

    fun handleSendImage(intent: Intent, callback: ((Boolean) -> Unit)) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            // Update UI to reflect image being shared
        }
    }

    fun handleSendMultipleImages(intent: Intent, callback: ((Boolean) -> Unit)) {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
            // Update UI to reflect multiple images being shared
        }
    }

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ShareConfig")
        private val SHARE_USER_NAME = stringPreferencesKey("SHARE_USER_NAME")
        private val SHARE_AUTH_CODE = stringPreferencesKey("SHARE_AUTH_CODE")
    }
}