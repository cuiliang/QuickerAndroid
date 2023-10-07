package cuiliang.quicker.ui.taskEdit

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import cuiliang.quicker.R
import cuiliang.quicker.adapter.TaskDetailsItemAdapter
import cuiliang.quicker.databinding.ActivityTaskEditBinding
import cuiliang.quicker.service.TaskManagerService
import cuiliang.quicker.taskManager.BaseEventOrAction
import cuiliang.quicker.taskManager.JsonAction
import cuiliang.quicker.taskManager.JsonEvent
import cuiliang.quicker.taskManager.Task
import cuiliang.quicker.taskManager.action.Action
import cuiliang.quicker.taskManager.action.ActionAdd
import cuiliang.quicker.taskManager.event.Event
import cuiliang.quicker.taskManager.event.EventAdd
import cuiliang.quicker.ui.EventOrActionActivity

class TaskEditActivity : AppCompatActivity(), ServiceConnection {
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var ifFactoryAdapter: TaskDetailsItemAdapter<Event>
    private lateinit var ifActionAdapter: TaskDetailsItemAdapter<Action>

    private lateinit var mBinding: ActivityTaskEditBinding
    private lateinit var task: Task
    private var mBinder: TaskManagerService.TaskManagerBinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityTaskEditBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        launcher = EventOrActionActivity.getLauncher(this, addEventCallback)

        ifFactoryAdapter = TaskDetailsItemAdapter(this)
        ifFactoryAdapter.setCallback(adapterClickCallback(0))
        ifFactoryAdapter.setFooterData(EventAdd())
        mBinding.rvIfFactorList.adapter = ifFactoryAdapter

        ifActionAdapter = TaskDetailsItemAdapter(this)
        ifActionAdapter.setCallback(adapterClickCallback(1))
        ifActionAdapter.setFooterData(ActionAdd())
        mBinding.rvIfActionList.adapter = ifActionAdapter
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(applicationContext, TaskManagerService::class.java),
            this,
            Service.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        super.onStop()
        unbindService(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_task_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.btn_save -> {
                task.name = mBinding.inputTaskName.text.toString()
                resultAndFinish(task)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (service == null) return
        mBinder = service as TaskManagerService.TaskManagerBinder
        initData()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        mBinder = null
    }

    private fun initData() {
        if (editType == 0) {
            mBinding.toolbar.setTitle(R.string.createTask_str)
            task = Task(true)
            return
        }
        val d = intent.getStringExtra(TASK_NAME)
        task = mBinder?.getTaskList()?.get(d) ?: Task(true)
        mBinding.toolbar.setTitle(R.string.editTask_str)
        mBinding.inputTaskName.text = SpannableStringBuilder.valueOf(task.name)
        ifFactoryAdapter.addItems(task.events)
        ifActionAdapter.addItems(task.taskActions)
    }

    private fun resultAndFinish(task: Task) {
        if (task.name.isEmpty() || task.events.isEmpty() || task.taskActions.isEmpty()) return
        mBinder?.getTaskList()?.put(task.name, task)
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(TASK_NAME, task.name)
            putExtra(EDIT_TYPE, editType)
        })
        finish()
    }

    private val addEventCallback = ActivityResultCallback<ActivityResult> { result ->
        if (result.resultCode != Activity.RESULT_OK || result.data == null) return@ActivityResultCallback
        val dataType = result.data!!.getIntExtra(EventOrActionActivity.DATA_TYPE, 0)
        val type = result.data!!.getStringExtra(EventOrActionActivity.RESULT_DATA)
        if (dataType == 0) {
            ifFactoryAdapter.addItem(JsonEvent.jsonToEvent(type).toEvent())
        } else {
            ifActionAdapter.addItem(JsonAction.jsonToAction(type).toAction())
        }
    }

    private fun <T : BaseEventOrAction> adapterClickCallback(type: Int): (List<T>) -> Unit {
        return {
            val array = Array(it.size) { "" }
            for (i in it.indices) {
                array[i] = it[i].getName()
            }
            launcher.launch(EventOrActionActivity.getInstant(this, type, array))
        }
    }

    companion object {
        private var editType: Int = 0 //0:新建任务 1:编辑任务
        const val EDIT_TYPE = "EDIT_TYPE"
        const val TASK_NAME = "TASK_NAME"

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
         * @param taskName 当taskName=null时，表示新增任务；否则是编辑任务
         */
        fun getIntent(context: Context, taskName: String? = null): Intent {
            editType = if (taskName.isNullOrEmpty()) 0 else 1
            return Intent(context, TaskEditActivity::class.java).apply {
                putExtra(TASK_NAME, taskName)
            }
        }
    }
}