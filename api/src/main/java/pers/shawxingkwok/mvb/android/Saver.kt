package pers.shawxingkwok.mvb.android

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import pers.shawxingkwok.ktutil.updateIf
import java.util.*
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
        MLog.d("Write $v in parcel.")
    }

    companion object CREATOR : Parcelable.Creator<Saver> {
        var parcelableLoader: ClassLoader? = null
        var arrayClass: Class<Array<*>>? = null
        var recover: ((Any?) -> Any?)? = null

        override fun createFromParcel(parcel: Parcel): Saver {
            @SuppressLint("ParcelClassLoader")
            val tag = parcel.readValue(null)

            val value: Any? =
                if (tag == null)
                    UNINITIALIZED
                else
                    parcel.readValue(parcelableLoader)
                    .updateIf({ arrayClass != null }){
                        Arrays.copyOf(it as Array<*>, it.size, arrayClass!!)
                    }
                    .updateIf({ recover != null }, recover!!)

            return Saver(value)
        }

        override fun newArray(size: Int): Array<Saver?> = arrayOfNulls(size)
    }
}