package pers.shawxingkwok.mvb

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import pers.shawxingkwok.ktutil.KReadWriteProperty
import kotlin.reflect.KProperty

public abstract class MVBData<L: LifecycleOwner, T> : KReadWriteProperty<L, T> {
    protected var t: T? = null

    protected lateinit var thisRef: L

    @Suppress("UNCHECKED_CAST")
    public val value: T get() = t as T

    protected lateinit var key: String
        private set

    public var isInitialized: Boolean = false
        private set

    protected abstract fun initialize()

    private fun initializeIfNotEver(){
        if (!isInitialized) {
            initialize()
            isInitialized = true
        }
    }

    protected abstract fun doOnDelegate(thisRef: L, property: KProperty<*>)

    protected abstract fun putValue(key: String, value: T)

    final override fun onDelegate(thisRef: L, property: KProperty<*>) {
        key = thisRef.javaClass.canonicalName!! + "." + property.name

        doOnDelegate(thisRef, property)

        thisRef.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                initializeIfNotEver()
            }
        })

        actionsOnDelegate.forEach { it(thisRef, property) }
    }

    override fun getValue(thisRef: L, property: KProperty<*>): T {
        initializeIfNotEver()
        return value
    }

    override fun setValue(thisRef: L, property: KProperty<*>, value: T) {
        isInitialized = true
        t = value
        putValue(key, value)
    }

    private val actionsOnDelegate = mutableListOf<(L, KProperty<*>) -> Unit>()

    public fun extend(act: (L, KProperty<*>) -> Unit){
        actionsOnDelegate += act
    }
}