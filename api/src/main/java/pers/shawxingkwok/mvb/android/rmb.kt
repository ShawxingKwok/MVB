package pers.shawxingkwok.mvb.android

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner

public fun <LSV, T> LSV.rmb(isSynchronized: Boolean = false, initialize: (() -> T)? = null): MVBData<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    MVBData(isSynchronized, this, initialize)