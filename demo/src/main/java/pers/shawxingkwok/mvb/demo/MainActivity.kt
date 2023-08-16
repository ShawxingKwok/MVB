package pers.shawxingkwok.mvb.demo

import android.os.*
import android.util.SparseArray
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.android.process
import pers.shawxingkwok.mvb.demo.databinding.ActivityMainBinding
import pers.shawxingkwok.mvb.android.save
import java.io.Serializable

// arrayOf<Parcelable> -> Array<Msg> : (toList as List<Msg>).toTypedArray()

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    val sparseArray by save { SparseArray<Msg>() }
        .process(
            convert = null,
            getFromBundle = { bundle, key ->
                bundle.getSparseParcelableArray(key, Msg::class.java)!!
            }
        )

    private var list by save { listOf<Any?>(F()) }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // KLog.d(sparseArray)
        // sparseArray.append(0, Msg(1, false, "fPG"))
        KLog.d(list)
        list += G()
    }
}

class F : Serializable
class G : Serializable