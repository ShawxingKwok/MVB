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
    private var initialize: (() -> T)? = null
)
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
{
    internal val actionsOnDelegate = mutableListOf<(LSV, String, () -> T) -> Unit>()

    internal open fun getValueOnNew(thisRef: LSV, key: String): Pair<Boolean, T?> = false to null

    public operator fun provideDelegate(thisRef: LSV, property: KProperty<*>) : ReadWriteProperty<LSV, T>{
        val isMutable = property is KMutableProperty<*>

        val propPath = thisRef.javaClass.canonicalName!! + "." + property.name

        require(isMutable || initialize != null){
            "$propPath can't be mutable with a null `initialize`."
        }

        var t: T? = null

        val key = MVBData::class.qualifiedName + "#" + propPath

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

        fun initializeIfNotEver() {
            if (state.isInitialized) return

            val v = vm.getValue(key)

            t =
                if (v !== UNINITIALIZED) {
                    MLog.d("$propPath get $v from MVBViewModel.")
                    v as T
                } else {
                    val (exists, ret) = getValueOnNew(thisRef, key)

                    if (exists)
                        ret as T
                    else {
                        if (initialize == null)
                            error(
                                "At $propPath, the lambda 'initialize' is null, which means you should set " +
                                        "the value before you get it."
                            )

                        initialize!!.invoke()
                    }
                }
                .also { vm.setValue(key, it) }

            state.isInitialized = true
            initialize = null
            MLog.d("$propPath is initialized.")
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
                    initialize = null
                    t = value
                    vm.setValue(key, value)
                }

                if (isSynchronized)
                    synchronized(this, ::setValue)
                else
                    setValue()
            }
        }
        .also { delegate ->
            actionsOnDelegate.forEach { onDelegate ->
                onDelegate(thisRef, key){
                    delegate.getValue(thisRef, property)
                }
            }
        }
    }
}