@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package pers.shawxingkwok.androidutil.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import pers.shawxingkwok.mvb.android.save
import pers.shawxingkwok.mvb.android.saveMutableStateFlow
import pers.shawxingkwok.mvb.android.transform

class SaveWithTransform {
    class MyFragment : Fragment() {
        class X(var i: Int = 0)

        val x by save { X() }
            .transform(
                convert = { it.i },
                recover = { X(it) }
            )

        class Y(val i: Int = 0, val key: String? = null)

        var y by save { Y() }
            .transform(
                convert = {
                    it.i to it.key // generally convert to triple or list when more values
                },
                recover = {(i, key) -> Y(i, key) }
            )
    }
}