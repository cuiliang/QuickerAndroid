package cuiliang.quicker.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.DialogCompat
import androidx.recyclerview.widget.RecyclerView
import cuiliang.quicker.R
import cuiliang.quicker.adapter.TaskEventAdapter
import cuiliang.quicker.databinding.ActivityTaskEventBinding
import cuiliang.quicker.taskEvent.Event
import cuiliang.quicker.taskEvent.EventFactory

/**
 * 条件，或者说事件。比如电量更新事件、系统通知事件、WIFI打开事件、蓝牙关闭事件等等
 * 当事件触发后执行预先设置的操作。如：WebSocket发送一条消息、执行quicker某个动作、执行Android某个任务等
 */
class TaskEventActivity : AppCompatActivity() {
    private lateinit var taskEventAdapter: TaskEventAdapter
    private lateinit var dialogEventAdapter: TaskEventAdapter
    private lateinit var mBinding: ActivityTaskEventBinding
//    private lateinit var mDialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityTaskEventBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        taskEventAdapter = TaskEventAdapter(this) {
            //事件item被点击，这里弹一个dialog
//            mDialog.show()
        }

        taskEventAdapter.setEvents(EventFactory().getEvents())
        mBinding.rvEventList.adapter = taskEventAdapter

//        mDialog = Dialog(this)
//        mDialog.setContentView(R.layout.activity_task_event)
        dialogEventAdapter=TaskEventAdapter(this){
        }

//        val dialogRv = DialogCompat.requireViewById(mDialog, R.id.rvEventList) as RecyclerView
//        dialogRv.adapter = dialogEventAdapter
//        dialogEventAdapter.setEvents(arrayListOf<Event>().apply {
//            add(TaskEventAdapter.Event("发送通知消息",R.drawable.ic_battery))
//            add(TaskEventAdapter.Event("发送通知消息2",R.drawable.ic_battery))
//        })
    }
}