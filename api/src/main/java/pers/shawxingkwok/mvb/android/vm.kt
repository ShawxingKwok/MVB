package pers.shawxingkwok.mvb.android

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

@Synchronized
internal fun ViewModelStoreOwner.getMVBVm(): MVBViewModel =
    try {
        ViewModelProvider(this)[MVBViewModel::class.java]
    } catch (e: IllegalStateException) {
        error("Mvb values are kept in a viewModel which is not accessible at the moment.\n$e")
    }