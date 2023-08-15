package pers.shawxingkwok.mvb.demo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dylanc.viewbinding.nonreflection.binding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pers.shawxingkwok.androidutil.view.onClick
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.mvb.*
import pers.shawxingkwok.mvb.demo.databinding.FragmentMainBinding

val flow = flowOf(1)

class MainFragment : Fragment(R.layout.fragment_main) {
    private val s by ::flow

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

    override fun onSaveInstanceState(outState: Bundle) {
        enableMVBSave()
        super.onSaveInstanceState(outState)
    }

    private val msgsFlow by
        save (
            put = Bundle::putParcelableArray,
            initialize = { MutableStateFlow(emptyArray<Msg>()) },
            convert = { it.value },
            recover = ::MutableStateFlow,
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