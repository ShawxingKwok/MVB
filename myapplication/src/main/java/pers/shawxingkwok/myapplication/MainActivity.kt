package pers.shawxingkwok.myapplication

import android.app.KeyguardManager.KeyguardLockedStateListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import pers.shawxingkwok.myapplication.ui.main.MainFragment

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