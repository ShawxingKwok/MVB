package pers.shawxingkwok.androidutil.view

import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith
import pers.shawxingkwok.androidutil.KLog

import kotlin.reflect.jvm.isAccessible

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        println(MainFragment::class.constructors.joinToString("\n"))
        X::class.constructors.joinToString("\n").let { KLog.d(it) }
        Y::class.constructors.joinToString("\n").let { KLog.d(it) }
    }
}

class X(val x: Int = 1)