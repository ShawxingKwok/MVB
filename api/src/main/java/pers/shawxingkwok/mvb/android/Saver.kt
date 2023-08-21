package pers.shawxingkwok.mvb.android

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import pers.shawxingkwok.ktutil.KReadWriteProperty
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass

internal class Saver(
    var value: Any?,
    var convert: ((Any?) -> Any?)? = null
)
    : Parcelable
{
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        val v: Any? = if (convert != null) convert!!(value) else value
        dest.writeValue(v)
    }

    companion object CREATOR : Parcelable.Creator<Saver> {
        val parcelableLoaderRef = AtomicReference<KClass<out Parcelable>>()

        override fun createFromParcel(parcel: Parcel): Saver =
            parcelableLoaderRef
                .getAndSet(null)
                ?.java
                ?.classLoader
                .let(parcel::readValue)
                .let(::Saver)

        override fun newArray(size: Int): Array<Saver?> = arrayOfNulls(size)
    }
}

public fun <LSV, T, D> LSV.saveWithTransform(
    value: T,
    convert: (T) -> D,
    recover: (D) -> T,
)
: KReadWriteProperty<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
{
    // Bundle().getParcelable()
    TODO()
}