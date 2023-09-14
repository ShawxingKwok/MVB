package pers.shawxingkwok.mvb

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pers.shawxingkwok.mvb.android.rmb
import pers.shawxingkwok.mvb.android.save
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
internal class Initializer {
    @Test
    fun callBeforeInitialization(){
        launchFragment<MyFragment>(initialState = Lifecycle.State.INITIALIZED).onFragment {
            assertFailsWith<IllegalStateException> { it.e }
            assertFailsWith<IllegalStateException> { it.f }
        }
    }

    @Test
    fun callValWithoutInitialize(){
        launchFragment<MyFragment>(initialState = Lifecycle.State.CREATED).onFragment {
            assertFailsWith<IllegalArgumentException>{ it.a }
            assertFailsWith<IllegalArgumentException>{ it.b }
        }
    }

    @Test
    fun callVarBeforeInitialize(){
        launchFragment<MyFragment>(initialState = Lifecycle.State.CREATED).onFragment {
            assertFailsWith<IllegalStateException>{ it.c }
            assertFailsWith<IllegalStateException>{ it.d }
        }
    }

    internal class MyFragment : Fragment() {
        val e by rmb { 1 }
        val f by save { 1 }

        val a: Int by rmb()
        val b: Int by save()

        var c: Int by rmb()
        var d: Int by save()
    }
}