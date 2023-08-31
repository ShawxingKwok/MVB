@file:Suppress("unused")

package pers.shawxingkwok.mvb

import androidx.fragment.app.Fragment
import pers.shawxingkwok.mvb.android.saveMutableStateFlow
import pers.shawxingkwok.mvb.android.transform

internal class Sample : Fragment(){
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
}