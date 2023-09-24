package pers.shawxingkwok.mvb

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import pers.shawxingkwok.mvb.android.mvbScope
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
internal class Scope {

    @Test
    fun useAppContext() {
        launchFragment<MyFragment>()
    }

    class MyViewModel : ViewModel(){}

    class MyFragment : Fragment(){
        val scopes = CopyOnWriteArrayList<CoroutineScope>()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            repeat(10){
                lifecycleScope.launch(Dispatchers.Default) {
                    scopes += mvbScope
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            assert(scopes.size == 10)
            scopes.single()
        }
    }
}