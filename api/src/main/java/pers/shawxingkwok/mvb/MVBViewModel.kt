package pers.shawxingkwok.mvb

import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner

internal class MVBViewModel : ViewModel() {
    val data = mutableMapOf<String, Any?>()
    val actionsOnSaveInstanceState = mutableListOf<(SavedStateRegistryOwner) -> Unit>()
}