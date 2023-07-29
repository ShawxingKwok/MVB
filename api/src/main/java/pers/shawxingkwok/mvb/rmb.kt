package pers.shawxingkwok.mvb

import androidx.lifecycle.*
import pers.shawxingkwok.ktutil.getOrPutNullable
import pers.shawxingkwok.ktutil.lazyFast
import kotlin.reflect.KProperty

@Suppress("UnusedReceiverParameter")
public fun <LV, T> LV.rmb(initialize: () -> T): MVBData<LV, T>
    where LV: LifecycleOwner,
          LV: ViewModelStoreOwner
=
    object : MVBData<LV, T>(){
        lateinit var thisRef: LV

        val vm by lazyFast{ ViewModelProvider(thisRef)[MVBViewModel::class.java] }

        override var isInitialized: Boolean = false
            private set

        private fun initializeIfNotEver(){
            if (!isInitialized) {
                @Suppress("UNCHECKED_CAST")
                t = vm.data.getOrPutNullable(key){ initialize() } as T
                isInitialized = true
            }
        }

        override fun doOnDelegate(thisRef: LV, property: KProperty<*>) {
            super.onDelegate(thisRef, property)
            this.thisRef = thisRef

            thisRef.lifecycle.addObserver(object : DefaultLifecycleObserver{
                override fun onCreate(owner: LifecycleOwner) {
                    initializeIfNotEver()
                }
            })
        }

        override fun getValue(thisRef: LV, property: KProperty<*>): T {
            initializeIfNotEver()
            return value
        }

        override fun setValue(thisRef: LV, property: KProperty<*>, value: T) {
            isInitialized = true
            t = value
            vm.data[key] = value
        }
    }
