@file:Suppress("UnusedReceiverParameter", "UNCHECKED_CAST")
package pers.shawxingkwok.mvb

import android.os.Binder
import android.os.Bundle
import android.os.Parcelable
import android.util.Size
import android.util.SizeF
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.Serializable
import kotlin.reflect.KFunction3

public fun <LSV> LSV.enableMVBSave()
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
{
    val vm = ViewModelProvider(this)[MVBViewModel::class.java]
    vm.actionsOnSaveInstanceState.forEach { it(this) }
}

@PublishedApi
internal fun <LSV, T, C> LSV._save(
    put: KFunction3<Bundle, String, C?, Unit>? = null,
    initialize: (() -> T)?,
    convert: ((T) -> C)?,
    recover: ((C) -> T)?,
)
: MVBData<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    object : MVBData<LSV, T>(initialize){
        override fun getValueOnNew(thisRef: LSV, key: String): Pair<Boolean, T?> =
            when(val restoredState = thisRef.savedStateRegistry.consumeRestoredStateForKey(key)) {
                null -> false to null
                else -> {
                    val value = when(recover){
                        null -> restoredState.get(key) as T
                        else -> (restoredState.get(key) as C).let(recover)
                    }
                    true to value
                }
            }

        override fun onNew(thisRef: LSV, key: String) {
            vm.actionsOnSaveInstanceState += { savedStateRegistryOwner ->
                MLog.d("On register $key")

                val saveState = Bundle()
                savedStateRegistryOwner.savedStateRegistry.registerSavedStateProvider(key){ saveState }

                val value = when(convert){
                    null -> value as C
                    else -> convert(value)
                }

                when {
                    value == null -> saveState.remove(key)
                    put != null -> put(saveState, key, value)
                    value is String -> saveState.putString(key, value)
                    value is Boolean -> saveState.putBoolean(key, value)
                    value is Int -> saveState.putInt(key, value)
                    value is Long -> saveState.putLong(key, value)
                    value is Float -> saveState.putFloat(key, value)
                    value is Double -> saveState.putDouble(key, value)
                    value is Byte -> saveState.putByte(key, value)
                    value is Char -> saveState.putChar(key, value)
                    value is Short -> saveState.putShort(key, value)
                    value is BooleanArray -> saveState.putBooleanArray(key, value)
                    value is IntArray -> saveState.putIntArray(key, value)
                    value is LongArray -> saveState.putLongArray(key, value)
                    value is FloatArray -> saveState.putFloatArray(key, value)
                    value is DoubleArray -> saveState.putDoubleArray(key, value)
                    value is ByteArray -> saveState.putByteArray(key, value)
                    value is CharArray -> saveState.putCharArray(key, value)
                    value is ShortArray -> saveState.putShortArray(key, value)
                    value is Size -> saveState.putSize(key, value)
                    value is SizeF -> saveState.putSizeF(key, value)
                    value is Bundle -> saveState.putBundle(key, value)
                    value is Binder -> saveState.putBinder(key, value)
                    value is CharSequence -> saveState.putCharSequence(key, value)
                    value is Parcelable -> saveState.putParcelable(key, value)
                    value is Serializable -> saveState.putSerializable(key, value)
                    else -> error("Type of $key is not supported to save.")
                }
            }
        }
    }

public fun <LSV, T> LSV.save(
    put: KFunction3<Bundle, String, T?, Unit>? = null,
    initialize: (() -> T)? = null
)
: MVBData<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    _save<LSV, T, T>(put, initialize, null, null)

public fun <LSV, T, C> LSV.save(
    put: KFunction3<Bundle, String, C?, Unit>? = null,
    initialize: (() -> T)? = null,
    convert: (T) -> C,
    recover: (C) -> T,
)
: MVBData<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    _save(put, initialize, convert, recover)

public fun <LSV, T> LSV.saveMutableStateFlow(
    put: KFunction3<Bundle, String, T?, Unit>? = null,
    initialize: () -> T,
)
: MVBData<LSV, MutableStateFlow<T>>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    _save(put, { MutableStateFlow(initialize()) }, { it.value }, ::MutableStateFlow)

public inline fun <LSV, T, C> LSV.saveMutableStateFlow(
    put: KFunction3<Bundle, String, C?, Unit>? = null,
    crossinline initialize: () -> T,
    crossinline convert: (T) -> C,
    crossinline recover: (C) -> T,
)
: MVBData<LSV, MutableStateFlow<T>>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    _save(
        put,
        { MutableStateFlow(initialize()) },
        { convert(it.value) },
        { MutableStateFlow(recover(it)) }
    )

public fun <LSV, T> LSV.saveMutableSharedFlow(
    put: KFunction3<Bundle, String, List<T>?, Unit>? = null,
    replay: Int = 0,
    extraBufferCapacity: Int = 0,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
)
: MVBData<LSV, MutableSharedFlow<T>>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    _save(
        put = put,
        initialize = { MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow) },
        convert = { it.replayCache },
        recover = { replayCache ->
            val flow = MutableSharedFlow<T>()
            replayCache.forEach(flow::tryEmit)
            flow
        }
    )

public inline fun <LSV, T, C> LSV.saveMutableSharedFlow(
    put: KFunction3<Bundle, String, List<C>?, Unit>? = null,
    crossinline convert: (T) -> C,
    crossinline recover: (C) -> T,
    replay: Int = 0,
    extraBufferCapacity: Int = 0,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
)
: MVBData<LSV, MutableSharedFlow<T>>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    _save(
        put = put,
        initialize = { MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow) },
        convert = { it.replayCache.map(convert) },
        recover = { replayCache ->
            val flow = MutableSharedFlow<T>()
            replayCache.map(recover).forEach(flow::tryEmit)
            flow
        }
    )