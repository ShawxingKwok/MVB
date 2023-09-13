@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package pers.shawxingkwok.androidutil.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import pers.shawxingkwok.mvb.android.rmb
import pers.shawxingkwok.mvb.android.saveMutableLiveData

class SaveLiveData{
    class MyViewModel(private val state: SavedStateHandle) : ViewModel(){
        val filteredData: LiveData<List<String>> =
            state.getLiveData<String>("query").switchMap { TODO() }

        fun setQuery(query: String) {
            state["query"] = query
        }
    }

    class MyFragment : Fragment(){
        val vm by viewModels<MyViewModel>()
    }

    class X{
        class MyFragment : Fragment(){
            val query by saveMutableLiveData<_, String>()
            val filteredData by rmb {
                query.switchMap<_, List<String>> { TODO() }
            }
        }
    }
}