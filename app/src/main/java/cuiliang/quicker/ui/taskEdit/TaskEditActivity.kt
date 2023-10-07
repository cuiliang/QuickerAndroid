package cuiliang.quicker.ui.taskEdit

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.cuiliang.quicker.ui.BaseDBActivity
import cuiliang.quicker.R
import cuiliang.quicker.databinding.ActivityTaskEditBinding
import cuiliang.quicker.service.TaskManagerService
import cuiliang.quicker.taskManager.Task

class TaskEditActivity : BaseDBActivity<ActivityTaskEditBinding, TaskEditViewModel>(),
    ServiceConnection {

    private var mBinder: TaskManagerService.TaskManagerBinder? = null

    override val mViewModel: TaskEditViewModel by lazy { TaskEditViewModel() }

    override fun getLayoutID(): Int = R.layout.activity_task_edit

    override fun onInit() {
        mBinding.vm=mViewModel
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, TaskManagerService::class.java), this, Service.BIND_AUTO_CREATE)
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
                resultAndFinish(mViewModel.model.task)
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
            mViewModel.title.value = getString(R.string.createTask_str)
            mViewModel.model.task = Task(true)
            return
        }
        val i = intent.getStringExtra(TASK_NAME)
        mViewModel.model.task = mBinder?.getTaskList()?.get(i) ?: Task(true)
        mViewModel.title.value = getString(R.string.editTask_str)
        mViewModel.taskName.value = mViewModel.model.task.name
        mViewModel.refreshAllData()
    }

    private fun resultAndFinish(task: Task) {
        if (!mViewModel.saveData()) return
        mBinder?.getTaskList()?.put(task.name, task)
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(TASK_NAME, task.name)
            putExtra(EDIT_TYPE, editType)
        })
        finish()
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