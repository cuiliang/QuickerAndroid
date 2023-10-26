package cuiliang.quicker.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.cuiliang.quicker.ui.BaseVBActivity
import com.cuiliang.quicker.ui.EmptyViewModel
import cuiliang.quicker.adapter.EventOrActionAdapter
import cuiliang.quicker.databinding.FragmentMyTaskBinding
import cuiliang.quicker.taskManager.BaseEventOrAction
import cuiliang.quicker.taskManager.JsonAction
import cuiliang.quicker.taskManager.JsonEvent
import cuiliang.quicker.taskManager.TaskDataFactory
import cuiliang.quicker.taskManager.TaskEventType
import cuiliang.quicker.taskManager.action.Action
import cuiliang.quicker.taskManager.event.Event

/**
 * 事件和动作列表
 * 事件，或者说条件。比如电量更新事件、系统通知事件、WIFI打开事件、蓝牙关闭事件等等
 * 动作：当满足某个事件时执行的操作，比如电量低于xxx发送提醒消息
 * @see TaskDataFactory
 * @see TaskEventType
 */
class EventOrActionActivity : BaseVBActivity<FragmentMyTaskBinding, EmptyViewModel>() {
    private lateinit var eventOrActionAdapter: EventOrActionAdapter
    private var dataType = 0

    override val mViewModel: EmptyViewModel by lazy { ViewModelProvider(this)[EmptyViewModel::class.java] }

    override fun onInit() {
        eventOrActionAdapter = EventOrActionAdapter(this) {
            resultAndFinish(it)
        }
        intent.getStringArrayExtra(UNABLE_ADD_LIST)?.let {
            eventOrActionAdapter.setUnableAddList(it)
        }
        dataType = intent.getIntExtra(DATA_TYPE, 0)
        if (dataType == 0) {
            eventOrActionAdapter.setEvents(TaskDataFactory().getEvents())
        } else {
            eventOrActionAdapter.setEvents(TaskDataFactory().getActions())
        }
        mBinding.root.adapter = eventOrActionAdapter
    }

    private fun resultAndFinish(data: BaseEventOrAction) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(DATA_TYPE, dataType)
            putExtra(
                RESULT_DATA,
                if (dataType == 0) {
                    JsonEvent(data as Event).toString()
                } else {
                    JsonAction(data as Action).toString()
                }
            )
        })
        finish()
    }

    companion object {
        const val UNABLE_ADD_LIST = "UNABLE_ADD_LIST"
        const val DATA_TYPE = "DATA_TYPE"
        const val RESULT_DATA = "RESULT_DATA"

        fun getLauncher(
            activity: ComponentActivity,
            callback: ActivityResultCallback<ActivityResult>
        ): ActivityResultLauncher<Intent> {
            return activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                callback
            )
        }

        /**
         * @param type 0=if条件；1=满足条件执行的内容
         * @param list 不可添加列表，当该item已经添加过了，就属于不可添加
         */
        fun getInstant(context: Context, type: Int, list: Array<String>): Intent {
            return Intent(context, EventOrActionActivity::class.java).apply {
                putExtra(DATA_TYPE, type)
                putExtra(UNABLE_ADD_LIST, list)
            }
        }
    }
}