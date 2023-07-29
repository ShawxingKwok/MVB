package pers.shawxingkwok.mvb

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import pers.shawxingkwok.ktutil.lazyFast
import java.io.Serializable
import kotlin.reflect.KProperty

@Suppress("UnusedReceiverParameter")
public fun <LS, T> LS.save(initialize: () -> T): MVBData<LS, T>
    where LS: LifecycleOwner, LS: SavedStateRegistryOwner
=
    object : MVBData<LS, T>(){
        lateinit var thisRef: LS

        val restoredState by lazyFast {
            thisRef.savedStateRegistry.consumeRestoredStateForKey("MVB.save")
        }

        val saveState by lazyFast{
            val stateProvider = SavedStateRegistry.SavedStateProvider { Bundle() }
            thisRef.savedStateRegistry.registerSavedStateProvider("MVB.save", stateProvider)
            stateProvider.saveState()
        }

        private fun initializeIfNotEver() {
            if (!isInitialized) {
                t =
                    if (restoredState == null)
                        initialize()
                    else
                        @Suppress("UNCHECKED_CAST")
                        restoredState!!.get(key) as T

                isInitialized = true
            }
        }

        override fun doOnDelegate(thisRef: LS, property: KProperty<*>) {
            super.onDelegate(thisRef, property)
            this.thisRef = thisRef

            thisRef.lifecycle.addObserver(object : DefaultLifecycleObserver{
                override fun onCreate(owner: LifecycleOwner) {
                    initializeIfNotEver()
                }
            })
        }

        override fun getValue(thisRef: LS, property: KProperty<*>): T {
            initializeIfNotEver()
            return value
        }

        override fun setValue(thisRef: LS, property: KProperty<*>, value: T) {
            isInitialized = true
            t = value

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
