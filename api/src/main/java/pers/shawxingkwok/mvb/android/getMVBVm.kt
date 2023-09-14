package pers.shawxingkwok.mvb.android

import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

private fun ViewModelStoreOwner._getMVBVm(): MVBViewModel =
    try {
        ViewModelProvider(this)[MVBViewModel::class.java]
    } catch (e: IllegalStateException) {
        error("Mvb values are kept in a viewModel which is not accessible at the moment because of $e")
    }

internal fun ViewModelStoreOwner.getMVBVm(): MVBViewModel =
    if (Looper.myLooper() != Looper.getMainLooper())
        runBlocking(Dispatchers.Main){
            _getMVBVm()
        }
    else
        _getMVBVm()