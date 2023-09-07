@file:Suppress("unused", "MemberVisibilityCanBePrivate", "PropertyName")

package pers.shawxingkwok.androidutil.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import pers.shawxingkwok.mvb.android.*

class SaveFlow {
    class MyViewModel(private val state: SavedStateHandle) : ViewModel() {
        val filteredData: Flow<List<String>> =
            state.getStateFlow<String?>("query", null)
                .map { if (it == null) emptyList() else TODO() }

        fun setQuery(query: String) {
            state["query"] = query
        }
    }

    class MyFragment : Fragment() {
        val vm by viewModels<MyViewModel>()
    }

    class X {
        class MyFragment : Fragment() {
            val query by saveMutableStateFlow<_, String?> { null }
            val filteredData by rmb {
                query.map { if (it == null) emptyList<String>() else TODO() }
            }

            // or via saveMutableSharedFlow
            val _query by saveMutableSharedFlow<_, String>(replay = 1)
            val _filteredData: Flow<List<String>> by rmb { _query.map { TODO() } }
        }
    }
}