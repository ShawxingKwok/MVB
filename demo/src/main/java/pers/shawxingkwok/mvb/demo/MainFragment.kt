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
        savedStateRegistry.consumeRestoredStateForKey("mystate").let { KLog.d(it) }
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

    val bundle = bundleOf("1" to 1)

    override fun onStop() {
        super.onStop()
        val provider = SavedStateRegistry.SavedStateProvider { bundle }
        savedStateRegistry.registerSavedStateProvider("mystate", provider)
        bundle.putParcelable("S", object : Parcelable{
            override fun describeContents(): Int {
                return Parcelable.CONTENTS_FILE_DESCRIPTOR
            }

            override fun writeToParcel(dest: Parcel, flags: Int) {
                KLog.d("")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        KLog.d("")
        bundle.putString("3", "3")
    }

    override fun onDestroy() {
        super.onDestroy()
        KLog.d("")
        bundle.putString("2", "2")
    }

    override fun onDetach() {
        super.onDetach()
        KLog.d("")
        savedStateRegistry
        bundle.putString("4", "4")
    }
}