@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package pers.shawxingkwok.mvb

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.ktutil.allDo
import pers.shawxingkwok.mvb.android.rmb
import pers.shawxingkwok.mvb.android.save

@RunWith(AndroidJUnit4::class)
internal class Rotate {

    @Test
    fun start(){
        launchFragment<MyFragment>().onFragment{
            it.requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    internal class MyFragment : Fragment(){
        val x by rmb { 1 }
        var y by rmb{ 2 }
        // or lateinit
        var z by rmb<_, Int>()

        val a by save { 1 }
        var b by save { 2 }
        // or lateinit
        var c by save<_, Int>()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            allDo(::y, ::z, ::b, ::c){
                if (savedInstanceState == null){
                    it.set(2)
                    it.set(it.get() + 1)
                }else{
                    assert(it.get() == 3)
                }
            }
        }
    }
}