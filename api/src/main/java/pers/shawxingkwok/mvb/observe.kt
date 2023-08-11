package pers.shawxingkwok.mvb

import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

public fun <L: LifecycleOwner, T, F: Flow<T>> MVBData<L, F>.observe(collector: FlowCollector<T>): MVBData<L, F> =
    extend { lifecycleOwner, _ ->
        (lifecycleOwner as LifecycleOwner).lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                value.collect(collector)
            }
        }
    }