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
import cuiliang.quicker.ui.taskManager.TaskData
import cuiliang.quicker.util.KLog

/**
 * Created by Voidcom on 2023/9/13 16:49
 * TODO
 */
class TaskListAdapter(private val context: Context, private val callback: ((TaskData) -> Unit)) :
    RecyclerView.Adapter<TaskListAdapter.TaskHolder>() {
    private val taskList = arrayListOf<TaskData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        return TaskHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_task_info, parent, false)
        )
    }

    override fun getItemCount(): Int = taskList.size

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        holder.taskTitle.text = taskList[position].name
        KLog.d(
            "TaskListAdapter",
            "isChecked:${holder.taskSwitch.isChecked}; taskState:${taskList[position].state}"
        )
        holder.taskSwitch.isChecked = taskList[position].state

        holder.itemView.setOnClickListener {
            callback.invoke(taskList[position])
        }
    }

    fun addTask(data: TaskData) {
        if (taskList.contains(data)) return
        taskList.add(data)
        notifyItemChanged(taskList.size - 1)
    }

    inner class TaskHolder(itemView: View) : ViewHolder(itemView) {
        val taskTitle: AppCompatTextView = itemView.findViewById(R.id.taskTitle)
        val taskSubTitle: AppCompatTextView = itemView.findViewById(R.id.taskSubTitle)
        val taskSwitch: SwitchCompat = itemView.findViewById(R.id.taskSwitch)

        init {
            taskSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {

                } else {

                }
            }
        }
    }
}