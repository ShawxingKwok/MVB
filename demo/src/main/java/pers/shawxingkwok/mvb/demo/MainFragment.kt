package pers.shawxingkwok.mvb.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dylanc.viewbinding.nonreflection.binding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.androidutil.view.KRecyclerViewAdapter
import pers.shawxingkwok.androidutil.view.onClick
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.mvb.android.*
import pers.shawxingkwok.mvb.demo.databinding.FragmentMainBinding
import pers.shawxingkwok.mvb.demo.databinding.ItemRecordBinding
import java.util.*
import kotlin.concurrent.timer

@SuppressLint("SetTextI18n")
class MainFragment : Fragment(R.layout.fragment_main) {
    //region colors
    private val whiteGrey by fastLazy { getColor(resources, R.color.white_grey, null) }
    private val lightGrey by fastLazy { getColor(resources, R.color.light_grey, null) }
    private val darkGrey by fastLazy { getColor(resources, R.color.dark_grey, null) }
    private val lightRed by fastLazy { getColor(resources, R.color.light_red, null) }
    private val darkRed by fastLazy { getColor(resources, R.color.dark_red, null) }
    private val lightGreen by fastLazy { getColor(resources, R.color.light_green, null) }
    private val darkGreen by fastLazy { getColor(resources, R.color.dark_green, null) }
    private val white by fastLazy { getColor(resources, R.color.white, null) }
    //endregion
    private val binding by binding(FragmentMainBinding::bind)
    private val adapter = object : KRecyclerViewAdapter(){
        var records = longArrayOf()

        override fun registerProcessRequiredHolderCreators() {}

        override fun arrangeHolderBinders() {
            // also sort to determine colors
            records.reversed().forEachIndexed { i, l ->
                val id = records.size - i
                HolderBinder(
                    inflate = ItemRecordBinding::inflate,
                    id = id,
                    contentId = l
                ){
                    it.binding.id.text = "Lap $id"
                    it.binding.id.setTextColor(white)
                    // TODO
                    it.binding.interval.text = "$l"
                    it.binding.interval.setTextColor(white)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        setListeners()
    }

    //region data bridge
    private var duration by saveMutableStateFlow { 0L }
        .observe {
            val decupleMilli = it % 100
            var sec = it / 100
            val min = sec / 60
            sec %= 60
            // The situation of an hour more is not considered.
            binding.tvDuration.text =
                listOf(min, sec, decupleMilli)
                .map { i -> if (i < 10) "0$i" else "$i" }
                .let { (min, sec, decupleMilli) -> "$min:$sec.$decupleMilli" }
        }

    private val records by saveMutableStateFlow{ longArrayOf() }
        .observe {
            adapter.records = it
            adapter.update{
                binding.rv.scrollToPosition(0)
            }
        }

    private var timer: Timer? = null

    private val isRunning by rmb { MutableStateFlow(false) }
        .observe {
            if (it) {
                timer = timer(period = 10){
                    duration.value++

                    records.update { arr ->
                        val newArr = if (arr.none()) longArrayOf(0) else arr.copyOf()
                        newArr[newArr.lastIndex]++
                        newArr
                    }
                }
                val tv = binding.tvRight
                tv.text = "Stop"
                tv.setBackgroundResource(R.drawable.circle_dark_red)
                tv.setTextColor(lightRed)
            } else {
                timer?.cancel()
                val tv = binding.tvRight
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
                        records.update { it + 0 }
                    }
                }
                // reset
                else ->{
                    tv.setBackgroundResource(R.drawable.circle_light_grey)
                    tv.text = "Reset"
                    tv.setTextColor(white)
                    tv.isClickable = true
                    tv.onClick {
                        this.duration.value = 0
                        this.records.value = longArrayOf()
                    }
                }
            }
        }
    //endregion

    private fun setListeners(){
        binding.tvRight.onClick {
            isRunning.update { !it }
        }
    }
}