package pers.shawxingkwok.mvb

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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

    protected abstract fun getInitialValue(): T

    protected abstract fun putValue(key: String, value: T)

    private fun initializeIfNotEver(){
        if (!isInitialized) {
            t = getInitialValue()
            putValue(key, t as T)
            isInitialized = true
        }
    }

    final override fun onDelegate(thisRef: L, property: KProperty<*>) {
        key = thisRef.javaClass.canonicalName!! + "." + property.name

        thisRef.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                initializeIfNotEver()
            }
        })
        actionsOnDelegate.forEach { it(thisRef, property) }
    }

    final override fun getValue(thisRef: L, property: KProperty<*>): T {
        initializeIfNotEver()
        return value
    }

    final override fun setValue(thisRef: L, property: KProperty<*>, value: T) {
        isInitialized = true
        t = value
        putValue(key, value)
    }

    private val actionsOnDelegate = mutableListOf<(L, KProperty<*>) -> Unit>()

    public fun extend(act: (L, KProperty<*>) -> Unit): MVBData<L, T> {
        actionsOnDelegate += act
        return this
    }
}