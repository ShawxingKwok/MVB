package pers.shawxingkwok.mvb.demo

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dylanc.viewbinding.nonreflection.binding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pers.shawxingkwok.mvb.android.process
import pers.shawxingkwok.mvb.android.mvbScope
import pers.shawxingkwok.mvb.android.observe
import pers.shawxingkwok.mvb.android.save
import pers.shawxingkwok.androidutil.view.onClick
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.mvb.demo.databinding.FragmentMainBinding

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainFragment : Fragment(R.layout.fragment_main) {
    private val binding by binding(FragmentMainBinding::bind)
    private val msgAdapter: MsgAdapter by fastLazy(::MsgAdapter)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rv.run {
            adapter = msgAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        onClicks()
    }

    private val msgsFlow by
        save { MutableStateFlow(emptyArray<Msg>()) }
        .process(
            convert = { it.value },
            getFromBundle = { bundle, key ->
                MutableStateFlow(bundle.getParcelableArray(key, Msg::class.java)!!)
            }
        )
        .observe {
            msgAdapter.msgs = it

            msgAdapter.update {
                binding.rv.scrollToPosition(msgAdapter.itemCount - 1)
                binding.etMsg.text.clear()
            }
        }

    private fun onClicks(){
        binding.btnSend.onClick {
            val text = binding.etMsg.text.toString()
            val greeting = Msg(0, true, text)
            msgsFlow.update { it + greeting }

            if (text == "How are you")
                mvbScope.launch {
                    delay(1000)
                    val reply = Msg(1, false, "Good")
                    msgsFlow.update { it + reply }
                }
        }
    }
}