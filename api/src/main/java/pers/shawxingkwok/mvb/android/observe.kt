package pers.shawxingkwok.mvb.android

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty0

// Since lambdas on mvb properties won't take too many memories, here doesn't use inline to avoid
// caring `@PublishedApi`.

// flow
private fun <T> LifecycleOwner.collectFlowOnStart(
    getFlow: () -> Flow<T>,
    act: suspend CoroutineScope.(T) -> Unit,
){
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED){
            getFlow().collect{ act(it) }
        }
    }
}

public fun <LSV, T, F: Flow<T>, M: MVBData<LSV, F>> M.observe(act: suspend CoroutineScope.(T) -> Unit): M
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    also { m ->
        m.actionsOnDelegate += { lifecycleOwner, _, _, getValue ->
            lifecycleOwner.collectFlowOnStart(getValue, act)
        }
    }

context (LifecycleOwner)
public fun <T, F: Flow<T>> KProperty0<F>.observe(act: suspend CoroutineScope.(T) -> Unit): KProperty0<F> =
    apply{
        collectFlowOnStart(this::get, act)
    }

// liveData
private fun <T> LifecycleOwner.observeLiveData(
    getLiveData: () -> LiveData<T>,
    act: (T) -> Unit,
) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            // if (owner !is Fragment)
            getLiveData().observe(owner, act)
            // else
            //     owner.viewLifecycleOwnerLiveData.observe(owner){
            //         getLiveData().observe(owner.viewLifecycleOwner, act)
            //     }
        }
    })
}

public fun <LSV, T, L: LiveData<T>, M: MVBData<LSV, L>> M.observe(act: (T) -> Unit): M
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    also { m ->
        m.actionsOnDelegate += { lifecycleOwner, _, _, getValue ->
            lifecycleOwner.observeLiveData(getValue, act)
        }
    }

context (LifecycleOwner)
public fun <T, F: LiveData<T>> KProperty0<F>.observe(act: (T) -> Unit): KProperty0<F> =
    apply{
        observeLiveData(this::get, act)
    }