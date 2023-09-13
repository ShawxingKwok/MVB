package pers.shawxingkwok.mvb.android

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import pers.shawxingkwok.ktutil.updateIf
import kotlin.reflect.KClass

internal class Saver(
    var value: Any?,
    var convert: ((Any?) -> Any?)? = null
)
    : Parcelable
{
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if (value === UNINITIALIZED) return
        dest.writeValue(true)
        val v: Any? = if (convert != null) convert!!(value) else value
        dest.writeValue(v)
    }

    companion object CREATOR : Parcelable.Creator<Saver> {
        private var parcelableComponent: KClass<out Parcelable>? = null
        private var parcelableLoader: ClassLoader? = null
        private var recover: ((Any?) -> Any?)? = null

        fun prepare(parcelableComponent: KClass<out Parcelable>?, recover: ((Any?) -> Any?)?){
            this.parcelableComponent = parcelableComponent
            parcelableLoader = parcelableComponent?.java?.classLoader
            this.recover = recover
        }

        fun clear(){
            parcelableComponent = null
            parcelableLoader = null
            recover = null
        }

        override fun createFromParcel(parcel: Parcel): Saver {
            @SuppressLint("ParcelClassLoader")
            val tag = parcel.readValue(null)

            val value: Any? =
                if (tag == null)
                    UNINITIALIZED
                else
                    parcel.readValue(parcelableLoader)
                    // convert Parcelable[] to the actual.
                    // However, the inner Parcelable[] can't be parsed here.
                    // This problem could only be fixed by the authority.
                    .convertParcelableArrayIfNeeded(parcelableComponent)
                    .updateIf({ recover != null }){ recover!!(it) }

            return Saver(value)
        }

        override fun newArray(size: Int): Array<Saver?> = arrayOfNulls(size)
    }
}