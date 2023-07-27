package pers.shawxingkwok.mvb

import pers.shawxingkwok.ktutil.KReadWriteProperty
import kotlin.reflect.KProperty

public sealed class MVBData<T> : KReadWriteProperty<MVBFragment, T> {
    internal lateinit var name: String
        private set

    internal lateinit var prop: KProperty<*>
        private set

    internal var value: T? = null

    override fun onDelegate(thisRef: MVBFragment, property: KProperty<*>) {
        prop = property
        name = thisRef.javaClass.canonicalName!! + "." + property.name
    }

    override fun getValue(thisRef: MVBFragment, property: KProperty<*>): T {
        @Suppress("UNCHECKED_CAST")
        return try {
            value as T
        } catch (e: ClassCastException) {
            if (value == null)
                error("$name has not been initialized.")
            else
                throw e
        }
    }

    override fun setValue(thisRef: MVBFragment, property: KProperty<*>, value: T) {
        this.value = value
    }

    internal class Rmb<T>(internal val initialize: () -> T) : MVBData<T>()
    internal open class Save<T>(internal val initialize: (() -> T)? = null) : MVBData<T>()
    internal class ConvertibleSave<T, S>(
        initialize: (() -> T)? = null,
        val convert: (T) -> S,
        val recover: (S) -> T,
    ) : Save<T>(initialize)
}