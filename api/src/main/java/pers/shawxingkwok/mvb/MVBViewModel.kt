package pers.shawxingkwok.mvb

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner

internal class MVBViewModel : ViewModel() {
    val data = mutableMapOf<String, Any?>()
    val saveActions = mutableListOf<(SavedStateRegistryOwner) -> Unit>()
    val saveState = Bundle()
}