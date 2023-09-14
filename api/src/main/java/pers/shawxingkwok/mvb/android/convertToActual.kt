package pers.shawxingkwok.mvb.android

import android.os.Parcelable

@Suppress("UNCHECKED_CAST")
public fun Array<Parcelable>.convertToActual(parcelableJavaComponent: Class<out Parcelable>): Array<out Parcelable> {
    val newArr = java.lang.reflect.Array.newInstance(parcelableJavaComponent, size) as Array<Any?>

    for (i in newArr.indices)
        newArr[i] = get(i)

    return newArr as Array<out Parcelable>
}