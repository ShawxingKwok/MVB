package pers.shawxingkwok.mvb.android

import android.os.Parcelable
import java.util.*
import kotlin.reflect.KClass

// TODO(rename with `convertIfIsParcelableArray` in the next version)
@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun <T> T.convertParcelableArrayIfNeeded(parcelableComponent: KClass<out Parcelable>?): T =
    if (this is Array<*> && javaClass.componentType == Parcelable::class.java) {
        requireNotNull(parcelableComponent){
            "The parcelable component misses."
        }
        val newArrClass = java.lang.reflect.Array.newInstance(parcelableComponent.java, size).javaClass as Class<out Array<*>>
        Arrays.copyOf(this, size, newArrClass) as T
    }
    else
        this