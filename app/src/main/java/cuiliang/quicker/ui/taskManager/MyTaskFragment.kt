package cuiliang.quicker.ui.taskManager

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import cuiliang.quicker.adapter.TaskListAdapter
import cuiliang.quicker.databinding.FragmentMyTaskBinding
import cuiliang.quicker.service.TaskManagerService
import cuiliang.quicker.ui.taskEdit.TaskEditActivity
import cuiliang.quicker.util.KLog

class MyTaskFragment : Fragment(), ServiceConnection {
    private lateinit var mBinding: FragmentMyTaskBinding
    private lateinit var goTaskEditLauncher: ActivityResultLauncher<Intent>
    private var mBinder: TaskManagerService.TaskManagerBinder? = null
    private var taskListAdapter: TaskListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentMyTaskBinding.inflate(inflater).run {
            mBinding = this
            this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        goTaskEditLauncher = TaskEditActivity.getLauncher(requireActivity(), editTaskCallback)
    }

    override fun onStart() {
        super.onStart()
        requireActivity().bindService(
            Intent(requireContext(), TaskManagerService::class.java),
            this,
            Service.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        super.onStop()
        mBinder?.getTaskList()?.saveConfig()
        requireActivity().unbindService(this)
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        KLog.d(TAG, "---onServiceConnected")
        if (service == null) return
        mBinder = service as TaskManagerService.TaskManagerBinder
        mBinder?.let { binder ->
            taskListAdapter = TaskListAdapter(requireActivity(), binder.getTaskList()) {
                openEditPageAndAddNewTask(it.name)
            }
            mBinding.taskListRv.adapter = taskListAdapter
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        mBinder = null
        taskListAdapter = null
    }

    /**
     * 打开任务编辑界面
     * @param name 创建一个新的任务，因为没有传data，所以操作类型为‘新建任务’
     */
    fun openEditPageAndAddNewTask(name: String? = null) {
        goTaskEditLauncher.launch(TaskEditActivity.getIntent(requireContext(), name))
    }

    private val editTaskCallback = ActivityResultCallback<ActivityResult> { result ->
        if (result.resultCode != Activity.RESULT_OK || result.data == null) return@ActivityResultCallback
        val name = result.data!!.getStringExtra(TaskEditActivity.TASK_NAME)
//        KLog.d("MyTaskFragment", "json:$name")
        mBinder?.getTaskList()?.let {
            it.saveConfig()
            taskListAdapter?.notifyItemChanged(it.indexOf(name))
        }
    }

    companion object {
        const val TAG = "MyTaskFragment"
    }
}