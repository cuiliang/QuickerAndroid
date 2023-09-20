package cuiliang.quicker.ui.taskManager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import cuiliang.quicker.R
import cuiliang.quicker.adapter.TaskListAdapter
import cuiliang.quicker.databinding.FragmentMyTaskBinding
import cuiliang.quicker.taskManager.ActionWebSocketMsg
import cuiliang.quicker.taskManager.EventBatteryStatus
import cuiliang.quicker.taskManager.EventWebSocket
import cuiliang.quicker.ui.taskEdit.TaskEditActivity
import cuiliang.quicker.util.FileTools
import cuiliang.quicker.util.KLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyTaskFragment : Fragment() {
    private lateinit var mBinding: FragmentMyTaskBinding
    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var goTaskEditLauncher: ActivityResultLauncher<Intent>
    private val taskList = TaskList()

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
        taskListAdapter = TaskListAdapter(requireActivity(), taskList) {
            openEditPageAndAddNewTask(it)
        }
        mBinding.taskListRv.adapter = taskListAdapter
        goTaskEditLauncher = TaskEditActivity.getLauncher(requireActivity(), editTaskCallback)
        if (taskList.isEmpty()) {
            GlobalScope.launch {
                readFileConfig()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        //在这里保存数据到文件
        GlobalScope.launch {
            taskList.forEach {
                FileTools.saveJsonToFile(requireContext(), it.name, it.toString())
            }
        }
    }

    /**
     * 打开任务编辑界面
     * @param data 创建一个新的任务，因为没有传data，所以操作类型为‘新建任务’
     */
    fun openEditPageAndAddNewTask(data: TaskData? = null) {
        goTaskEditLauncher.launch(TaskEditActivity.getIntent(requireContext(), data))
    }

    private suspend fun readFileConfig() = coroutineScope {
        val files = FileTools.getFileList(requireContext())
        //判断是否有配置文件
        if (files.isNullOrEmpty()) {
            //没有配置文件，直接加载默认配置。finish Activity时会保存配置，然后这份默认不论有没有被修改都会变成配置文件
            defaultTaskConfig().forEach {
                val index = taskList.indexOf(it)
                if (index == -1) taskList.add(it) else taskList[index] = it
            }
        } else {
            files.forEach {
                val tmp = TaskData.jsonToTaskData(FileTools.readJsonFromFile(it))
                val index = taskList.indexOf(tmp)
                if (index == -1) taskList.add(tmp) else taskList[index] = tmp
            }
        }
        withContext(Dispatchers.Main) {
            taskListAdapter.notifyItemRangeChanged(0, taskList.size)
        }
    }

    private val editTaskCallback = ActivityResultCallback<ActivityResult> { result ->
        if (result.resultCode != Activity.RESULT_OK || result.data == null) return@ActivityResultCallback
        val json = result.data!!.getStringExtra(TaskEditActivity.DATA)
        KLog.d("MyTaskFragment", "json:$json")
        val tmp = TaskData.jsonToTaskData(json)
        val index = taskList.indexOf(tmp)
        if (index == -1) taskList.add(tmp) else taskList[index] = tmp
        taskListAdapter.notifyItemChanged(taskList.size - 1)
    }

    private fun defaultTaskConfig(): List<TaskData> {
        val list = arrayListOf<TaskData>()
        list.add(TaskData(true).apply {
            name = "WebSocket连接通知"
            events.add(TaskEditItemData(EventWebSocket()))
            taskActions.add(TaskEditItemData(ActionWebSocketMsg("WebSocket连接成功!")))
        })

        list.add(TaskData(true).apply {
            name = "充电通知"
            events.add(TaskEditItemData(EventBatteryStatus(resources.getStringArray(R.array.dialog_battery_status_list)[2])))
            taskActions.add(TaskEditItemData(ActionWebSocketMsg("快没电了！")))
        })
        return list
    }

    class TaskList : ArrayList<TaskData>() {
        override fun indexOf(element: TaskData): Int {
            for (i in 0 until size) {
                if (element.name == this[i].name)
                    return i
            }
            return -1
        }
    }
}