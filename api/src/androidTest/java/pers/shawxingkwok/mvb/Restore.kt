package pers.shawxingkwok.mvb

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.parcelize.Parcelize
import org.junit.Test
import org.junit.runner.RunWith
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.android.*

@RunWith(AndroidJUnit4::class)
internal class Restore {

    @Test
    fun start(){
        launchFragment<MyFragment>().recreate()
    }

    @Parcelize
    data class P(val i: Int) : Parcelable

    internal class MyFragment : Fragment() {
        private val sharedFlow by saveMutableSharedFlow<_, List<Array<P>>>(
            replay = 3,
            parcelableKClass = P::class
        )

        private val stateFlow by saveMutableStateFlow { 0 }

        val liveData by saveMutableLiveData { 0 }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (savedInstanceState == null){
                stateFlow.value++
                liveData.value = 1
                sharedFlow.tryEmit(listOf(arrayOf(P(1))))
                sharedFlow.tryEmit(listOf(arrayOf(P(2))))
            }else {
                assert(stateFlow.value == 1)
                assert(sharedFlow.replayCache.first().first().first().i == 1)
                assert(liveData.value == 1)
            }
        }
    }
}