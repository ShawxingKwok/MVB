package pers.shawxingkwok.mvb

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

public fun <LSV, T, F: Flow<T>> MVBData<LSV, F>.observe(act: (T) -> Unit): MVBData<LSV, F>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    extend { lifecycleOwner, _ ->
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                value.collect(act)
            }
        }
    }