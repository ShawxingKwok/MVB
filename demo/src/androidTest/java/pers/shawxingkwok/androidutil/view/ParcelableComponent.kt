@file:Suppress("unused")

package pers.shawxingkwok.androidutil.view

import android.os.Parcelable
import android.util.SparseArray
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import pers.shawxingkwok.mvb.android.save

internal class ParcelableComponent {
    internal class MyFragment : Fragment() {
        @Parcelize
        data class User(val id: Long, val name: String): Parcelable

        /**
         * Pass `KClass<out Parcelable>` (`User::class` in this case) when ultimately
         * saved as `List<out Parcelable>`/`SparseArray<out Parcelable>`.
         *
         * 'ultimately' means saved types change with `transform`.
         */
        val userList by save(User::class) { mutableListOf<User>() }
        val userSparseArr by save(User::class) { SparseArray<User>() }

        /**
         * `User::class` could be automatically recognized in cases below.
         */
        var user: User by save()
        var userArr by save { emptyArray<User>() }
    }
}