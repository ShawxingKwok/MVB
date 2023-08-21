@file:Suppress("DEPRECATION", "UNCHECKED_CAST")

package pers.shawxingkwok.mvb.android

import android.os.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import kotlin.reflect.KClass

@Suppress("NAME_SHADOWING")
public class SavableMVBData<LSV, T, C> @PublishedApi internal constructor(
    isSynchronized: Boolean,
    private val parcelableKClass: KClass<out Parcelable>?,
    thisRef: LSV,
    initialize: (() -> T)?,
    @PublishedApi internal var savedTypeClass: Class<*>,
)
: MVBData<LSV, T>(isSynchronized, thisRef, initialize)
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
{
    @PublishedApi internal var convert: ((Any?) -> Any?)? = null
    @PublishedApi internal var recover: ((Any?) -> Any?)? = null

    private var saver: Saver? = null

    init {
        actionsOnDelegate += { thisRef, key, _ ->
            thisRef.savedStateRegistry.registerSavedStateProvider(key){
                val bundle = thisRef.savedStateRegistry.consumeRestoredStateForKey(key)

                if (bundle != null) {
                    // update [convert] to remove any possible references to old [thisRef].
                    val savedSaver =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            bundle.getParcelable("", Saver::class.java)
                        else
                            bundle.getParcelable("")

                    if (savedSaver != null){
                        savedSaver.convert = convert
                        return@registerSavedStateProvider bundle
                    }
                }

                (bundle ?: Bundle()).apply { putParcelable("", saver) }
            }
        }
    }

    override fun initializeIfNotEver(thisRef: LSV, key: String): Boolean {
        val restoredState = thisRef.savedStateRegistry.consumeRestoredStateForKey(key)

        if (parcelableKClass != null)
            Saver.parcelableLoaderRef.set(parcelableKClass)

        val savedSaver =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                restoredState.getParcelable(key, Saver::class.java)
            else
                restoredState.getParcelable(key)

        if (savedSaver == null)
            savedSaver = Saver(initialize!!())

        else {
            if (recover != null)
                savedSaver.value = recover!!(savedSaver.value)

            true to savedSaver.value as T
        }
        return true
    }

    override fun setValue(value: Any?): Boolean {
        if (saver == null)
            saver = Saver(value)
        else
            saver!!.value = value

        return true
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