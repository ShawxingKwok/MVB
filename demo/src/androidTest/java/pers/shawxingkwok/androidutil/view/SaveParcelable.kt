@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package pers.shawxingkwok.androidutil.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import pers.shawxingkwok.mvb.android.save

class SaveParcelable{
    class MyViewModel(private val state: SavedStateHandle) : ViewModel() {
        val arr = state.get<Array<Int?>>("arr")
            ?: arrayOfNulls<Int>(2).also { state["arr"] = it }

        var x = state.get<Int>("x") ?: 0
            set(value) {
                state["x"] = value
                field = value
            }
    }

    class MyFragment : Fragment(){
        val vm by viewModels<MyViewModel>()
    }

    class X {
        class MyFragment : Fragment() {
            val arr by save { arrayOfNulls<Int>(2) }
            var x by save { 0 }
            // lateinit is also supported
            var name by save<_, String>()
        }
    }
}