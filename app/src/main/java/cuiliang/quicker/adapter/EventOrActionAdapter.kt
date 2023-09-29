package cuiliang.quicker.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import cuiliang.quicker.R
import cuiliang.quicker.taskManager.BaseEventOrAction

class EventOrActionAdapter(
    private val context: Context,
    private val callback: ((BaseEventOrAction) -> Unit)
) :
    RecyclerView.Adapter<EventOrActionAdapter.TaskHolder>() {
    private val eventList = arrayListOf<BaseEventOrAction>()
    private var unableAddArray = arrayOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        return TaskHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_event_item, parent, false)
        )
    }

    override fun getItemCount(): Int = eventList.size

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        holder.setData(eventList[position])
    }

    /**
     * 设置不可添加列表
     */
    fun setUnableAddList(array: Array<String>) {
        this.unableAddArray = array
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setEvents(list: List<BaseEventOrAction>) {
        eventList.clear()
        eventList.addAll(list)
        notifyDataSetChanged()
    }

    inner class TaskHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventIcon: AppCompatImageView = itemView.findViewById(R.id.ivIcon)
        private val eventTitle: AppCompatTextView = itemView.findViewById(R.id.tvTitle)

        fun setData(data: BaseEventOrAction) {
            eventTitle.text = data.getName()
            Glide.with(context).load(data.getIcon()).into(eventIcon)

            //检查这个item是否已经添加，如果添加了就设置为不可点击
            for (u in unableAddArray.iterator()) {
                if (data.getName() == u) {
                    eventTitle.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.event_unableClick
                        )
                    )
                    itemView.isEnabled = false
                    itemView.isClickable = false
                    return
                }
            }
            itemView.setOnClickListener {
                data.showDialogAndCallback(context, callback)
            }
        }
    }
}
