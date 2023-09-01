package pers.shawxingkwok.mvb.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dylanc.viewbinding.nonreflection.binding
import kotlinx.coroutines.flow.*
import pers.shawxingkwok.androidutil.view.onClick
import pers.shawxingkwok.ktutil.updateIf
import pers.shawxingkwok.mvb.android.observe
import pers.shawxingkwok.mvb.android.rmb
import pers.shawxingkwok.mvb.android.saveMutableStateFlow
import pers.shawxingkwok.mvb.demo.databinding.FragmentMainBinding
import java.util.*
import kotlin.concurrent.timer

@SuppressLint("SetTextI18n")
class StopwatchFragment : Fragment(R.layout.fragment_main) {
    private val binding by binding(FragmentMainBinding::bind)
    private val adapter = StopwatchAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        setFixedListeners()
    }

    //region data bridge
    private var duration by saveMutableStateFlow { 0 }
        .observe {
            binding.tvDuration.text = StopwatchUtil.formatDuration(it)
        }

    private val intervals by saveMutableStateFlow { intArrayOf() }
        .observe {
            val sizeChanged = it.size != adapter.intervals.size
            adapter.intervals = it
            adapter.update(sizeChanged)
        }

    private var timer: Timer? = null

    private val isRunning by rmb { MutableStateFlow(false) }
        // update duration and adapter periodically
        .observe {
            if (it)
                timer = timer(period = 10) {
                    duration.value++

                    if (intervals.value.none())
                        intervals.value = intArrayOf(0)
                    else
                        intervals.value = intervals.value.clone()

                    intervals.value[0]++
                }
            else
                timer?.cancel()
        }
        // update button stop/start
        .observe {
            val tv = binding.tvRight

            if (it) {
                tv.text = "Stop"
                tv.setBackgroundResource(R.drawable.circle_dark_red)
                tv.setTextColor(StopwatchUtil.lightRed)
            } else {
                tv.text = "Start"
                tv.setBackgroundResource(R.drawable.circle_dark_green)
                tv.setTextColor(StopwatchUtil.lightGreen)
            }
        }

    @Suppress("unused")
    private val tvLeftState by rmb { combine(duration, isRunning){ a, b -> a to b } }
        .observe { (duration, isRunning) ->
            val tv = binding.tvLeft
            when{
                // disabled
                duration == 0 -> {
                    tv.setBackgroundResource(R.drawable.circle_dark_grey)
                    tv.text = "Lap"
                    tv.setTextColor(StopwatchUtil.whiteGrey)
                    tv.isClickable = false
                }
                // lap
                isRunning -> {
                    tv.setBackgroundResource(R.drawable.circle_light_grey)
                    tv.text = "Lap"
                    tv.setTextColor(StopwatchUtil.white)
                    tv.isClickable = true
                    tv.onClick {
                        intervals.update { intArrayOf(0) + it }

                        val layoutManager = binding.rv.layoutManager as LinearLayoutManager
                        val topVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                            .updateIf({ it == -1 }) { 0 }
                        binding.rv.scrollToPosition(topVisiblePosition)
                    }
                }
                // reset
                else ->{
                    tv.setBackgroundResource(R.drawable.circle_light_grey)
                    tv.text = "Reset"
                    tv.setTextColor(StopwatchUtil.white)
                    tv.isClickable = true
                    tv.onClick {
                        this@StopwatchFragment.duration.value = 0
                        intervals.value = intArrayOf()
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