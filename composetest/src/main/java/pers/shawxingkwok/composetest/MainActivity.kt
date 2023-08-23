package pers.shawxingkwok.composetest

import android.os.*
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.composetest.ui.theme.MVBTheme

class MainActivity : ComponentActivity() {
    private val vm by viewModels<GameViewModel>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MVBTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }
        val p = savedInstanceState?.getParcelable("p", P::class.java)
        if (p != null) {
            KLog.d(p)
            KLog.d(p.ms)
            KLog.d(p.ms.first())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("p", P(arrayOf(M(1), M(2))))
    }
}

@Parcelize
// open
class M(val i: Int) : Parcelable

// @Parcelize
// class SubM(val a: String, val s: Int) : M(1)

class P(val ms: Array<M>) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // dest.writeParcelableArray(ms, flags)
        dest.writeValue(ms)
    }

    companion object CREATOR : Parcelable.Creator<P> {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun createFromParcel(parcel: Parcel): P {
            val ms = parcel.readParcelableArray(M::class.java.classLoader, M::class.java)!!
            return P(ms)
        }

        override fun newArray(size: Int): Array<P?> {
            return arrayOfNulls(size)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MVBTheme {
        Greeting("Android")
    }
}

class GameViewModel : ViewModel(){
    init {
        Log.d("KLOG", Thread.currentThread().toString())
    }
}