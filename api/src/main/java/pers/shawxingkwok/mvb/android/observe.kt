package pers.shawxingkwok.mvb.android

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// Since lambdas on mvb properties won't take too many memories, here doesn't use inline to avoid
// caring `@PublishedApi`.

/**
 * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/mvb/android/#observe).
 */
public fun <LV, T, F: Flow<T>, M: MVBData<LV, F>> M.observe(
    repeatOnResumed: Boolean = false,
    act: suspend CoroutineScope.(T) -> Unit,
): M
    where LV: LifecycleOwner, LV: ViewModelStoreOwner
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
public fun <LV, T, L: LiveData<T>, M: MVBData<LV, L>> M.observe(act: (T) -> Unit): M
    where LV: LifecycleOwner, LV: ViewModelStoreOwner
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