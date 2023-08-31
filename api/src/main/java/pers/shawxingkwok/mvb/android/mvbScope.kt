package pers.shawxingkwok.mvb.android

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope

public val <LVS> LVS.mvbScope: CoroutineScope
    where LVS: LifecycleOwner, LVS: ViewModelStoreOwner, LVS: SavedStateRegistryOwner
    get() = ViewModelProvider(this)[MVBViewModel::class.java].viewModelScope