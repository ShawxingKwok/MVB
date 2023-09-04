package pers.shawxingkwok.restoretest

import android.os.Bundle
import android.util.SparseArray
import androidx.activity.ComponentActivity
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.android.save
import pers.shawxingkwok.mvb.android.saveMutableLiveData
import pers.shawxingkwok.mvb.android.saveMutableSharedFlow
import pers.shawxingkwok.mvb.android.saveMutableStateFlow
import java.math.BigDecimal

class MainActivity : ComponentActivity() {
    private val sharedFlow by saveMutableSharedFlow<_, List<Array<P>>>(
        replay = 3,
        parcelableComponent = P::class
    )

    private val stateFlow by saveMutableStateFlow { 0 }

    val liveData by saveMutableLiveData { 0 }

    val sparseArray by save(P::class) { SparseArray<P>() }

    var number by save { BigDecimal(0.0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null){
            stateFlow.value++
            liveData.value = 1
            sharedFlow.tryEmit(listOf(arrayOf(P(2))))
            sharedFlow.tryEmit(listOf(arrayOf(P(3))))
            sparseArray.append(0, P(2))
            number += BigDecimal(1)

            KLog.d(number)
        }else {
            assert(stateFlow.value == 1)
            assert(liveData.value == 1)
            assert(sharedFlow.replayCache.first().first().first().i == 2)
            assert(sparseArray.valueAt(0) == P(2))
            KLog.d(number)
            KLog.d("done")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSparseParcelableArray("as", SparseArray<P>())
    }
}
