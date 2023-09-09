package pers.shawxingkwok.mvb.android

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner

/**
 * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/mvb/android/#rmb).
 */
public fun <LV, T> LV.rmb(initialize: (() -> T)? = null): MVBData<LV, T>
    where LV: LifecycleOwner, LV: ViewModelStoreOwner
=
    MVBData(this, initialize)