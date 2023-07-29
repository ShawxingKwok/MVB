package pers.shawxingkwok.mvb

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import kotlin.reflect.KProperty

public interface Savable {
    public class MVBDataContainer{
        internal val savedMVBData: MutableList<MVBData<*, *>> = mutableListOf()
    }

    public val mvbDataContainer: MVBDataContainer

    public fun onSaveInstanceState(outState: Bundle) {
        // when (v) {
        //     null -> {}
        //     is String -> outState.putString(mvbData.name, v)
        //     is Boolean -> outState.putBoolean(mvbData.name, v)
        //     is Int -> outState.putInt(mvbData.name, v)
        //     is Long -> outState.putLong(mvbData.name, v)
        //     is Float -> outState.putFloat(mvbData.name, v)
        //     is Double -> outState.putDouble(mvbData.name, v)
        //     is Parcelable -> outState.putParcelable(mvbData.name, v)
        //     is Serializable -> outState.putSerializable(mvbData.name, v)
        //     // is KT serializable ->
        //     else -> error("Type of ${mvbData.name} is not supported for saving.")
        // }
    }
}

@Suppress("UnusedReceiverParameter")
public fun <LS, T> LS.save(initialize: () -> T): MVBData<LS, T>
    where LS: LifecycleOwner, LS: Savable
=
    object : MVBData<LS, T>(initialize){
        override fun getValue(thisRef: LS, property: KProperty<*>): T {
            TODO("Not yet implemented")
        }

        override fun setValue(thisRef: LS, property: KProperty<*>, value: T) {
            TODO("Not yet implemented")
        }
    }