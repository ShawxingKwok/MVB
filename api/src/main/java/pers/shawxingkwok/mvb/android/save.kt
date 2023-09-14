@file:Suppress("UNCHECKED_CAST")

package pers.shawxingkwok.mvb.android

import android.os.*
import androidx.lifecycle.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import pers.shawxingkwok.ktutil.updateIf
import java.io.Serializable
import java.util.*
import kotlin.reflect.KClass

@PublishedApi
internal val KClass<*>.parcelableComponent: KClass<out Parcelable>? get() {
    fun Class<*>.isParcelable(): Boolean =
        Parcelable::class.java.isAssignableFrom(this)

    return if (this.java.isParcelable())
        this as KClass<out Parcelable>
    else
        java.componentType?.takeIf { it.isParcelable() }?.kotlin as KClass<out Parcelable>?
}

public class SavableMVBData<LV, T, C> @PublishedApi internal constructor(
    public var parcelableComponent: KClass<out Parcelable>?,
    @PublishedApi internal var savedType: KClass<C & Any>,
    thisRef: LV,
    initialize: (() -> T)?,
)
: MVBData<LV, T>(thisRef, initialize)
    where LV: LifecycleOwner, LV: ViewModelStoreOwner
{
    @PublishedApi internal var convert: ((Any?) -> Any?)? = null
    @PublishedApi internal var recover: ((Any?) -> Any?)? = null

    override fun getContainer(key: String, vm: MVBViewModel, getV: () -> Any?): Saver =
        synchronized(vm.state) {
            vm.state.get<Saver>(key)?.let { return@synchronized it }

            val parcelableComponent = parcelableComponent ?: savedType.parcelableComponent
            val saver = Saver(parcelableComponent, getV(), true)
            vm.state[key] = saver
            saver
        }
        .also {
            if (!it.recovered) {
                it.recovered = true
                if (recover != null)
                    it.value = recover!!(it.value)
            }

            // always update [convert] to remove any possible reference to the old [thisRef].
            it.convert = convert
        }
}

/**
 * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/mvb/android/#save).
 */
public inline fun <LV, reified T> LV.save(
    parcelableComponent: KClass<out Parcelable>? = null,
    noinline initialize: (() -> T)? = null,
)
: SavableMVBData<LV, T, T>
    where LV: LifecycleOwner, LV: ViewModelStoreOwner
=
    SavableMVBData(
        parcelableComponent = parcelableComponent,
        savedType = T::class as KClass<T & Any>,
        thisRef = this,
        initialize = initialize
    )

/**
 * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/mvb/android/#transform).
 */
public inline fun <LV, T, C, reified D> SavableMVBData<LV, T, C>.transform(
    noinline convert: (C) -> D,
    noinline recover: (D) -> C,
)
: SavableMVBData<LV, T, D>
    where LV: LifecycleOwner, LV: ViewModelStoreOwner
=
    (this as SavableMVBData<LV, T, D>).also {
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

/**
 * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/mvb/android/#save).
 */
public inline fun <LV, reified T> LV.saveMutableStateFlow(
    parcelableComponent: KClass<out Parcelable>? = null,
    noinline initialize: () -> T,
)
: SavableMVBData<LV, MutableStateFlow<T>, T>
    where LV: LifecycleOwner, LV: ViewModelStoreOwner
=
    save<LV, MutableStateFlow<T>>(
        parcelableComponent = parcelableComponent,
        initialize = { MutableStateFlow(initialize()) }
    )
    .transform(
        convert = MutableStateFlow<T>::value,
        recover = ::MutableStateFlow
    )

/**
 * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/mvb/android/#save).
 */
public inline fun <LV, reified T> LV.saveMutableSharedFlow(
    parcelableComponent: KClass<out Parcelable>? = null,
    replay: Int = 0,
    extraBufferCapacity: Int = 0,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
)
: SavableMVBData<LV, MutableSharedFlow<T>, List<T>>
    where LV: LifecycleOwner, LV: ViewModelStoreOwner
=
    save<LV, MutableSharedFlow<T>>(
        parcelableComponent = parcelableComponent,
        initialize = { MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow) }
    )
    .transform<LV, MutableSharedFlow<T>, MutableSharedFlow<T>, List<T>>(
        convert = { it.replayCache },
        recover = { cache ->
            val flow = MutableSharedFlow<T>(replay, extraBufferCapacity, onBufferOverflow)

            // This function is from "io.github.shawxingkwok:kt-util:1.0.2"
            cache.updateIf({
                val componentType = T::class.java.componentType ?: return@updateIf false
                Parcelable::class.java.isAssignableFrom(componentType)
            }){
                it.map { arr ->
                    val componentType = T::class.java.componentType as Class<out Parcelable>
                    (arr as Array<Parcelable>).convertToActual(componentType) as T
                }
            }
            .forEach(flow::tryEmit)

            flow
        }
    )
    // This part is essential.
    .also {
        if (it.parcelableComponent == null)
            it.parcelableComponent = T::class.parcelableComponent
    }

/**
 * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/mvb/android/#save).
 */
public inline fun <LV, reified T> LV.saveMutableLiveData(
    parcelableComponent: KClass<out Parcelable>? = null,
    noinline initialize: (() -> T)? = null,
)
: SavableMVBData<LV, MutableLiveData<T>, Any?>
    where LV: LifecycleOwner, LV: ViewModelStoreOwner
=
    save<LV, MutableLiveData<T>>(
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