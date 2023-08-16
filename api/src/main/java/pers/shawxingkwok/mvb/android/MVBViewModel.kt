package pers.shawxingkwok.mvb.android

import androidx.lifecycle.ViewModel
import pers.shawxingkwok.ktutil.fastLazy
import java.util.concurrent.ConcurrentHashMap

internal class MVBViewModel : ViewModel() {
    val data = mutableMapOf<String, Any?>()
}

fun main() {
    val map = ConcurrentHashMap<String, Any?>()
    map["s"] = null
    println(map)
}

public var s by fastLazy { 1 }