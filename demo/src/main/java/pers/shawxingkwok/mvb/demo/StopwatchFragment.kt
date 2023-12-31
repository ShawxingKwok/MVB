package pers.shawxingkwok.mvb.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dylanc.viewbinding.nonreflection.binding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
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
    //region static processing: binding, adapter
    private val binding by binding(FragmentMainBinding::bind)
    private val adapter = StopwatchAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        setFixedListeners()
    }
    //endregion

    //region bridge: duration, intervals, isRunning
    private val duration by saveMutableStateFlow { 0 }
        .observe {
            binding.tvDuration.text = StopwatchUtil.formatDuration(it)
        }

    private val intervals by saveMutableStateFlow { intArrayOf() }
        .observe {
            val sizeChanged = it.size != adapter.intervals.size
            adapter.intervals = it
            adapter.update(sizeChanged)
        }

    private var timer: Timer? by rmb{ null }

    // `isRunning` is not saved because it's best to be false when the app is restored.
    private val isRunning by rmb { MutableStateFlow(false) }
        // if true, update duration and intervals periodically
        .observe {
            when{
                !it -> {
                    timer?.cancel()
                    timer = null
                }
                timer == null ->
                    timer = timer(period = 10) {
                        duration.value++

                        intervals.value =
                            if (intervals.value.none())
                                 intArrayOf(1)
                            else
                                intervals.value.clone().also { clone -> clone[0]++ }
                    }
                // else -> do nothing if recovered after onStop
            }
        }
        // switch button stop/start
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

    // suppress because tvLeftState is hinted 'unused' though it's actually observed.
    // this problem could be fixed via lint in the future.
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
                    // This OnClickListener is variable, which also explains why there is a
                    // function `setFixedListeners` at last.
                    tv.onClick { _ ->
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

    private fun setFixedListeners() {
        binding.tvRight.onClick {
            isRunning.update { !it }
        }
    }

    /* or separate actions
    //region fixed actions
    private fun setFixedListeners(){
        binding.tvRight.onClick { switchIsRunning() }
        // other fixed actions could also be put here
    }

    private fun switchIsRunning(){
        isRunning.update { !it }
    }
    //endregion
    */
}