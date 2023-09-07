@file:Suppress("unused")

package pers.shawxingkwok.androidutil.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import pers.shawxingkwok.mvb.android.rmb

class Rmb {
    class MyViewModel : ViewModel() {
        val arr = arrayOfNulls<Int>(2)
        var x = 0
        lateinit var name: String
    }

    class MyFragment : Fragment() {
        val vm by viewModels<MyViewModel>()
    }

    class XX {
        class MyFragment : Fragment() {
            val arr by rmb { arrayOfNulls<Int>(2) }
            var x by rmb { 0 }
            // lateinit
            var name by rmb<_, String>()
        }
    }
}
