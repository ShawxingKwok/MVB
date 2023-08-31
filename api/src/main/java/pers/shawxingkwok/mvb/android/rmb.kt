package pers.shawxingkwok.mvb.android

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner

public fun <LVS, T> LVS.rmb(initialize: (() -> T)? = null): MVBData<LVS, T>
    where LVS: LifecycleOwner, LVS: ViewModelStoreOwner, LVS: SavedStateRegistryOwner
=
    MVBData(this, initialize)