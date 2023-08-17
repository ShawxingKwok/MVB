package pers.shawxingkwok.mvb.android

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

public fun <LSV, T, F: Flow<T>, M: MVBData<LSV, F>> M.observe(act: (T) -> Unit): M
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    also {
        it.actionsOnDelegate += { lifecycleOwner, _, getValue ->
            lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    getValue().collect(act)
                }
            }
        }
    }