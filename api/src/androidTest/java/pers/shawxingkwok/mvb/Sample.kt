@file:Suppress("unused")

package pers.shawxingkwok.mvb

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import pers.shawxingkwok.mvb.android.*

internal class Sample : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mvbScope.launch {

        }
    }

    // val flow by save { MutableStateFlow(0) }
    //     .transform(
    //         convert = { it.value },
    //         recover = { MutableStateFlow(it) }, // or ::MutableStateFlow
    //     )

    // val flow by saveMutableStateFlow { 0 }

    val i by saveMutableStateFlow { 0 }

    // or append `transform` if the value type in the lambda violates Parcelize rules.
    class X(val i: Int)
    val x by saveMutableStateFlow { X(0) }
        .transform(
            convert = { it.i },
            recover = { X(it) }
        )

    // @Parcelize
    // data class User(val id: Long, val name: String) : Parcelable

    // var users by save(User::class) { listOf(User(0, "Jack")) }

    // If the flow property is bound to multiple view parts,
    // I suggest appending multiple `observe{...}`.
    val isRunning by rmb { MutableStateFlow(false) }
        .observe {
            // here generally updates view with `binding`(ViewBinding)
        }
        .observe {
            // ...
        }

    val duration by saveMutableStateFlow { 0 }
        // here could be inserted with `transform` which doesn't matter with `observe`.
        .observe { }
}