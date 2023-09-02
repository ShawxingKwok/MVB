package pers.shawxingkwok.mvb.demo.orginalAdapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dylanc.viewbinding.nonreflection.binding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.androidutil.view.onClick
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.mvb.android.observe
import pers.shawxingkwok.mvb.android.rmb
import pers.shawxingkwok.mvb.android.save
import pers.shawxingkwok.mvb.android.saveMutableStateFlow
import pers.shawxingkwok.mvb.demo.R
import pers.shawxingkwok.mvb.demo.StopwatchUtil
import pers.shawxingkwok.mvb.demo.StopwatchUtil.lightGreen
import pers.shawxingkwok.mvb.demo.StopwatchUtil.lightRed
import pers.shawxingkwok.mvb.demo.StopwatchUtil.white
import pers.shawxingkwok.mvb.demo.StopwatchUtil.whiteGrey
import pers.shawxingkwok.mvb.demo.databinding.FragmentMainBinding
import java.util.*
import kotlin.concurrent.timer
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@SuppressLint("SetTextI18n")
class OriginalFragment : Fragment(R.layout.fragment_main) {
    private val binding by binding(FragmentMainBinding::bind)
    private val intervals by save { mutableListOf<Int>() }
    private val adapter by fastLazy { OrginalAdapter(intervals.asReversed()) }

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
            adapter.updateTop()
        }

    private var timer: Timer? = null

    private val isRunning by rmb { MutableStateFlow(false) }
        // update duration and adapter periodically
        .observe {
            if (it)
                timer = timer(period = 10) {
                    duration.value++
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
                tv.setTextColor(lightRed)
            } else {
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
                duration == 0 -> {
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
                        adapter.insertAndScrollUp(binding.rv)
                    }
                }
                // reset
                else ->{
                    tv.setBackgroundResource(R.drawable.circle_light_grey)
                    tv.text = "Reset"
                    tv.setTextColor(white)
                    tv.isClickable = true
                    tv.onClick {
                        this@OriginalFragment.duration.value = 0
                        adapter.reset()
                    }
                }
            }
        }
    //endregion

    private fun setFixedListeners(){
        binding.tvRight.onClick {
            isRunning.update { !it }
        }
        lifecycleScope.launch {
            delay(300)
            binding.tvRight.performClick()
            repeat(1000){
                delay(150)
                binding.tvLeft.performClick()
            }
        }
    }
}