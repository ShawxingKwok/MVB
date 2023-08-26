package pers.shawxingkwok.mvb.android

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import pers.shawxingkwok.androidutil.KLog
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
    internal val actionsOnDelegate = mutableListOf<(LSV, String, () -> T) -> Unit>()

    internal lateinit var key: String
        private set

    internal open val saver: Saver? = null

    public operator fun provideDelegate(thisRef: LSV, property: KProperty<*>) : ReadWriteProperty<LSV, T>{
        val isMutable = property is KMutableProperty<*>

        val propPath = thisRef.javaClass.canonicalName!! + "." + property.name

        require(isMutable || initialize != null){
            "$propPath can't be immutable with a null `initialize`."
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

        fun initializeIfNotEver() {
            if (state.isInitialized) return

            requireNotNull(initialize){
                "At $propPath, the lambda 'initialize' is null, which means you should set " +
                "the value before you get it."
            }

            val saver = saver
            if (saver != null) {
                if (saver.value == UNINITIALIZED)
                    saver.value = initialize.invoke()
            }else {
                val v = vm.getValue(key)

                t =
                    if (v !== UNINITIALIZED) {
                        v as T
                    } else
                        initialize.invoke().also { vm.setValue(key, it) }
            }
            state.isInitialized = true
            MLog.d("$propPath is initialized.")
        }

        return object : ReadWriteProperty<LSV, T>{
            val getValue = {
                if (saver != null) saver!!.value.also { MLog.d(it) }
                else t.also { MLog.d(it) }
            }

            override fun getValue(thisRef: LSV, property: KProperty<*>): T =
                when{
                    isSynchronized && isMutable ->
                        synchronized(this){
                            initializeIfNotEver()
                            getValue()
                        }
                    isSynchronized -> {
                        if (!state.isInitialized)
                            synchronized(this, ::initializeIfNotEver)

                        getValue()
                    }
                    else -> {
                        initializeIfNotEver()
                        getValue()
                    }
                } as T

            override fun setValue(thisRef: LSV, property: KProperty<*>, value: T) {
                //todo: consider moving
                fun setValue(){
                    t = value

                    if (saver != null)
                        saver!!.value = value
                    else
                        vm.setValue(key, value)

                    state.isInitialized = true
                }

                if (isSynchronized)
                    synchronized(this, ::setValue)
                else
                    setValue()
            }
        }
        .also { delegate ->
            actionsOnDelegate.forEach {
                it(thisRef, key){
                    delegate.getValue(thisRef, property)
                }
            }
        }
    }
}