package pers.shawxingkwok.mvb.android

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

public open class MVBData<LV, T> internal constructor(
    private val thisRef: LV,
    private val initialize: (() -> T)? = null
)
    where LV: LifecycleOwner, LV: ViewModelStoreOwner
{
    internal val actionsOnDelegate = mutableListOf<(LV, KProperty<*>, String, () -> T) -> Unit>()

    internal open fun getContainer(key: String, vm: MVBViewModel, getV: () -> Any?) =
        vm.map.getOrPut(key){
            Container(getV())
        }

    public operator fun provideDelegate(thisRef: LV, property: KProperty<*>) : ReadWriteProperty<LV, T>{
        val propPath = thisRef::class.qualifiedName + "." + property.name

        val isMutable = property is KMutableProperty<*>

        val container by lazy(
            mode = if (isMutable) LazyThreadSafetyMode.NONE else LazyThreadSafetyMode.SYNCHRONIZED
        ){
            getContainer(propPath, thisRef.getMVBVm()) {
                if (isMutable)
                    UNINITIALIZED
                else {
                    requireNotNull(initialize) {
                        "$propPath can't be immutable with a null `initialize`."
                    }
                    initialize.invoke()
                }
            }
        }

        return object : ReadWriteProperty<LV, T>{
            override fun getValue(thisRef: LV, property: KProperty<*>): T {
                if (isMutable && container.value === UNINITIALIZED) {
                    checkNotNull(initialize){
                        "At $propPath, the lambda 'initialize' is null, which means you should set " +
                        "the value before you get it."
                    }
                    container.value = initialize.invoke()
                }
                @Suppress("UNCHECKED_CAST")
                return container.value as T
            }

            override fun setValue(thisRef: LV, property: KProperty<*>, value: T) {
                container.value = value
            }
        }
        .also { delegate ->
            actionsOnDelegate.forEach {
                it(thisRef, property, propPath) {
                    delegate.getValue(thisRef, property)
                }
            }
        }
    }
}