package pers.shawxingkwok.mvb

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope

public val <LSV> LSV.mvbScope: CoroutineScope
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
    get() = ViewModelProvider(this)[MVBViewModel::class.java].viewModelScope

