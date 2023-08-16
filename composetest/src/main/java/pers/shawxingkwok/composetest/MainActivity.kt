package pers.shawxingkwok.composetest

import android.app.KeyguardManager.KeyguardLockedStateListener
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
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
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.parcelize.Parcelize
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.composetest.ui.theme.MVBTheme
import pers.shawxingkwok.ktutil.KReadWriteProperty
import pers.shawxingkwok.ktutil.fastLazy
import kotlin.concurrent.thread
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

class MainActivity : ComponentActivity() {
    private val vm by viewModels<GameViewModel>()

    var x by object : KReadWriteProperty<Any?, Int>{
        override fun onDelegate(thisRef: Any?, property: KProperty<*>) {
            KLog.d(property is KMutableProperty<*>)
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            TODO("Not yet implemented")
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            TODO("Not yet implemented")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bundle()
        savedInstanceState?.get("LEU").let {
            it ?: Log.d("KLOG", "null").also { return@let  }
            Log.d("KLOG", it.toString())
            Log.d("KLOG", (it as F).toString())
            Log.d("KLOG", (it as F).i.toString())
        }

        setContent {
            MVBTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val bundle = bundleOf("LEU" to F(1))
        outState.putAll(bundle)
    }
}

@Parcelize
class F(val i: Int) : Parcelable

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