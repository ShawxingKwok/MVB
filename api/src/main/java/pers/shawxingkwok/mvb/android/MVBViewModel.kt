package pers.shawxingkwok.mvb.android

import androidx.lifecycle.ViewModel
import pers.shawxingkwok.ktutil.fastLazy
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

internal class MVBViewModel : ViewModel() {
    private object NULL

    private val data = ConcurrentHashMap<String, Any>()

    fun getValue(key: String): Any? =
        when(val v = data[key]){
            null -> UNINITIALIZED
            NULL -> null
            else -> v
        }

    fun setValue(key: String, value: Any?){
        data[key] = value ?: NULL
    }
}