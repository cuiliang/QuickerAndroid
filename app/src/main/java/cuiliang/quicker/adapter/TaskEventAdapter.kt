package cuiliang.quicker.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import cuiliang.quicker.R
import cuiliang.quicker.taskEvent.Event

class TaskEventAdapter(private val context: Context, private val callback: ((String) -> Unit)) :
    RecyclerView.Adapter<TaskEventAdapter.TaskHolder>() {
    private val eventList = arrayListOf<Event>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        return TaskHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_event_item, parent, false)
        )
    }

    override fun getItemCount(): Int = eventList.size

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        holder.setData(eventList[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setEvents(list: List<Event>) {
        eventList.clear()
        eventList.addAll(list)
        notifyDataSetChanged()
    }

    inner class TaskHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventIcon: AppCompatImageView = itemView.findViewById(R.id.ivIcon)
        private val eventTitle: AppCompatTextView = itemView.findViewById(R.id.tvTitle)

        fun setData(data: Event) {
            eventTitle.text = data.getName()
            Glide.with(context).load(data.getIcon()).into(eventIcon)
            itemView.setOnClickListener {
                data.getDialog {
                    callback("")
                }.show()
            }
        }
    }
}
