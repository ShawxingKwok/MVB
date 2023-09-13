package pers.shawxingkwok.restoretest

import android.os.Parcel
import android.os.Parcelable

data class P(var i: Int) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(i)
    }

    override fun toString(): String {
        return "P($i)"
    }

    companion object CREATOR : Parcelable.Creator<P> {
        override fun createFromParcel(parcel: Parcel): P {
            val i = parcel.readValue(null) as Int
            return P(i)
        }

        override fun newArray(size: Int): Array<P?> {
            return arrayOfNulls(size)
        }
    }
}