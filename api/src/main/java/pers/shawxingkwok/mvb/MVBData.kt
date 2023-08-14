package pers.shawxingkwok.mvb

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import pers.shawxingkwok.ktutil.KReadWriteProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
public open class MVBData<LSV, T>(private val initialize: (() -> T)? = null) : KReadWriteProperty<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
{
    private var t: T? = null

    private lateinit var thisRef: LSV
    private lateinit var prop: KProperty<*>
    private lateinit var key: String
    private var isInitialized: Boolean = false

    internal val vm by lazy {
        try {
            ViewModelProvider(thisRef)[MVBViewModel::class.java]
        } catch (e: IllegalStateException) {
            error("Mvb values are kept in a viewModel which is not accessible at the moment.\n$e")
        }
    }

    internal val actionsOnDelegate = mutableListOf<(LSV, KProperty<*>) -> Unit>()

    internal open fun onNew(thisRef: LSV, key: String){}

    internal open fun getValueOnNew(thisRef: LSV, key: String): Pair<Boolean, T?> = false to null

    internal val value: T get() {
        if (!isInitialized) {
            if (key in vm.data) {
                t = vm.data[key] as T
                isInitialized = true
            } else {
                val (existed, ret) = getValueOnNew(thisRef, key)
                val _t =
                    if (existed)
                        ret
                    else {
                        if (initialize == null)
                            error(
                                "An $key, the lambda 'initialize' is null, which means you set " +
                                        "the value before you get it."
                            )

                        initialize.invoke()
                    }

                setValue(thisRef, prop, _t as T) // onNew is called inside
            }
        }
        return t as T
    }

    final override fun onDelegate(thisRef: LSV, property: KProperty<*>) {
        this.thisRef = thisRef
        this.prop = property
        key = thisRef.javaClass.canonicalName!! + "." + property.name
        actionsOnDelegate.forEach { it(thisRef, property) }
    }

    final override fun getValue(thisRef: LSV, property: KProperty<*>): T = value

    override fun setValue(thisRef: LSV, property: KProperty<*>, value: T) {
        if (!isInitialized){
            if (key !in vm.data)
                onNew(thisRef, key)
            isInitialized = true
        }
        t = value
        vm.data[key] = t
    }

    final override fun provideDelegate(thisRef: LSV, property: KProperty<*>): ReadWriteProperty<LSV, T> {
        return super.provideDelegate(thisRef, property)
    }
}