package pers.shawxingkwok.mvb

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

public val <VL> VL.mvbScope: CoroutineScope
    where VL: ViewModelStoreOwner, VL: LifecycleOwner
    get() = ViewModelProvider(this)[MVBViewModel::class.java].viewModelScope