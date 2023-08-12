package pers.shawxingkwok.mvb

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import pers.shawxingkwok.ktutil.KReadWriteProperty
import pers.shawxingkwok.ktutil.fastLazy
import java.io.Serializable

@Suppress("UnusedReceiverParameter", "UNCHECKED_CAST")
public fun <LSV, T> LSV.save(initialize: () -> T): MVBData<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    object : MVBData<LSV, T>(initialize){
        val saveState by fastLazy {
            val stateProvider = SavedStateRegistry.SavedStateProvider { Bundle() }
            thisRef.savedStateRegistry.registerSavedStateProvider("MVB.save", stateProvider)
            stateProvider.saveState()
        }

        override fun getGetFromOtherSource(): Pair<Boolean, T?> {
            val restoredState = thisRef.savedStateRegistry.consumeRestoredStateForKey("MVB.save")
            if (restoredState?.containsKey(key) == true)
                return true to restoredState.get(key) as T
            else
                return false to null
        }

        // TODO consider support customizing with requiring the converting process is fast.
        override fun putValue(key: String, value: T) {
            super.putValue(key, value)

            when (value) {
                null -> {}
                is String -> saveState.putString(key, value)
                is Boolean -> saveState.putBoolean(key, value)
                is Int -> saveState.putInt(key, value)
                is Long -> saveState.putLong(key, value)
                is Float -> saveState.putFloat(key, value)
                is Double -> saveState.putDouble(key, value)
                is Parcelable -> saveState.putParcelable(key, value)
                is Serializable -> saveState.putSerializable(key, value)
                else -> error("Type of $key is not supported for saving.")
            }
        }
    }