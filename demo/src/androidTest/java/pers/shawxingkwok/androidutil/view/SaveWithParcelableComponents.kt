@file:Suppress("unused", "MemberVisibilityCanBePrivate", "PropertyName")

package pers.shawxingkwok.androidutil.view

import android.os.Parcelable
import android.util.SparseArray
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import pers.shawxingkwok.mvb.android.save
import pers.shawxingkwok.mvb.android.transform

class SaveWithParcelableComponents {
    class MyFragment : Fragment(){
        @Parcelize
        data class User(val id: Long, val name: String): Parcelable

        /**
         * `KClass<out Parcelable>`(`User::class` in this case) could be automatically
         * recognized as the parcelable component, if the ultimately saved type is `Parcelable`
         * or Array<Parcelable>.
         */
        var user by save{ User(1, "Apollo") }
        var users by save{ emptyArray<User>() }

        // pass `User::class` when ultimately saved in List/SparseArray
        val _users by save(User::class){ mutableListOf<User>() }
        val userGroups by save(User::class){ mutableListOf<Array<User>>() }
        val __users by save(User::class){ SparseArray<User>() }
        val _userGroups by save(User::class){ SparseArray<Array<User>>() }

        // `ultimately` means saved types change with transform.
    }
}