package pers.shawxingkwok.myapplication

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dylanc.viewbinding.nonreflection.binding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pers.shawxingkwok.androidutil.view.demo.databinding.FragmentMainBinding
import pers.shawxingkwok.androidutil.view.onClick
import pers.shawxingkwok.androidutil.view.withView
import pers.shawxingkwok.mvb.mvbScope
import pers.shawxingkwok.mvb.rmb

class MVBFragment : Fragment(R.layout.fragment_main) {
    //region static processing
    private val binding by binding(FragmentMainBinding::bind)

    private val msgAdapter: MsgAdapter by withView {
        MsgAdapter().also { binding.rv.adapter = it }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClicks()
    }
    //endregion

    //region data binding
    private val messagesFlow: MutableStateFlow<List<Msg>> by
        rmb { MutableStateFlow(emptyList<Msg>()) }
        .observe{
            msgAdapter.msgs = it

            msgAdapter.update {
                binding.rv.scrollToPosition(msgAdapter.itemCount - 1)
                binding.etMsg.text.clear()
            }
        }
    //endregion

    //region actions
    private fun onClicks(){
        binding.btnSend.onClick {
            val text = binding.etMsg.text.toString()
            val greeting = Msg(0, true, text)
            messagesFlow.update { it + greeting }

            if (text == "How are you")
                mvbScope.launch {
                    delay(1000)
                    val reply = Msg(1, false, "Good")
                    messagesFlow.update { it + reply }
                }
        }
    }
    //endregion
}