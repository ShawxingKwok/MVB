package pers.shawxingkwok.mvb.demo

import android.os.*
import android.util.SparseArray
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.parcelize.Parcelize
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.android.save
import pers.shawxingkwok.mvb.demo.databinding.ActivityMainBinding
import java.util.*
import java.util.concurrent.atomic.AtomicReference

// arrayOf<Parcelable> -> Array<Msg> : (toList as List<Msg>).toTypedArray()

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}