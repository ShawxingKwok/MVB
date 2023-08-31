package pers.shawxingkwok.mvb.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dylanc.viewbinding.nonreflection.binding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import pers.shawxingkwok.androidutil.view.KRecyclerViewAdapter
import pers.shawxingkwok.androidutil.view.onClick
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.ktutil.updateIf
import pers.shawxingkwok.mvb.android.observe
import pers.shawxingkwok.mvb.android.rmb
import pers.shawxingkwok.mvb.android.saveMutableStateFlow
import pers.shawxingkwok.mvb.demo.databinding.FragmentMainBinding
import pers.shawxingkwok.mvb.demo.databinding.ItemIntervalBinding
import java.util.*
import kotlin.concurrent.timer

@SuppressLint("SetTextI18n")
class MainFragment : Fragment(R.layout.fragment_main) {
    private val binding by binding(FragmentMainBinding::bind)

    //region colors
    private val whiteGrey by fastLazy { getColor(resources, R.color.white_grey, null) }
    private val lightRed by fastLazy { getColor(resources, R.color.light_red, null) }
    private val lightGreen by fastLazy { getColor(resources, R.color.light_green, null) }
    private val white by fastLazy { getColor(resources, R.color.white, null) }
    //endregion

    private fun formatDuration(duration: Long): String{
        val percentSec = duration % 100
        var sec = duration / 100
        val min = sec / 60
        sec %= 60

        // The situation of an hour more is not considered.
        val (minText, secText, percentSecondText) =
            listOf(min, sec, percentSec)
            .map { i -> if (i < 10) "0$i" else "$i" }

        return "$minText:$secText.$percentSecondText"
    }

    private val adapter = object : KRecyclerViewAdapter(){
        var intervals = longArrayOf()

        override fun arrangeHolderBinders() {
            val comparedIntervals = intervals.dropLast(1)
            val max = comparedIntervals.maxOrNull()
            val min = comparedIntervals.minOrNull()

            intervals.reversed().forEachIndexed { i, l ->
                val id = intervals.size - i

                val textColor =
                    when{
                        i == 0 || intervals.size <= 2 -> white
                        l == max -> lightRed
                        l == min -> lightGreen
                        else -> white
                    }

                HolderBinder(
                    inflate = ItemIntervalBinding::inflate,
                    id = id,
                    contentId = l to textColor
                ) {
                    it.binding.id.text = "Lap $id"
                    it.binding.interval.text = formatDuration(l)
                    it.binding.id.setTextColor(textColor)
                    it.binding.interval.setTextColor(textColor)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        setFixedListeners()
    }

    //region data bridge
    private var duration by saveMutableStateFlow { 0L }
        .observe {
            binding.tvDuration.text = formatDuration(it)
        }

    private val intervals by saveMutableStateFlow{ longArrayOf() }
        .observe { intervals ->
            val sizeChanged = intervals.size != adapter.intervals.size
            adapter.intervals = intervals
            adapter.update(sizeChanged)
            if (sizeChanged) {
                val layoutManager = binding.rv.layoutManager as LinearLayoutManager
                val topVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                    .updateIf({ it == -1 }){ 0 }
                binding.rv.scrollToPosition(topVisiblePosition)
            }
        }

    private var timer: Timer? = null

    private val isRunning by rmb { MutableStateFlow(false) }
        .observe {
            val tv = binding.tvRight

            if (it) {
                timer = timer(period = 10){
                    duration.value++

                    intervals.update { arr ->
                        val newArr = if (arr.none()) longArrayOf(0) else arr.clone()
                        newArr[newArr.lastIndex]++
                        newArr
                    }
                }
                tv.text = "Stop"
                tv.setBackgroundResource(R.drawable.circle_dark_red)
                tv.setTextColor(lightRed)
            } else {
                timer?.cancel()
                tv.text = "Start"
                tv.setBackgroundResource(R.drawable.circle_dark_green)
                tv.setTextColor(lightGreen)
            }
        }

    @Suppress("unused")
    private val tvLeftState by rmb { combine(duration, isRunning){ a, b -> a to b } }
        .observe { (duration, isRunning) ->
            val tv = binding.tvLeft
            when{
                // disabled
                duration == 0L -> {
                    tv.setBackgroundResource(R.drawable.circle_dark_grey)
                    tv.text = "Lap"
                    tv.setTextColor(whiteGrey)
                    tv.isClickable = false
                }
                // lap
                isRunning -> {
                    tv.setBackgroundResource(R.drawable.circle_light_grey)
                    tv.text = "Lap"
                    tv.setTextColor(white)
                    tv.isClickable = true
                    tv.onClick {
                        intervals.update { it + 0 }
                    }
                }
                // reset
                else ->{
                    tv.setBackgroundResource(R.drawable.circle_light_grey)
                    tv.text = "Reset"
                    tv.setTextColor(white)
                    tv.isClickable = true
                    tv.onClick {
                        this@MainFragment.duration.value = 0
                        this@MainFragment.intervals.value = longArrayOf()
                    }
                }
            }
        }
    //endregion

    private fun setFixedListeners(){
        binding.tvRight.onClick {
            isRunning.update { !it }
        }
    }
}