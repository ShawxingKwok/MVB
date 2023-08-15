package pers.shawxingkwok.myapplication

import android.app.Application
import android.app.KeyguardManager.KeyguardLockedStateListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.lifecycleScope
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.myapplication.ui.main.MainFragment
import pers.shawxingkwok.myapplication.ui.main.MainViewModel

class MainActivity : AppCompatActivity() {
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