package pers.shawxingkwok.mvb

import org.junit.Test

import org.junit.Assert.*
import pers.shawxingkwok.ktutil.KReadOnlyProperty
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun delegate(): ReadOnlyProperty<Any?, Int> = object : KReadOnlyProperty<Any?, Int> {
    override fun onDelegate(thisRef: Any?, property: KProperty<*>) {
        println(0)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return 1
    }
}

fun main() {
    val s by delegate()
    s
}