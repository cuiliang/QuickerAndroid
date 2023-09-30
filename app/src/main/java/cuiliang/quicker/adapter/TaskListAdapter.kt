package cuiliang.quicker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cuiliang.quicker.R
import cuiliang.quicker.taskManager.Task
import cuiliang.quicker.ui.taskManager.TaskList

/**
 * Created by Voidcom on 2023/9/13 16:49
 * TODO
 */
class TaskListAdapter(
    private val context: Context,
    private val taskList: TaskList,
    private val callback: ((Task) -> Unit)
) :
    RecyclerView.Adapter<TaskListAdapter.TaskHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        return TaskHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_task_info, parent, false)
        )
    }

    override fun getItemCount(): Int = taskList.size()

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        holder.setData(taskList.get(position))
    }

    inner class TaskHolder(itemView: View) : ViewHolder(itemView) {
        private val taskTitle: AppCompatTextView = itemView.findViewById(R.id.taskTitle)
        private val taskSubTitle: AppCompatTextView = itemView.findViewById(R.id.taskSubTitle)
        private val taskSwitch: SwitchCompat = itemView.findViewById(R.id.taskSwitch)

        init {
            taskSwitch.setOnCheckedChangeListener { _, isChecked ->
                taskList.get(taskTitle.text.toString()).updateState(context, isChecked)
            }
        }

        fun setData(data: Task) {
            taskTitle.text = data.name
            taskSubTitle.text = data.toDescription()
            taskSwitch.isChecked = data.getIsEnable()
//            KLog.d(
//                "TaskListAdapter",
//                "isChecked:${taskSwitch.isChecked}; taskState:${data.getIsEnable()}"
//            )
            itemView.setOnClickListener {
                //任务Item点击事件
                callback.invoke(data)
            }
        }
    }
}