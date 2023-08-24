package pers.shawxingkwok.mvb.android

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import pers.shawxingkwok.androidutil.KLog

public fun <LSV, T, F: Flow<T>, M: MVBData<LSV, F>> M.observe(act: suspend (T) -> Unit): M
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    also {
        it.actionsOnDelegate += { lifecycleOwner, key, getValue ->
            lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    getValue().collect(act)
                }
            }
        }
    }