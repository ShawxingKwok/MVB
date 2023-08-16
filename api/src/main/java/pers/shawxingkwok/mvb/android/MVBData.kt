package pers.shawxingkwok.mvb.android

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

private open class State{
    open var isInitialized: Boolean = false
}

@Suppress("UNCHECKED_CAST")
public open class MVBData<LSV, T> internal constructor(
    private val isSynchronized: Boolean,
    private val thisRef: LSV,
    private val initialize: (() -> T)? = null
)
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
{
    internal lateinit var key: String
        private set

    internal val actionsOnDelegate = mutableListOf<(LSV, KProperty<*>) -> Unit>()

    internal open fun getValueOnNew(thisRef: LSV, key: String): Pair<Boolean, T?> = false to null

    internal val value get() = getValue()

    private lateinit var getValue: () -> T

    internal operator fun provideDelegate(thisRef: LSV, property: KProperty<*>) : ReadWriteProperty<LSV, T>{
        val isMutable = property is KMutableProperty<*>

        val propPath = thisRef.javaClass.canonicalName!! + "." + property.name

        require(isMutable || initialize != null){
            "$propPath can't be mutable with a null `initialize`."
        }

        var t: T? = null

        key = MVBData::class.qualifiedName + "#" + propPath

        val state =
            if (isMutable || !isSynchronized)
                State()
            else
                object : State(){
                    @Volatile
                    override var isInitialized: Boolean = false
                }

        val vm by lazy(thisRef.savedStateRegistry) {
            try {
                ViewModelProvider(thisRef)[MVBViewModel::class.java]
            } catch (e: IllegalStateException) {
                error("Mvb values are kept in a viewModel which is not accessible at the moment.\n$e")
            }
        }

        actionsOnDelegate.forEach { it(thisRef, property) }

        fun initializeIfNotEver() {
            if (state.isInitialized) return

            if (key in vm.data) {
                t = vm.data[key] as T
                return
            }

            val (exists, ret) = getValueOnNew(thisRef, key)

            if (exists)
                t = ret as T
            else {
                if (initialize == null)
                    error(
                        "At $propPath, the lambda 'initialize' is null, which means you should set " +
                                "the value before you get it."
                    )

                t = initialize.invoke()
            }
            vm.data[key] = t
        }

        return object : ReadWriteProperty<LSV, T>{
            override fun getValue(thisRef: LSV, property: KProperty<*>): T =
                when{
                    isSynchronized && isMutable ->
                        synchronized(this){
                            initializeIfNotEver()
                            t
                        }
                    isSynchronized -> {
                        if (!state.isInitialized)
                            synchronized(this, ::initializeIfNotEver)
                        t
                    }
                    else -> {
                        initializeIfNotEver()
                        t
                    }
                } as T

            override fun setValue(thisRef: LSV, property: KProperty<*>, value: T) {
                //todo: consider moving
                fun setValue(){
                    state.isInitialized = true
                    t = value
                    vm.data[key] = value
                }

                if (isSynchronized)
                    synchronized(this, ::setValue)
                else
                    setValue()
            }
        }
        .also {
            getValue = {
                it.getValue(thisRef, property)
            }
        }
    }
}