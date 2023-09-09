package pers.shawxingkwok.mvb.android

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

internal fun <LV> LV.getMVBVm(): MVBViewModel
    where LV: LifecycleOwner, LV: ViewModelStoreOwner
=
    synchronized(lifecycle) {
        try {
            ViewModelProvider(this)[MVBViewModel::class.java]
        } catch (e: IllegalStateException) {
            error("Mvb values are kept in a viewModel which is not accessible at the moment.\n$e")
        }
    }