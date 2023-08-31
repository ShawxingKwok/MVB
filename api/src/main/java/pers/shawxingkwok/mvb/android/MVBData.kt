package pers.shawxingkwok.mvb.android

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import java.lang.NullPointerException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
public open class MVBData<LVS, T> internal constructor(
    private val thisRef: LVS,
    private val initialize: (() -> T)? = null
)
    where LVS: LifecycleOwner, LVS: ViewModelStoreOwner, LVS: SavedStateRegistryOwner
{
    internal val actionsOnDelegate = mutableListOf<(LVS, KProperty<*>, String, () -> T) -> Unit>()

    internal lateinit var key: String
        private set

    internal open val saver: Saver? = null

    @Volatile
    private var isInitialized: Boolean = false

    public operator fun provideDelegate(thisRef: LVS, property: KProperty<*>) : ReadWriteProperty<LVS, T>{
        val isMutable = property is KMutableProperty<*>

        val propPath = thisRef.javaClass.canonicalName!! + "." + property.name

        require(isMutable || initialize != null){
            "$propPath can't be immutable with a null `initialize`."
        }

        var t: T? = null

        key = MVBData::class.qualifiedName + "#" + propPath

        val vm by lazy(thisRef.savedStateRegistry) {
            try {
                ViewModelProvider(thisRef)[MVBViewModel::class.java]
            } catch (e: IllegalStateException) {
                error("Mvb values are kept in a viewModel which is not accessible at the moment.\n$e")
            }
        }

        return object : ReadWriteProperty<LVS, T>{
            override fun getValue(thisRef: LVS, property: KProperty<*>): T {
                if (!isInitialized)
                    synchronized(this){
                        if (isInitialized) return@synchronized

                        check(thisRef.savedStateRegistry.isRestored){
                            "All mvb properties must be called after `super.onCreate(savedInstanceState)` " +
                                    "in ${thisRef.javaClass.canonicalName}."
                        }

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

            override fun setValue(thisRef: LVS, property: KProperty<*>, value: T) {
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