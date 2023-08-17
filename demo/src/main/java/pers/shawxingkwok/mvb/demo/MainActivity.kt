package pers.shawxingkwok.mvb.demo

import android.os.*
import android.util.SparseArray
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.parcelize.Parcelize
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.android.process
import pers.shawxingkwok.mvb.demo.databinding.ActivityMainBinding
import pers.shawxingkwok.mvb.android.save
import java.io.Serializable
import java.util.*

// arrayOf<Parcelable> -> Array<Msg> : (toList as List<Msg>).toTypedArray()

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // var serializables by save<_, Array<Msg>>{ arrayOf(Msg(1, false,"")) }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

@Parcelize
class P : Parcelable