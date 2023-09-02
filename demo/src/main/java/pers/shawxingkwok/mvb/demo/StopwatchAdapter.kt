package pers.shawxingkwok.mvb.demo

import android.annotation.SuppressLint
import pers.shawxingkwok.androidutil.view.KRecyclerViewAdapter
import pers.shawxingkwok.mvb.demo.databinding.ItemIntervalBinding

class StopwatchAdapter : KRecyclerViewAdapter(){
    var intervals = intArrayOf()

    @SuppressLint("SetTextI18n")
    override fun arrangeHolderBinders() {
        val (max, min) = intervals.drop(1).run { maxOrNull() to minOrNull() }

        intervals.forEachIndexed { i, interval ->
            val id = intervals.size - i

            val textColor = when {
                i == 0 || intervals.size <= 2 || max == min -> StopwatchUtil.white
                interval == max -> StopwatchUtil.lightRed
                interval == min -> StopwatchUtil.lightGreen
                else -> StopwatchUtil.white
            }

            HolderBinder(
                inflate = ItemIntervalBinding::inflate,
                id = id,
                contentId = interval to textColor,
            ){ holder, oldContentId ->
                // init fixed parts
                if (oldContentId == null)
                    holder.binding.id.text = "Lap $id"

                // init or update
                if (interval != oldContentId?.first)
                    holder.binding.interval.text = StopwatchUtil.formatDuration(interval)

                if (textColor != oldContentId?.second) {
                    holder.binding.id.setTextColor(textColor)
                    holder.binding.interval.setTextColor(textColor)
                }
            }
        }
    }
}