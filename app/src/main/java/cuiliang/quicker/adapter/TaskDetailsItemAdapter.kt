package cuiliang.quicker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import cuiliang.quicker.R
import cuiliang.quicker.taskManager.BaseEventOrAction
import cuiliang.quicker.util.KLog
import cuiliang.quicker.util.gone
import cuiliang.quicker.util.visible

/**
 * Created by Voidcom on 2023/9/15 18:30
 * TODO
 */
class TaskDetailsItemAdapter<T : BaseEventOrAction>(
    private val context: Context,
) : RecyclerView.Adapter<TaskDetailsItemAdapter<T>.ItemViewHolder>() {
    private val items: ArrayList<T> = arrayListOf()
    private var callback: ((List<T>) -> Unit)? = null
    private var footerViewData: T? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_task_details_item, parent, false)
        )
    }

    override fun getItemCount(): Int = items.size + 1
    override fun getItemViewType(position: Int): Int {
        return if (position < items.size) TYPE_INVALID else TYPE_FOOTER
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (TYPE_FOOTER == getItemViewType(position)) {
            footerViewData?.let { holder.setData(it, TYPE_FOOTER) }
        } else {
            KLog.d("FactorItemAdapter", "position:$position; data:${items[position]}")
            holder.setData(items[position], TYPE_INVALID)
        }
    }

    fun addItem(data: T) {
        items.add(data)
        notifyItemChanged(items.size - 1)
    }

    fun addItems(array: List<T>) {
        items.clear()
        items.addAll(array)
        notifyItemRangeChanged(0, itemCount)
    }

    fun removeItem(i: Int) {
        items.removeAt(i)
        notifyItemRemoved(i)
    }

    fun setFooterData(data: T) {
        this.footerViewData = data
        notifyItemChanged(itemCount - 1)
    }

    fun setCallback(c: (List<T>) -> Unit) {
        this.callback=c
    }

    inner class ItemViewHolder(v: View) : ViewHolder(v), OnClickListener,
        OnLongClickListener {
        private var rootView: RelativeLayout = v.findViewById(R.id.rootView)
        private var ivIcon: AppCompatImageView = v.findViewById(R.id.ivIcon)
        private var ivClose: AppCompatImageView = v.findViewById(R.id.ivClose)
        private var tvTitle: AppCompatTextView = v.findViewById(R.id.tvTitle)
        private var tvSubTitle: AppCompatTextView = v.findViewById(R.id.tvSubTitle)

        fun setData(d: T, type: Int) {
            tvTitle.text = d.getName()
            if (d.resultStr().isEmpty()) {
                tvSubTitle.gone()
            } else {
                tvSubTitle.text = d.resultStr()
            }
            Glide.with(context).load(d.getIcon()).into(ivIcon)
            if (TYPE_INVALID == type) {
                ivClose.setOnClickListener(this)
                ivClose.visible()
                rootView.onLongClickListener = this
            } else {
                ivClose.gone()
                rootView.setOnClickListener(this)
            }
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.rootView -> callback?.invoke(items)
                R.id.ivClose -> removeItem(layoutPosition)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            when (v?.id) {
                R.id.rootView -> {
                    Snackbar.make(v, "还没做好", Snackbar.LENGTH_LONG).show()
                }
            }
            return true
        }
    }

    companion object {
        private const val TYPE_INVALID = 0
        private const val TYPE_FOOTER = 1
    }
}