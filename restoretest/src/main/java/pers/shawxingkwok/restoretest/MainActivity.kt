@file:Suppress("MemberVisibilityCanBePrivate")

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

inline fun <reified T> Any?.isArray() =
    this is Array<*> && javaClass.componentType == T::class.java

class MainActivity : ComponentActivity() {
    init {
        assert(arrayOf(P(0)).isArray<P>())
    }

    private val sharedFlow by saveMutableSharedFlow<_, List<P>>(
        replay = 3,
        parcelableComponent = P::class
    )

    private val _sharedFlow by saveMutableSharedFlow<_, Array<P>>(replay = 3)

    private val stateFlow by saveMutableStateFlow { 0 }

    val liveData by saveMutableLiveData { 0 }
    val emptyLiveData by saveMutableLiveData<_, Int>()
    val nullableLiveData by saveMutableLiveData<_, Int?> { 1 }
    val sparseArray by save(P::class) { SparseArray<P>() }

    var number by save { BigDecimal(0.0) }

    val ints by save { intArrayOf(0) }

    val ps by save { arrayOf(P(0)) }
    val _ps by save(P::class) { listOf(arrayOf(P(0))) }

    val qs by save { arrayOf(Q1(), Q2()) }
    val _qs by save { listOf(arrayOf(Q1(), Q2())) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null){
            stateFlow.value++
            assert(emptyLiveData.value == null)
            liveData.value = 1
            nullableLiveData.value = null
            sharedFlow.tryEmit(listOf(P(0)))
            _sharedFlow.tryEmit(arrayOf())
            sparseArray.append(0, P(0))
            number += BigDecimal(1)
            ints[0]++
            ps[0].i++
            _ps[0][0].i++
            qs[0].i++
            _qs.first()[0].i++
        }else {

            assert(stateFlow.value == 1)
            assert(emptyLiveData.value == null)
            assert(liveData.value == 1)
            assert(nullableLiveData.value == null)
            assert(sharedFlow.replayCache.first().first().i == 0)
            assert(_sharedFlow.replayCache.first().isArray<P>())
            assert(sparseArray.valueAt(0) == P(0))
            assert(number.toInt() == 1)
            assert(ints.first() == 1)
            assert(ps.isArray<P>())
            assert(qs.isArray<Q>())
            assert(_qs.first().isArray<Q>())
            // This parcelable bug should be fixed by the authority
            KLog.d(_ps.first().isArray<Q>())
            KLog.d("done")
        }
    }
}