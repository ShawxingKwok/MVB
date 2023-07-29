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
import androidx.savedstate.SavedStateRegistry
import com.dylanc.viewbinding.nonreflection.binding
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.androidutil.view.collectOnResume
import pers.shawxingkwok.androidutil.view.onClick
import pers.shawxingkwok.androidutil.view.withView
import pers.shawxingkwok.mvb.demo.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {
    private val binding by binding(FragmentMainBinding::bind)
    private val vm: MainViewModel by viewModels()

    private val msgAdapter: MsgAdapter by withView {
        MsgAdapter().also { binding.rv.adapter = it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val provider = SavedStateRegistry.SavedStateProvider { bundleOf("a" to "initial") }
            savedStateRegistry.registerSavedStateProvider("mystate", provider)
        }else {
            val provider = SavedStateRegistry.SavedStateProvider { bundleOf("a" to "second") }
            savedStateRegistry.registerSavedStateProvider("mystate", provider)
            savedStateRegistry.getSavedStateProvider("mystate")?.saveState().let { KLog.d(it) }
            savedStateRegistry.consumeRestoredStateForKey("mystate").let { KLog.d(it) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.msgsFlow.collectOnResume{
            msgAdapter.msgs = it

            msgAdapter.update {
                binding.rv.scrollToPosition(msgAdapter.itemCount - 1)
                binding.etMsg.text.clear()
            }
        }

        binding.btnSend.onClick {
            vm.sendMsg(binding.etMsg.text.toString())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("a", "D")
    }
}