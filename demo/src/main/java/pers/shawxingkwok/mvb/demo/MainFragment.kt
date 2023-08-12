package pers.shawxingkwok.mvb.demo

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.savedstate.SavedStateRegistry
import com.dylanc.viewbinding.nonreflection.binding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.androidutil.view.collectOnResume
import pers.shawxingkwok.androidutil.view.onClick
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.mvb.demo.databinding.FragmentMainBinding
import pers.shawxingkwok.mvb.mvbScope
import pers.shawxingkwok.mvb.observe
import pers.shawxingkwok.mvb.rmb

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
        rmb { MutableStateFlow(emptyList<Msg>()) }
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