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
public class SavableMVBData<LSV, T, C> @PublishedApi internal constructor(
    isSynchronized: Boolean,
    thisRef: LSV,
    initialize: (() -> T)?,
    @PublishedApi internal var savedTypeClass: Class<*>,
)
: MVBData<LSV, T>(isSynchronized, thisRef, initialize)
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
{
    @PublishedApi internal var convert: ((T) -> C)? = null
    @PublishedApi internal var recover: ((C) -> T)? = null

    init {
        actionsOnDelegate += { thisRef, key, getValue ->
            thisRef.savedStateRegistry.registerSavedStateProvider(key){
                val v = getValue()

                val saved: Any? =
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
                val restored = restoredState.get(key)
                    .updateIf({ it is Array<*> && it.isArrayOf<Parcelable>() }) {
                        Arrays.copyOf(it as Array<Parcelable>, it.size, savedTypeClass as Class<Array<*>>)
                    }

                val v =
                    if (recover != null)
                        recover!!(restored as C)
                    else
                        restored as T

                true to v
            }
        }
}

public inline fun <LSV, reified T> LSV.save(
    isSynchronized: Boolean = false,
    noinline initialize: (() -> T)?,
)
: SavableMVBData<LSV, T, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    SavableMVBData(isSynchronized, this, initialize, T::class.java)

public inline fun <LSV, T, C, reified D> SavableMVBData<LSV, T, C>.process(
    noinline convert: (C) -> D,
    noinline recover: (D) -> C,
)
: SavableMVBData<LSV, T, D>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
{
    val newConvert: (T) -> D =
        when (val oldConvert = this.convert) {
            null -> convert as (T) -> D
            else -> { t -> convert(oldConvert(t)) }
        }

    val newRecover: (D) -> T =
        when (val oldRecover = this.recover) {
            null -> recover as (D) -> T
            else -> { d -> oldRecover(recover(d)) }
        }

    return (this as SavableMVBData<LSV, T, D>).also {
        it.savedTypeClass = D::class.java
        it.convert = newConvert
        it.recover = newRecover
    }
}

// saveMutableStateFlow


// saveMutableStateFlow