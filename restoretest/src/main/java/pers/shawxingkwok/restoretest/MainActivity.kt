package pers.shawxingkwok.restoretest

import android.app.Fragment
import android.icu.util.GregorianCalendar
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.parcelize.Parcelize
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.android.save
import pers.shawxingkwok.mvb.android.saveMutableLiveData
import pers.shawxingkwok.mvb.android.saveMutableSharedFlow
import pers.shawxingkwok.mvb.android.saveMutableStateFlow
import pers.shawxingkwok.restoretest.ui.theme.MVBTheme
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Calendar
import java.util.Timer

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
        setContentView(R.layout.activity_main)

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
