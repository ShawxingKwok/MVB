package pers.shawxingkwok.mvb

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.junit.runner.RunWith
import pers.shawxingkwok.mvb.android.*

@RunWith(AndroidJUnit4::class)
internal class Observe {
    @Test
    fun start(){
        launchFragment<MyFragment>()
    }

    internal class MyFragment : Fragment() {
        private val names = mutableListOf<String>()

        private fun act(name: String){
            names += name
            assert(lifecycle.currentState == Lifecycle.State.STARTED)
        }

        private val x: Flow<Int> by rmb { flowOf(1) }.observe { act("x") }
        private val y by saveMutableStateFlow { 1 }.observe { act("y") }
        private val z by saveMutableSharedFlow<_, Int>(replay = 1).observe { act("z") }
        private val a by saveMutableLiveData { 1 }.observe { act("a") }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            z.tryEmit(1)
        }

        override fun onResume() {
            super.onResume()
            assertAll(::x, ::y, ::z, ::a){
                assert(it.name in names)
            }
        }
    }
}

@RunWith(AndroidJUnit4::class)
internal class Observe_ {

    @Test
    fun checkObserveFlowOnResume() {
        launchFragment<MyFragment>()
    }

    internal class MyFragment : Fragment(){
        val observedPropNames = mutableListOf<String>()

        private val a by rmb { flowOf(1) }.observe(true){
            observedPropNames += "a"
            assert(lifecycle.currentState == Lifecycle.State.RESUMED)
        }

        private val b by save { flowOf(1) }.observe(true){
            observedPropNames += "b"
            assert(lifecycle.currentState == Lifecycle.State.RESUMED)
        }

        override fun onDestroy() {
            super.onDestroy()
            assertAll("a", "b"){
                assert(it in observedPropNames)
            }
        }
    }
}