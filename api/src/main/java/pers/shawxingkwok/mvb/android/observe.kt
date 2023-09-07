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

/**
 * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/mvb/android/#observe).
 */
public fun <LVS, T, F: Flow<T>, M: MVBData<LVS, F>> M.observe(
    repeatOnResumed: Boolean = false,
    act: suspend CoroutineScope.(T) -> Unit,
): M
    where LVS: LifecycleOwner, LVS: ViewModelStoreOwner, LVS: SavedStateRegistryOwner
=
    also { m ->
        val state = if (repeatOnResumed) Lifecycle.State.RESUMED else Lifecycle.State.STARTED

        m.actionsOnDelegate += { lifecycleOwner, _, _, getValue ->
            lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.repeatOnLifecycle(state){
                    getValue().collect{ act(it) }
                }
            }
        }
    }

/**
 * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/mvb/android/#observe).
 */
public fun <LVS, T, L: LiveData<T>, M: MVBData<LVS, L>> M.observe(act: (T) -> Unit): M
    where LVS: LifecycleOwner, LVS: ViewModelStoreOwner, LVS: SavedStateRegistryOwner
=
    also { m ->
        m.actionsOnDelegate += { lifecycleOwner, _, _, getValue ->
            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver{
                override fun onCreate(owner: LifecycleOwner) {
                    getValue().observe(lifecycleOwner, act)
                }
            })
        }
    }