package cuiliang.quicker.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import cuiliang.quicker.adapter.EventOrActionAdapter
import cuiliang.quicker.databinding.FragmentMyTaskBinding
import cuiliang.quicker.taskManager.BaseTaskData
import cuiliang.quicker.taskManager.TaskDataFactory

/**
 * 条件，或者说事件。比如电量更新事件、系统通知事件、WIFI打开事件、蓝牙关闭事件等等
 * 当事件触发后执行预先设置的操作。如：WebSocket发送一条消息、执行quicker某个动作、执行Android某个任务等
 */
class EventOrActionActivity : AppCompatActivity() {
    private lateinit var eventOrActionAdapter: EventOrActionAdapter
    private lateinit var mBinding: FragmentMyTaskBinding
    private var dataType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = FragmentMyTaskBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        eventOrActionAdapter = EventOrActionAdapter(this) {
            resultAndFinish(it)
        }
        if (intent.hasExtra(UNABLE_ADD_LIST)) {
            eventOrActionAdapter.setUnableAddList(intent.getStringArrayExtra(UNABLE_ADD_LIST))
        }
        dataType = intent.getIntExtra(DATA_TYPE, 0)
        if (dataType == 0) {
            eventOrActionAdapter.setEvents(TaskDataFactory().getEvents())
        } else {
            eventOrActionAdapter.setEvents(TaskDataFactory().getActions())
        }
        mBinding.root.adapter = eventOrActionAdapter
    }

    private fun resultAndFinish(data: BaseTaskData) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(DATA_TYPE, dataType)
            putExtra(RESULT_DATA, data)
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