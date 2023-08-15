@file:Suppress("DEPRECATION", "UNCHECKED_CAST")

package pers.shawxingkwok.mvb

import android.os.*
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner

// TODO(change)
@PublishedApi
internal fun Bundle.putAny(key: String, value: Any?) {
    putAll(bundleOf(key to value))
}

public class Savable<LSV, T> internal constructor(
    thisRef: LSV,
    initialize: (() -> T)?,
)
: MVBData<LSV, T>(thisRef, initialize)
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
{
    internal var convert: ((T) -> Any?)? = null
    internal var recover: ((Any?) -> T)? = null

    init {
        actionsOnDelegate += { _, _ ->
            thisRef.savedStateRegistry.registerSavedStateProvider(key){
                val v =
                    when(val convert = convert){
                        null -> value
                        else -> convert(value)
                    }
                bundleOf(key to v)
            }
        }
    }

    override fun getValueOnNew(thisRef: LSV, key: String): Pair<Boolean, T?> =
        when (val restoredState = thisRef.savedStateRegistry.consumeRestoredStateForKey(key)) {
            null -> false to null
            else -> {
                val restored = restoredState.get(key)
                val value = when(val recover = recover){
                    null -> restored as T
                    else -> recover(restored)
                }
                true to value
            }
        }
}

public fun <LSV, T> LSV.save(initialize: (() -> T)?): Savable<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    Savable(this, initialize)

public fun <LSV, T, S> Savable<LSV, T>.customize(
    convert: (T) -> S,
    recover: (S) -> T,
)
: Savable<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    also {
        it.convert = convert
        it.recover = recover as (Any?) -> T
    }