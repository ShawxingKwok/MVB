package pers.shawxingkwok.mvb

import androidx.lifecycle.*
import pers.shawxingkwok.ktutil.KReadWriteProperty
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.ktutil.getOrPutNullable
import kotlin.reflect.KProperty

@Suppress("UnusedReceiverParameter")
public fun <LV, T> LV.rmb(initialize: () -> T): KReadWriteProperty<LV, T>
    where LV: LifecycleOwner,
          LV: ViewModelStoreOwner
=
    object : MVBData<LV, T>(){
        val vm by fastLazy { ViewModelProvider(thisRef)[MVBViewModel::class.java] }

        override fun getInitialValue(): T =
            if (key in vm.data)
                vm.data[key] as T
            else
                initialize()

        override fun putValue(key: String, value: T) {
            vm.data[key] = value
        }
    }