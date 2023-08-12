package pers.shawxingkwok.mvb

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import pers.shawxingkwok.ktutil.KReadWriteProperty
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.ktutil.getOrPutNullable
import kotlin.reflect.KProperty

@Suppress("UnusedReceiverParameter")
public fun <LSV, T> LSV.rmb(initialize: () -> T): MVBData<LSV, T>
    where LSV: LifecycleOwner, LSV: SavedStateRegistryOwner, LSV: ViewModelStoreOwner
=
    object : MVBData<LSV, T>(initialize) { }