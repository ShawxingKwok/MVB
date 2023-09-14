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
import pers.shawxingkwok.mvb.android.MVBViewModel
import pers.shawxingkwok.mvb.android.getMVBVm

@RunWith(AndroidJUnit4::class)
internal class VM {
    @Test
    fun start(){
        launchFragment<MyFragment>()
    }

    class MyFragment : Fragment(){
        private val viewModels = mutableSetOf<MVBViewModel>()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            lifecycleScope.launch {
                coroutineScope {
                    repeat(1000) {
                        launch(Dispatchers.Default) {
                            viewModels += getMVBVm()
                        }
                    }
                }
                assert(viewModels.size == 1)
            }
        }
    }
}