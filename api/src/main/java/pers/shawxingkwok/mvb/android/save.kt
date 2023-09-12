@file:Suppress("DEPRECATION", "UNCHECKED_CAST")

package pers.shawxingkwok.mvb.android

import android.os.*
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.Serializable
import java.util.*
import kotlin.reflect.KClass

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

    override val saver by lazy(Saver.CREATOR){
        val state = vm.state
        when(val bundle = state.get<Bundle>(key)){
            null -> Saver(UNINITIALIZED, convert).also { state[key] = bundleOf("" to it) }
            else -> {
                Saver.parcelableLoader = (parcelableComponent ?: savedType.parcelableComponent)?.java?.classLoader
                Saver.recover = recover

                val saver =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        bundle.getParcelable("", Saver::class.java)!!
                    else
                        bundle.getParcelable("")!!

                Saver.recover = null
                // update [convert] to remove any possible references to old [thisRef].
                saver.convert = convert
                saver
            }
        }
    }
}

@PublishedApi
internal val KClass<*>.parcelableComponent: KClass<out Parcelable>? get(){
    if (Parcelable::class.java.isAssignableFrom(this.java))
        return this as KClass<out Parcelable>

    val component = java.componentType ?: return null
    if (Parcelable::class.java.isAssignableFrom(component))
        return component.kotlin as KClass<out Parcelable>

    return null
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
            cache.forEach(flow::tryEmit)
            flow
        }
    )
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