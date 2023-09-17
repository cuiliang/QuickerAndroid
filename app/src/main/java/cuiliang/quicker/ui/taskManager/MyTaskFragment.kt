package cuiliang.quicker.ui.taskManager

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import cuiliang.quicker.adapter.TaskListAdapter
import cuiliang.quicker.databinding.FragmentMyTaskBinding
import cuiliang.quicker.ui.taskEdit.TaskEditActivity

class MyTaskFragment : Fragment() {
    private lateinit var mBinding: FragmentMyTaskBinding
    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var goTaskEditLauncher: ActivityResultLauncher<Intent>


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
        taskListAdapter = TaskListAdapter(requireActivity()) { data: EventData ->
            goTaskEditLauncher.launch(TaskEditActivity.getIntent(requireContext(), data))
        }
        mBinding.taskListRv.adapter = taskListAdapter
        readFileConfig()
        goTaskEditLauncher = TaskEditActivity.getLauncher(requireActivity(), editTaskCallback)
    }

    /**
     * 打开任务编辑界面创建一个新的任务，因为没有传data，所以操作类型为‘新建任务’
     */
    fun openEditPageAndAddNewTask() {
        goTaskEditLauncher.launch(TaskEditActivity.getIntent(requireContext()))
    }

    private fun readFileConfig() {
        //判断是否有配置文件
        //没有时不使用配置文件，直接加载默认配置。finish Activity时会保存配置，然后这份默认不论有没有被修改都会变成配置文件
        taskListAdapter.addTask(EventData("WebSocket通知", "WebSocket连接成功通知", true))
        taskListAdapter.addTask(EventData("充电通知", "电量低于20%", false))
    }

    private val editTaskCallback = ActivityResultCallback<ActivityResult> { result ->
        //todo 添加task结果回调
    }

}