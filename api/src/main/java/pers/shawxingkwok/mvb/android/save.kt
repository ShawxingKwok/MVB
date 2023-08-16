@file:Suppress("DEPRECATION", "UNCHECKED_CAST")

package pers.shawxingkwok.mvb.android

import android.os.*
import android.util.SparseArray
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner

public class Savable<LSV, T> internal constructor(
    isSynchronized: Boolean,
    thisRef: LSV,
    initialize: (() -> T)?,
)
: MVBData<LSV, T>(isSynchronized, thisRef, initialize)
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
{
    internal var convert: ((T) -> Any?)? = null
    internal var getFromBundle: ((Bundle, String) -> T)? = null

    init {
        actionsOnDelegate += { _, _ ->
            thisRef.savedStateRegistry.registerSavedStateProvider(key){
                val v =
                    when(val convert = convert){
                        null -> value
                        else -> convert(value)
                    }

                if (v is SparseArray<*>)
                    Bundle().also { it.putSparseParcelableArray(key, v as SparseArray<out Parcelable>) }
                else
                    bundleOf(key to v)
            }
        }
    }

    override fun getValueOnNew(thisRef: LSV, key: String): Pair<Boolean, T?> =
        when (val restoredState = thisRef.savedStateRegistry.consumeRestoredStateForKey(key)) {
            null -> false to null
            else -> {
                val v =
                    if (getFromBundle != null)
                        getFromBundle!!(restoredState, key)
                    else
                        restoredState.get(key) as T

                true to v
            }
        }
}

public fun <LSV, T> LSV.save(isSynchronized: Boolean, initialize: (() -> T)?): Savable<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    Savable(isSynchronized, this, initialize)

public fun <LSV, T> Savable<LSV, T>.process(
    convert: ((T) -> Any?)?,
    getFromBundle: (bundle: Bundle, key: String) -> T,
)
: Savable<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    also {
        it.convert = convert
        it.getFromBundle = getFromBundle
    }

public fun <LSV, T, S> Savable<LSV, T>.process(
    convert: (T) -> S,
    recover: (S) -> T,
)
: Savable<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    also {
        it.convert = convert

        it.getFromBundle = { bundle, key ->
            val saved = bundle.get(key) as S
            recover(saved)
        }
    }