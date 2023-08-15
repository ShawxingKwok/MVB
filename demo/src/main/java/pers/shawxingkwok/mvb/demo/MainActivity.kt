package pers.shawxingkwok.mvb.demo

import android.os.*
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.demo.databinding.ActivityMainBinding
import pers.shawxingkwok.mvb.enableMVBSave

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        enableMVBSave()
        super.onSaveInstanceState(outState)
    }
}