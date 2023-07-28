package pers.shawxingkwok.mvb

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import pers.shawxingkwok.ktutil.KReadWriteProperty
import kotlin.reflect.KProperty

public abstract class MVBData<L: LifecycleOwner, T>(public val initialize: () -> T) : KReadWriteProperty<L, T> {
    protected var t: Any? = UNINITIALIZED

    @Suppress("UNCHECKED_CAST")
    public val value: T get() = t as T

    public val isInitialized: Boolean get() = t !== UNINITIALIZED

    protected lateinit var key: String

    private val actionsOnDelegate = mutableListOf<(L, KProperty<*>) -> Unit>()
    public fun extend(act: (L, KProperty<*>) -> Unit){
        actionsOnDelegate += act
    }

    override fun onDelegate(thisRef: L, property: KProperty<*>) {
        key = thisRef.javaClass.canonicalName!! + "." + property.name
        actionsOnDelegate.forEach { it(thisRef, property) }
    }
}