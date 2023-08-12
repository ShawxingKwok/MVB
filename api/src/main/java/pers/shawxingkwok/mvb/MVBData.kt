package pers.shawxingkwok.mvb

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import pers.shawxingkwok.ktutil.KReadWriteProperty
import pers.shawxingkwok.ktutil.fastLazy
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
public abstract class MVBData<LSV, T>(private val initialize: () -> T) : KReadWriteProperty<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
{
    private var t: T? = null

    protected lateinit var thisRef: LSV

    public val value: T get() = t as T

    protected lateinit var key: String
        private set

    public var isInitialized: Boolean = false
        private set

    private val vm by fastLazy { ViewModelProvider(thisRef)[MVBViewModel::class.java] }

    internal open fun getGetFromOtherSource(): Pair<Boolean, T?> = false to null

    protected open fun putValue(key: String, value: T){
        vm.data[key] = value
    }

    protected fun initializeIfNotEver(){
        if (!isInitialized) {
            t =
                if (key in vm.data)
                    vm.data[key] as T
                else {
                    val (existed, ret) = getGetFromOtherSource()
                    if (existed)
                        ret
                    else
                        initialize()
                }

            putValue(key, t as T)
            isInitialized = true
        }
    }

    final override fun onDelegate(thisRef: LSV, property: KProperty<*>) {
        this.thisRef = thisRef
        key = thisRef.javaClass.canonicalName!! + "." + property.name

        thisRef.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                initializeIfNotEver()
            }
        })
        actionsOnDelegate.forEach { it(thisRef, property) }
    }

    final override fun getValue(thisRef: LSV, property: KProperty<*>): T {
        initializeIfNotEver()
        return value
    }

    final override fun setValue(thisRef: LSV, property: KProperty<*>, value: T) {
        isInitialized = true
        t = value
        putValue(key, value)
    }

    private val actionsOnDelegate = mutableListOf<(LSV, KProperty<*>) -> Unit>()

    public fun extend(act: (LSV, KProperty<*>) -> Unit): MVBData<LSV, T> {
        actionsOnDelegate += act
        return this
    }
}