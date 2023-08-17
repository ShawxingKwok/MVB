@file:Suppress("DEPRECATION", "UNCHECKED_CAST")

package pers.shawxingkwok.mvb.android

import android.os.*
import android.util.SparseArray
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import pers.shawxingkwok.ktutil.updateIf
import java.util.*

@Suppress("NAME_SHADOWING")
public class SavableMVBData<LSV, T> @PublishedApi internal constructor(
    isSynchronized: Boolean,
    thisRef: LSV,
    initialize: (() -> T)?,
    @PublishedApi internal var savedTypeClass: Class<*>,
)
: MVBData<LSV, T>(isSynchronized, thisRef, initialize)
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
{
    @PublishedApi internal var putToBundle: ((Bundle, String, T) -> Unit)? = null
    @PublishedApi internal var getFromBundle: ((Bundle, String) -> T)? = null

    @PublishedApi internal var convert: ((T) -> Any?)? = null
    @PublishedApi internal var recover: ((Any?) -> T)? = null

    init {
        actionsOnDelegate += { thisRef, key, getValue ->
            thisRef.savedStateRegistry.registerSavedStateProvider(key){
                val v = getValue()

                if (putToBundle != null) {
                    val bundle = Bundle()
                    putToBundle!!(bundle, key, v)
                    return@registerSavedStateProvider bundle
                }

                val saved =
                    when{
                        convert != null -> convert!!(v)
                        else -> v
                    }

                if (saved is SparseArray<*>)
                    Bundle().also { it.putSparseParcelableArray(key, saved as SparseArray<out Parcelable>) }
                else
                    bundleOf(key to saved)
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
                    else {
                        val restored = restoredState.get(key)
                            .updateIf({ it is Array<*> && it.isArrayOf<Parcelable>() }) {
                                Arrays.copyOf(it as Array<Parcelable>, it.size, savedTypeClass as Class<Array<*>>)
                            }

                        if (recover != null)
                            recover!!(restored)
                        else
                            restored as T
                    }

                true to v
            }
        }
}

public inline fun <LSV, reified T> LSV.save(
    isSynchronized: Boolean = false,
    noinline initialize: (() -> T)?,
)
: SavableMVBData<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    SavableMVBData(isSynchronized, this, initialize, T::class.java)

public fun <LSV, T> SavableMVBData<LSV, T>.process(
    putToBundle: (bundle: Bundle, key: String, value: T) -> Unit,
    getFromBundle: (bundle: Bundle, key: String) -> T,
)
: SavableMVBData<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    also {
        it.convert = null
        it.recover = null

        it.putToBundle = putToBundle
        it.getFromBundle = getFromBundle
    }

public inline fun <LSV, T, reified C> SavableMVBData<LSV, T>.process(
    noinline convert: (T) -> C,
    noinline recover: (C) -> T,
)
: SavableMVBData<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    also {
        it.putToBundle = null
        it.getFromBundle = null

        it.savedTypeClass = C::class.java
        it.convert = convert
        it.recover = recover as (Any?) -> T
    }