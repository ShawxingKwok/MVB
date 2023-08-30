package pers.shawxingkwok.mvb

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.junit.runner.RunWith
import pers.shawxingkwok.mvb.android.*

private var i = 0

@RunWith(AndroidJUnit4::class)
internal class Observe {
    @Test
    fun start(){
        launchFragment<MyFragment>()
        assert(i == 6)
    }

    internal class MyFragment : Fragment() {
        companion object{
            private val _flow = flowOf(1)
            private val _livedata = MutableLiveData(1)
        }

        private fun act(name: String){
            i++
            // KLog.d(name)
            assert(lifecycle.currentState == Lifecycle.State.STARTED)
        }

        private val x by rmb { flowOf(1) }.observe { act("x") }
        private val y by saveMutableStateFlow { 1 }.observe { act("y") }
        private val z by saveMutableSharedFlow<_, Int>(replay = 1).observe { act("z") }
        private val a by saveMutableLiveData { 1 }.observe { act("a") }

        private val flow by ::_flow.observe { act("flow") }
        private val livedata by ::_livedata.observe { act("livedata") }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            z.tryEmit(1)
        }
    }
}