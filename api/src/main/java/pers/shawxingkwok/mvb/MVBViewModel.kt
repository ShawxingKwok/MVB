package pers.shawxingkwok.mvb

import androidx.lifecycle.ViewModel

internal class MVBViewModel : ViewModel() {
    var firstBuilt = true
    val data = mutableMapOf<String, Any?>()
}