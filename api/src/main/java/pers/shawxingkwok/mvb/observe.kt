package pers.shawxingkwok.mvb

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

public fun <LSV, T, F: Flow<T>, M: MVBData<LSV, F>> M.observe(act: (T) -> Unit): M
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    also {
        it.actionsOnDelegate += { lifecycleOwner, _ ->
            lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    value.collect(act)
                }
            }
        }
    }