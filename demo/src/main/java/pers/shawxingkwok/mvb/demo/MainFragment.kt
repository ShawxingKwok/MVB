package pers.shawxingkwok.mvb.demo

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dylanc.viewbinding.nonreflection.binding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.androidutil.view.onClick
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.mvb.android.*
import pers.shawxingkwok.mvb.demo.databinding.FragmentMainBinding

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainFragment : Fragment(R.layout.fragment_main) {
    private val binding by binding(FragmentMainBinding::bind)
    private val msgAdapter: MsgAdapter by fastLazy(::MsgAdapter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null)
            initMsgs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rv.run {
            adapter = msgAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private val a by saveMutableSharedFlow<_, Array<Msg?>>(replay = 1)
        .observe {
            KLog.d(it)
        }

    private val b by saveMutableSharedFlow<_, Msg>(replay = 1)
        .observe {
            KLog.d(it)
        }

    @Suppress("unused")
    private val msgsFlow by
        rmb { combine(a, b){ _a, _b -> _a + _b } }
        .observe {
            msgAdapter.msgs = it.filterNotNull().toTypedArray()

            msgAdapter.update {
                binding.rv.scrollToPosition(msgAdapter.itemCount - 1)
                binding.etMsg.text.clear()
            }
        }

    private fun initMsgs(){
        lifecycleScope.launch {
            delay(1000)
            a.emit(arrayOf(Msg(1, true, "How are you"), null))
            b.emit( Msg(2, false, "Good") )
        }
    }
}