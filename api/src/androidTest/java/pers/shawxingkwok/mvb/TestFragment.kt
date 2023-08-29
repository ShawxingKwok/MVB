package pers.shawxingkwok.mvb

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.android.observe
import pers.shawxingkwok.mvb.android.rmb

internal class TestFragment : Fragment() {
    val liveData by rmb { MutableLiveData(1) }
        .observe {
            KLog.d(lifecycle.currentState)
        }

    val _liveData = MutableLiveData(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _liveData.observe(this){
            KLog.d(lifecycle.currentState)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        KLog.d(lifecycle.currentState)
        liveData.value = liveData.value!! + 1
        _liveData.value = liveData.value!! + 1
    }
}

@RunWith(AndroidJUnit4::class)
internal class FragmentObserveTest {

    @Test
    fun start(){
        val scenario = launchFragmentInContainer<TestFragment>()

        scenario.onFragment{

        }
    }
}