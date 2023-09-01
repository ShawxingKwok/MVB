package pers.shawxingkwok.mvb.demo

import android.annotation.SuppressLint
import android.os.Parcelable
import android.provider.SyncStateContract.Helpers.update
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.parcelize.Parcelize
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.androidutil.view.KRecyclerViewAdapter
import pers.shawxingkwok.ktutil.updateIf
import pers.shawxingkwok.mvb.demo.databinding.ItemIntervalBinding

// TODO 考虑 min = max 的情况
class StopwatchAdapter(private val intervals: MutableList<Int>) : RecyclerView.Adapter<KRecyclerViewAdapter.ViewBindingHolder<ItemIntervalBinding>>() {
    private var max = 0
    private var min = 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): KRecyclerViewAdapter.ViewBindingHolder<ItemIntervalBinding> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemIntervalBinding.inflate(inflater, parent, false)
        return KRecyclerViewAdapter.ViewBindingHolder(binding)
    }

    override fun getItemCount(): Int {
        if (max == 0 && min == 0 && intervals.size >= 3){
            val topOmitted = intervals.drop(1)
            max = topOmitted.max()
            min = topOmitted.min()
        }
        return intervals.size
    }

    override fun onBindViewHolder(holder: KRecyclerViewAdapter.ViewBindingHolder<ItemIntervalBinding>, position: Int) {
        val id = intervals.size - position
        val interval = intervals[position]

        val textColor =
            when {
                position == 0 || intervals.size <= 2 -> StopwatchUtil.white
                interval == max -> StopwatchUtil.lightRed
                interval == min -> StopwatchUtil.lightGreen
                else -> StopwatchUtil.white
            }

        holder.binding.interval.text = StopwatchUtil.formatDuration(interval)
        holder.binding.id.text = "Lap $id"
        holder.binding.id.setTextColor(textColor)
        holder.binding.interval.setTextColor(textColor)
    }

    override fun onBindViewHolder(
        holder: KRecyclerViewAdapter.ViewBindingHolder<ItemIntervalBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        if (position == 0)
            holder.binding.interval.text = StopwatchUtil.formatDuration(intervals.first())
        else {
            holder.binding.id.setTextColor(StopwatchUtil.white)
            holder.binding.interval.setTextColor(StopwatchUtil.white)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun insertAndScrollUp(rv: RecyclerView) {
        intervals.add(0, 0)

        when (intervals.size) {
            1, 2 -> notifyItemInserted(0)

            // also change colors
            3 -> notifyDataSetChanged()

            else -> {
                val interval = intervals[1]
                val dest = when {
                    interval < min -> min.also { min = interval }
                    interval > max -> max.also { max = interval }
                    else -> null
                }
                notifyItemInserted(0)

                if (dest != null) {
                    notifyItemChanged(1, "")

                    for (i in 2..intervals.lastIndex) {
                        if (intervals[i] == dest)
                            notifyItemChanged(i).also { KLog.d("update item at position $i, the value is $dest") }
                    }
                }
            }
        }

        val layoutManager = rv.layoutManager as LinearLayoutManager
        val topVisiblePosition = layoutManager.findFirstVisibleItemPosition()
            .updateIf({ it == -1 }) { 0 }
        rv.scrollToPosition(topVisiblePosition)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reset(){
        intervals.clear()
        max = 0
        min = 0
        notifyDataSetChanged()
    }

    fun updateTop(){
        if (intervals.none()) {
            intervals += 1
            notifyItemInserted(0)
        } else {
            intervals[0]++
            notifyItemChanged(0, "")
        }
    }
}