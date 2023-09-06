package pers.shawxingkwok.mvb.demo.orginalAdapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pers.shawxingkwok.androidutil.view.KRecyclerViewAdapter
import pers.shawxingkwok.ktutil.updateIf
import pers.shawxingkwok.mvb.demo.StopwatchUtil
import pers.shawxingkwok.mvb.demo.databinding.ItemIntervalBinding

class OriginalAdapter(private val intervals: MutableList<Int>) : RecyclerView.Adapter<KRecyclerViewAdapter.ViewBindingHolder<ItemIntervalBinding>>() {
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
                position == 0 || intervals.size <= 2 || max == min -> StopwatchUtil.white
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
        when{
            payloads.none() -> onBindViewHolder(holder, position)
            position == 0 -> holder.binding.interval.text = StopwatchUtil.formatDuration(intervals.first())
            else -> {
                holder.binding.id.setTextColor(StopwatchUtil.white)
                holder.binding.interval.setTextColor(StopwatchUtil.white)
            }
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

                if (interval == min || interval == max)
                    notifyItemChanged(1)

                if (dest != null) {
                    for (i in 2..intervals.lastIndex) {
                        if (intervals[i] == dest)
                            // set text color white
                            notifyItemChanged(i, "")
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