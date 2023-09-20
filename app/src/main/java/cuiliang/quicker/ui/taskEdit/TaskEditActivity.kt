package cuiliang.quicker.ui.taskEdit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import cuiliang.quicker.taskManager.BaseTaskData
import cuiliang.quicker.ui.EventOrActionActivity
import cuiliang.quicker.ui.taskManager.TaskData
import cuiliang.quicker.ui.taskManager.TaskEditItemData
import cuiliang.quicker.util.GsonUtils

class TaskEditActivity : AppCompatActivity() {
    private lateinit var addEventLauncher: ActivityResultLauncher<Intent>
    private lateinit var ifFactoryAdapter: TaskDetailsItemAdapter
    private lateinit var ifActionAdapter: TaskDetailsItemAdapter

    private lateinit var mBinding: ActivityTaskEditBinding
    private lateinit var taskData: TaskData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityTaskEditBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initData()

        addEventLauncher = EventOrActionActivity.getLauncher(this, addEventCallback)

        ifFactoryAdapter = TaskDetailsItemAdapter(this, taskData.events) {
            val array = Array(it.size) { "" }
            for (i in it.indices) {
                array[i] = it[i].title
            }
            addEventLauncher.launch(EventOrActionActivity.getInstant(this, 0, array))
        }
        ifActionAdapter = TaskDetailsItemAdapter(this, taskData.taskActions) {
            val array = Array(it.size) { "" }
            for (i in it.indices) {
                array[i] = it[i].title
            }
            addEventLauncher.launch(EventOrActionActivity.getInstant(this, 1, array))
        }
        mBinding.rvIfFactorList.adapter = ifFactoryAdapter.apply {
            setFooterData("添加条件", "如：当电量低于20%")
        }
        mBinding.rvIfActionList.adapter = ifActionAdapter.apply {
            setFooterData("添加结果", "例如：发送充电提示通知")
        }
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
                taskData.name = mBinding.inputTaskName.text.toString()
                resultAndFinish(taskData)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initData() {
        val d = intent.getStringExtra(DATA)
        if (editType == 0) {
            mBinding.toolbar.setTitle(R.string.createTask_str)
            taskData = TaskData(true)
            return
        }
        taskData = TaskData.jsonToTaskData(d)
        mBinding.toolbar.setTitle(R.string.editTask_str)
        mBinding.inputTaskName.text = SpannableStringBuilder.valueOf(taskData.name)
    }

    private fun resultAndFinish(taskData: TaskData) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(DATA, GsonUtils.toString(taskData))
            putExtra(EDIT_TYPE, editType)
        })
        finish()
    }

    private val addEventCallback = ActivityResultCallback<ActivityResult> { result ->
        if (result.resultCode != Activity.RESULT_OK || result.data == null) return@ActivityResultCallback
        val event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result.data!!.getParcelableExtra(
                EventOrActionActivity.RESULT_DATA,
                BaseTaskData::class.java
            )
        } else {
            result.data!!.getParcelableExtra(EventOrActionActivity.RESULT_DATA) as BaseTaskData
        }
        val dataType = result.data!!.getIntExtra(EventOrActionActivity.DATA_TYPE, 0)
        if (dataType == 0)
            ifFactoryAdapter.addItem(TaskEditItemData(event))
        else
            ifActionAdapter.addItem(TaskEditItemData(event))
    }

    companion object {
        private var editType: Int = 0 //0:新建任务 1:编辑任务
        const val EDIT_TYPE = "EDIT_TYPE"
        const val DATA = "DATA"

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
         * @param data 当data=null时，表示新增任务；否则是编辑任务
         */
        fun getIntent(context: Context, data: TaskData? = null): Intent {
            editType = if (data == null) 0 else 1
            return Intent(context, TaskEditActivity::class.java).apply {
                if (data != null) putExtra(DATA, data.toString())
            }
        }
    }
}