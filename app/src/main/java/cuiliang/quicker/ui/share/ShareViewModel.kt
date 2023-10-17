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
import androidx.datastore.preferences.preferencesDataStore
import com.cuiliang.quicker.ui.BaseModel
import com.cuiliang.quicker.ui.BaseViewModel
import cuiliang.quicker.util.KLog
import cuiliang.quicker.util.ShareDataToPCManager
import kotlinx.coroutines.flow.map

/**
 * Created by voidcom on 2023/10/10
 *
 */
class ShareViewModel : BaseViewModel() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ShareConfig")

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
//        val userNameFlow: Flow<String>
//        val authCodeFlow: Flow<String>
        context.dataStore.data.run {
            map {
                it[ShareActivity.SHARE_USER_NAME] ?: ""
            }.collect{
                userName.value = it
            }
            map {
                it[ShareActivity.SHARE_AUTH_CODE] ?: ""
            }.collect{
                authCode.value = it
            }
        }
//        val userNameFlow = context.dataStore.data.map {
//            it[ShareActivity.SHARE_USER_NAME] ?: ""
//        }
//        val authCodeFlow = context.dataStore.data.map {
//            it[ShareActivity.SHARE_AUTH_CODE] ?: ""
//        }
//        userNameFlow.collect { userName.value = it }
//        authCodeFlow.collect { authCode.value = it }
    }

    suspend fun saveShareConfig(context: Context) {
        context.dataStore.edit {
            it[ShareActivity.SHARE_USER_NAME] = userName.value
            it[ShareActivity.SHARE_AUTH_CODE] = authCode.value
        }
    }

    /**
     * @param callback 执行在IO线程
     */
    fun handleSendText(intent: Intent, callback: ((Boolean) -> Unit)) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
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
}