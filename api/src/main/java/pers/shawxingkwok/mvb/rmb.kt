package pers.shawxingkwok.mvb

import androidx.lifecycle.*
import kotlin.reflect.KProperty

/**
 *
 */
@Suppress("UnusedReceiverParameter")
public fun <LV, T> LV.rmb(initialize: () -> T): MVBData<LV, T>
    where LV: LifecycleOwner,
          LV: ViewModelStoreOwner
=
    object : MVBData<LV, T>(initialize){
        lateinit var vm: MVBViewModel

        override fun onDelegate(thisRef: LV, property: KProperty<*>) {
            super.onDelegate(thisRef, property)

            key = thisRef.javaClass.canonicalName!! + "." + property.name

            thisRef.lifecycle.addObserver(object : DefaultLifecycleObserver{
                override fun onCreate(owner: LifecycleOwner) {
                    super.onCreate(owner)
                    vm = ViewModelProvider(thisRef)[MVBViewModel::class.java]
                    if (vm.firstBuilt) {
                        t = initialize()
                        vm.data[key] = t
                    }else{
                        t = vm.data[key]
                    }
                }

                override fun onStart(owner: LifecycleOwner) {
                    super.onStart(owner)
                    vm.firstBuilt = false
                }
            })
        }

        fun requireRightState(thisRef: LV, property: KProperty<*>){
            require(t != UNINITIALIZED){
                "Can't access ${thisRef.javaClass.canonicalName}.${property.name} before or in onCreate."
            }
        }

        override fun getValue(thisRef: LV, property: KProperty<*>): T {
            requireRightState(thisRef, property)
            @Suppress("UNCHECKED_CAST")
            return t as T
        }

        override fun setValue(thisRef: LV, property: KProperty<*>, value: T) {
            requireRightState(thisRef, property)
            t = value
            vm.data[key] = value
        }
    }