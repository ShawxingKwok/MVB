package pers.shawxingkwok.mvb.android

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.concurrent.ConcurrentHashMap

internal class MVBViewModel(val state: SavedStateHandle) : ViewModel() {
    // viewModelScope is not thread-safe.
    val scope by lazy{ viewModelScope }

    // keeps data from `rmb`
    val map = ConcurrentHashMap<String, Container>()
}