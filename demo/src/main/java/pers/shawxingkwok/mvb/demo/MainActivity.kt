package pers.shawxingkwok.mvb.demo

import android.os.*
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.demo.databinding.ActivityMainBinding
import pers.shawxingkwok.mvb.rmb
import pers.shawxingkwok.mvb.save

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    var x by save { 1 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        KLog.d(x++)
    }
}