package pers.shawxingkwok.mvb

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockkObject
import io.mockk.verifyOrder
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
        companion object{
            fun act(lifecycle: Lifecycle, name: String){
                assert(lifecycle.currentState == Lifecycle.State.STARTED)
            }
        }

        init {
            mockkObject(MyFragment)
        }

        private val x: Flow<Int> by rmb { flowOf(1) }.observe { act(lifecycle, "x") }
        private val y by saveMutableStateFlow { 1 }.observe { act(lifecycle, "y") }
        private val z by saveMutableSharedFlow<_, Int>(replay = 1).observe { act(lifecycle, "z") }
        private val a by saveMutableLiveData { 1 }.observe { act(lifecycle, "a") }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            z.tryEmit(1)
        }

        override fun onResume() {
            super.onResume()
            // verifyAll does not work in this case.
            verifyOrder {
                act(any(), "x")
                act(any(), "y")
                act(any(), "z")
                act(any(), "a")
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