@file:Suppress("DEPRECATION", "UNCHECKED_CAST")

package pers.shawxingkwok.mvb.android

import android.os.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*
import kotlin.reflect.KClass

@Suppress("NAME_SHADOWING")
public class SavableMVBData<LSV, T, C> @PublishedApi internal constructor(
    isSynchronized: Boolean,
    public var parcelableKClass: KClass<out Parcelable>?,
    @PublishedApi internal var savedType: KClass<C & Any>,
    thisRef: LSV,
    initialize: (() -> T)?,
)
: MVBData<LSV, T>(isSynchronized, thisRef, initialize)
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
{
    @PublishedApi internal var convert: ((Any?) -> Any?)? = null
    @PublishedApi internal var recover: ((Any?) -> Any?)? = null

    override val saver by lazy(Saver.CREATOR){
        val bundle = thisRef.savedStateRegistry.consumeRestoredStateForKey(key)

        if (bundle != null) {
            Saver.parcelableLoader = (parcelableKClass ?: savedType.parcelableKClass)?.java?.classLoader
            Saver.arrayClass = savedType.java.takeIf { it.isArray } as Class<Array<*>>?
            Saver.recover = recover

            // update [convert] to remove any possible references to old [thisRef].
            val saver =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    bundle.getParcelable("", Saver::class.java)!!
                else
                    bundle.getParcelable("")!!

            Saver.recover = null
            saver.convert = convert
            saver
        }else
            Saver(UNINITIALIZED, convert)
    }

    init {
        actionsOnDelegate += { thisRef, key, _ ->
            thisRef.savedStateRegistry.registerSavedStateProvider(key){
                Bundle().also { it.putParcelable("", saver) }
            }
        }
    }
}

@PublishedApi
internal val KClass<*>.isParcelableType: Boolean get() = Parcelable::class.java.isAssignableFrom(this.java)

@PublishedApi
internal val KClass<*>.parcelableKClass: KClass<out Parcelable>? get(){
    if (Parcelable::class.java.isAssignableFrom(this.java))
        return this as KClass<out Parcelable>

    val component = java.componentType ?: return null
    if (Parcelable::class.java.isAssignableFrom(component))
        return component.kotlin as KClass<out Parcelable>

    return null
}

public inline fun <LSV, reified T> LSV.save(
    isSynchronized: Boolean = false,
    parcelableKClass: KClass<out Parcelable>? = null,
    noinline initialize: (() -> T)? = null,
)
: SavableMVBData<LSV, T, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    SavableMVBData(
        isSynchronized = isSynchronized,
        parcelableKClass = parcelableKClass.also { require(it?.isParcelableType ?: true) },
        savedType = T::class as KClass<T & Any>,
        thisRef = this,
        initialize = initialize
    )

public inline fun <LSV, T, C, reified D> SavableMVBData<LSV, T, C>.transform(
    noinline convert: (C) -> D,
    noinline recover: (D) -> C,
)
: SavableMVBData<LSV, T, D>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    (this as SavableMVBData<LSV, T, D>).also {
        it.savedType = D::class as KClass<D & Any>

        it.convert = when (val oldConvert = it.convert) {
            null -> convert as (Any?) -> Any?
            else -> { t -> convert(oldConvert(t) as C) }
        }

        it.recover = when (val oldRecover = it.recover) {
            null -> recover as (Any?) -> Any?
            else -> { d -> oldRecover(recover(d as D)) }
        }
    }

public inline fun <LSV, reified T> LSV.saveMutableStateFlow(
    isSynchronized: Boolean = false,
    parcelableKClass: KClass<out Parcelable>? = null,
    noinline initialize: () -> T,
)
: SavableMVBData<LSV, MutableStateFlow<T>, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    save<LSV, MutableStateFlow<T>>(
        isSynchronized = isSynchronized,
        parcelableKClass = parcelableKClass,
        initialize = { MutableStateFlow(initialize()) }
    )
    .transform(
        convert = MutableStateFlow<T>::value,
        recover = ::MutableStateFlow
    )

public inline fun <LSV, reified T> LSV.saveMutableSharedFlow(
    isSynchronized: Boolean = false,
    parcelableKClass: KClass<out Parcelable>? = null,
    replay: Int = 0,
    extraBufferCapacity: Int = 0,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
)
: SavableMVBData<LSV, MutableSharedFlow<T>, List<T>>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    save<LSV, MutableSharedFlow<T>>(
        isSynchronized = isSynchronized,
        parcelableKClass = parcelableKClass,
        initialize = { MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow) }
    )
    .transform<LSV, MutableSharedFlow<T>, MutableSharedFlow<T>, List<T>>(
        convert = { it.replayCache },
        recover = { cache ->
            val flow = MutableSharedFlow<T>(replay, extraBufferCapacity, onBufferOverflow)

            cache.map{
                if(it is Array<*>)
                    Arrays.copyOf(it as Array<*>, it.size, T::class.java as Class<out Array<T>>) as T
                else
                    it
            }
            .forEach(flow::tryEmit)

            flow
        }
    )
    .also {
        if (it.parcelableKClass == null)
            it.parcelableKClass = T::class.parcelableKClass
    }