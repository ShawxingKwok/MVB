@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package pers.shawxingkwok.mvb

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pers.shawxingkwok.ktutil.allDo
import pers.shawxingkwok.mvb.android.rmb
import pers.shawxingkwok.mvb.android.save

private var initializationDone = false
private var recreationDone = false

@RunWith(AndroidJUnit4::class)
internal class Rotate {
    @Test
    fun start(){
        launchFragment<MyFragment>().recreate()
        assert(initializationDone)
        assert(recreationDone)
    }

    internal class MyFragment : Fragment(){
        var x by rmb { 1 }
        var y: Int by rmb()

        var a by save { 1 }
        var b: Int by save()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            if (savedInstanceState == null)
                allDo(::x, ::y, ::a, ::b){
                    it.set(2)
                    it.set(it.get() + 1)
                    initializationDone = true
                }
            else {
                assertAll(::x, ::y, ::a, ::b){
                    assert(it.get() == 3)
                }
                recreationDone = true
            }
        }
    }
}