package pers.shawxingkwok.mvb.android

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.util.concurrent.ConcurrentHashMap

internal class MVBViewModel(val state: SavedStateHandle) : ViewModel() {
    // keeps data from `rmb`
    val map = ConcurrentHashMap<String, Container>()
}