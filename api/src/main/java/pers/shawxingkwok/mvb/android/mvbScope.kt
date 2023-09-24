package pers.shawxingkwok.mvb.android

import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

/**
 * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/mvb/android/#mvbscope).
 */
public val ViewModelStoreOwner.mvbScope: CoroutineScope get() = getMVBVm().viewModelScope