package pers.shawxingkwok.myapplication

import android.app.KeyguardManager.KeyguardLockedStateListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import pers.shawxingkwok.myapplication.ui.main.MainFragment
import pers.shawxingkwok.myapplication.ui.main.MainViewModel

class MainActivity : AppCompatActivity() {

    private val vm by viewModels<MainViewModel>()

    init {
        Log.d("KLOG", lifecycle.toString())
        Log.d("KLOG", lifecycleScope.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance(1))
                .commitNow()
        }
    }
}