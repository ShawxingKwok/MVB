package pers.shawxingkwok.mvb.android

import android.os.Parcel
import android.os.Parcelable
import pers.shawxingkwok.ktutil.updateIf
import kotlin.reflect.KClass

internal class Saver(
    private val parcelableComponent: KClass<out Parcelable>?,
    value: Any?,
    var recovered: Boolean,
    var convert: ((Any?) -> Any?)? = null,
)
    : Container(value), Parcelable
{
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeSerializable(parcelableComponent?.java)
        val v: Any? = if (convert != null) convert!!(value) else value
        dest.writeValue(v)
    }

    companion object CREATOR : Parcelable.Creator<Saver> {
        override fun createFromParcel(parcel: Parcel): Saver {
            @Suppress("UNCHECKED_CAST")
            val parcelableJavaComponent =
                // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                //     parcel.readSerializable(Class::class.java.classLoader, Class::class.java) as Class<out Parcelable>?
                // else
                @Suppress("DEPRECATION")
                parcel.readSerializable() as Class<out Parcelable>?

            val value: Any? =
                    parcel.readValue(parcelableJavaComponent?.classLoader)
                    .updateIf({ it is Array<*> && it.javaClass.componentType == Parcelable::class.java }){
                        // convert Parcelable[] to the actual.
                        // However, the inner Parcelable[] can't be parsed here.
                        // This problem could only be fixed by the authority.

                        requireNotNull(parcelableJavaComponent){
                            "The parcelable component misses."
                        }

                        @Suppress("UNCHECKED_CAST")
                        (it as Array<Parcelable>).convertToActual(parcelableJavaComponent)
                    }

            return Saver(parcelableJavaComponent?.kotlin, value, false)
        }

        override fun newArray(size: Int): Array<Saver?> = arrayOfNulls(size)
    }
}