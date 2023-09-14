package pers.shawxingkwok.mvb

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.lifecycleScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import pers.shawxingkwok.mvb.android.rmb
import pers.shawxingkwok.mvb.android.save
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
internal class SynchronizedVal {
    @Test
    fun start(){
        launchFragment<MyFragment>()
    }

    internal class MyFragment : Fragment(){
        val times = AtomicInteger(0)

        private val x by rmb { times.getAndAdd(1) }
        private val y by save { times.getAndAdd(1) }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            lifecycleScope.launch {
                coroutineScope {
                    repeat(30) {
                        launch(Dispatchers.Default) { x; y }
                    }
                }
                assert(times.get() == 2)
            }
        }
    }
}

@RunWith(AndroidJUnit4::class)
internal class NotSynchronizedVar {
    @Test
    fun start(){
        launchFragment<MyFragment>()
    }

    internal class MyFragment : Fragment(){
        private var x by rmb { 0 }
        private var y by save { 0 }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val dest = 1000
            lifecycleScope.launch {
                repeat(10) {
                    launch(Dispatchers.Default) {
                        repeat(dest / 10) {
                            x++; y++
                        }
                    }
                }
            }
            .invokeOnCompletion {
                assert(x != dest)
                assert(y != dest)
            }
        }
    }
}