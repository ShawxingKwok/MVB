package pers.shawxingkwok.mvb.android

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner

public fun <LSV, T> LSV.rmb(initialize: (() -> T)? = null): MVBData<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    MVBData(this, initialize)