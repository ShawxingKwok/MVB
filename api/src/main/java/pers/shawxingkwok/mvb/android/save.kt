@file:Suppress("DEPRECATION", "UNCHECKED_CAST")

package pers.shawxingkwok.mvb.android

import android.os.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.Serializable
import java.util.*
import kotlin.reflect.KClass

@Suppress("NAME_SHADOWING")
public class SavableMVBData<LVS, T, C> @PublishedApi internal constructor(
    public var parcelableComponent: KClass<out Parcelable>?,
    @PublishedApi internal var savedType: KClass<C & Any>,
    thisRef: LVS,
    initialize: (() -> T)?,
)
: MVBData<LVS, T>(thisRef, initialize)
    where LVS: LifecycleOwner, LVS: ViewModelStoreOwner, LVS: SavedStateRegistryOwner
{
    @PublishedApi internal var convert: ((Any?) -> Any?)? = null
    @PublishedApi internal var recover: ((Any?) -> Any?)? = null

    override val saver by lazy(Saver.CREATOR){
        val bundle = thisRef.savedStateRegistry.consumeRestoredStateForKey(key)

        if (bundle != null) {
            Saver.parcelableLoader = (parcelableComponent ?: savedType.parcelableComponent)?.java?.classLoader
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
        actionsOnDelegate += { thisRef, _, key, _ ->
            thisRef.savedStateRegistry.registerSavedStateProvider(key){
                Bundle().also { it.putParcelable("", saver) }
            }
        }
    }
}

@PublishedApi
internal val KClass<*>.isParcelableType: Boolean get() = Parcelable::class.java.isAssignableFrom(this.java)

@PublishedApi
internal val KClass<*>.parcelableComponent: KClass<out Parcelable>? get(){
    if (Parcelable::class.java.isAssignableFrom(this.java))
        return this as KClass<out Parcelable>

    val component = java.componentType ?: return null
    if (Parcelable::class.java.isAssignableFrom(component))
        return component.kotlin as KClass<out Parcelable>

    return null
}

public inline fun <LVS, reified T> LVS.save(
    parcelableComponent: KClass<out Parcelable>? = null,
    noinline initialize: (() -> T)? = null,
)
: SavableMVBData<LVS, T, T>
    where LVS: LifecycleOwner, LVS: ViewModelStoreOwner, LVS: SavedStateRegistryOwner
=
    SavableMVBData(
        parcelableComponent = parcelableComponent.also { require(it?.isParcelableType ?: true) },
        savedType = T::class as KClass<T & Any>,
        thisRef = this,
        initialize = initialize
    )

public inline fun <LVS, T, C, reified D> SavableMVBData<LVS, T, C>.transform(
    noinline convert: (C) -> D,
    noinline recover: (D) -> C,
)
: SavableMVBData<LVS, T, D>
    where LVS: LifecycleOwner, LVS: ViewModelStoreOwner, LVS: SavedStateRegistryOwner
=
    (this as SavableMVBData<LVS, T, D>).also {
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

public inline fun <LVS, reified T> LVS.saveMutableStateFlow(
    parcelableComponent: KClass<out Parcelable>? = null,
    noinline initialize: () -> T,
)
: SavableMVBData<LVS, MutableStateFlow<T>, T>
    where LVS: LifecycleOwner, LVS: ViewModelStoreOwner, LVS: SavedStateRegistryOwner
=
    save<LVS, MutableStateFlow<T>>(
        parcelableComponent = parcelableComponent,
        initialize = { MutableStateFlow(initialize()) }
    )
    .transform(
        convert = MutableStateFlow<T>::value,
        recover = ::MutableStateFlow
    )

public inline fun <LVS, reified T> LVS.saveMutableSharedFlow(
    parcelableComponent: KClass<out Parcelable>? = null,
    replay: Int = 0,
    extraBufferCapacity: Int = 0,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
)
: SavableMVBData<LVS, MutableSharedFlow<T>, List<T>>
    where LVS: LifecycleOwner, LVS: ViewModelStoreOwner, LVS: SavedStateRegistryOwner
=
    save<LVS, MutableSharedFlow<T>>(
        parcelableComponent = parcelableComponent,
        initialize = { MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow) }
    )
    .transform<LVS, MutableSharedFlow<T>, MutableSharedFlow<T>, List<T>>(
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
        if (it.parcelableComponent == null)
            it.parcelableComponent = T::class.parcelableComponent
    }

public inline fun <LVS, reified T> LVS.saveMutableLiveData(
    parcelableComponent: KClass<out Parcelable>? = null,
    noinline initialize: (() -> T)? = null,
)
: SavableMVBData<LVS, MutableLiveData<T>, Any?>
    where LVS: LifecycleOwner, LVS: ViewModelStoreOwner, LVS: SavedStateRegistryOwner
=
    save<LVS, MutableLiveData<T>>(
        parcelableComponent = parcelableComponent,
        initialize = {
            if (initialize == null)
                MutableLiveData<T>()
            else
                MutableLiveData(initialize())
        }
    )
    .transform(
        convert = {
            if (!it.isInitialized)
                EMPTY_MUTABLE_LIVE_DATA
            else
                it.value
        },
        recover = {
            if (it === EMPTY_MUTABLE_LIVE_DATA)
                MutableLiveData()
            else
                MutableLiveData(it as T)
        }
    )

@PublishedApi
@Suppress("ClassName")
internal object EMPTY_MUTABLE_LIVE_DATA : Serializable {
    private fun readResolve(): Any = EMPTY_MUTABLE_LIVE_DATA
}