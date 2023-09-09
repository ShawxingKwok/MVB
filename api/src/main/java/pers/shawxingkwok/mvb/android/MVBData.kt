package pers.shawxingkwok.mvb.android

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import pers.shawxingkwok.ktutil.fastLazy
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
public open class MVBData<LV, T> internal constructor(
    private val thisRef: LV,
    private val initialize: (() -> T)? = null
)
    where LV: LifecycleOwner, LV: ViewModelStoreOwner
{
    internal val actionsOnDelegate = mutableListOf<(LV, KProperty<*>, String, () -> T) -> Unit>()

    internal lateinit var key: String
        private set

    internal open val saver: Saver? = null

    @Volatile
    private var isInitialized: Boolean = false

    internal val vm by fastLazy(thisRef::getMVBVm)

    public operator fun provideDelegate(thisRef: LV, property: KProperty<*>) : ReadWriteProperty<LV, T>{
        val isMutable = property is KMutableProperty<*>

        val propPath = thisRef::class.qualifiedName + "." + property.name
        key =  propPath

        require(isMutable || initialize != null){
            "$propPath can't be immutable with a null `initialize`."
        }

        var t: T? = null

        return object : ReadWriteProperty<LV, T>{
            override fun getValue(thisRef: LV, property: KProperty<*>): T {
                if (!isInitialized)
                    synchronized(this){
                        if (isInitialized) return@synchronized

                        val saver = saver
                        try {
                            when{
                                saver == null -> {
                                    val v = vm.getValue(key)

                                    t =
                                        if (v !== UNINITIALIZED) {
                                            v as T
                                        } else
                                            initialize!!().also { vm.setValue(key, it) }
                                }

                                saver.value == UNINITIALIZED -> saver.value = initialize!!()
                            }
                        } catch (e: NullPointerException) {
                            checkNotNull(initialize){
                                "At $propPath, the lambda 'initialize' is null, which means you should set " +
                                        "the value before you get it."
                            }
                            throw e
                        }

                        isInitialized = true
                    }

                if (saver != null)
                    return saver!!.value as T
                else
                    return t as T
            }

            override fun setValue(thisRef: LV, property: KProperty<*>, value: T) {
                isInitialized = true

                t = value

                if (saver != null)
                    saver!!.value = value
                else
                    vm.setValue(key, value)
            }
        }
        .also { delegate ->
            actionsOnDelegate.forEach {
                it(thisRef, property, key){
                    delegate.getValue(thisRef, property)
                }
            }
        }
    }
}