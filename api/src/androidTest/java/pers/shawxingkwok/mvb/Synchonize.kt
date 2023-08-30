package pers.shawxingkwok.mvb

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.android.rmb
import pers.shawxingkwok.mvb.android.save
import java.util.concurrent.atomic.AtomicInteger

private val times = AtomicInteger(0)

@RunWith(AndroidJUnit4::class)
internal class Synchronize {
    @Test
    fun start(){
        launchFragment<MyFragment>()
        assert(times.get() == 2)
    }

    internal class MyFragment : Fragment(){
        private val x by rmb { times.getAndAdd(1) }
        private val y by save { times.getAndAdd(1) }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            runBlocking {
                repeat(30){
                    launch(Dispatchers.Default) { x; y }
                }
            }
        }
    }
}