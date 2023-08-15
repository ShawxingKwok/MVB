package pers.shawxingkwok.mvb

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty0

context(LSV)
public fun <LSV, T, F: Flow<T>> MVBData<LSV, F>.observe(act: (T) -> Unit): MVBData<LSV, F>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    apply {
        // it.actionsOnDelegate += { lifecycleOwner, _ ->
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    value.collect(act)
                }
            }
        // }
    }

context (LSV)
public fun <LSV, T, F: Flow<T>, P: KProperty0<F>> P.observe(act: (T) -> Unit): P
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    also { prop ->
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                prop.get().collect(act)
            }
        }
    }