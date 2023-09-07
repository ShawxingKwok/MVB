@file:Suppress("unused", "MemberVisibilityCanBePrivate", "PropertyName")

package pers.shawxingkwok.androidutil.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import pers.shawxingkwok.mvb.android.observe
import pers.shawxingkwok.mvb.android.rmb

class Observe {
    class MyViewModel : ViewModel(){
        private val _isRunning = MutableLiveData(false)
        val isRunning: LiveData<Boolean> = _isRunning
    }
    class MyFragment : Fragment(){
        val vm by viewModels<MyViewModel>()

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            vm.isRunning.observe(viewLifecycleOwner){
                // ...
            }
        }
    }

    class X{
        class MyFragment : Fragment(){
            val isRunning by rmb { MutableLiveData(false) }
                .observe {
                    // ...
                }
            // or via Flow
            val _isRunning by rmb { MutableStateFlow(false) }
                .observe {
                    // ...
                }
                // The lambda is active from STARTED by default,
                // whereas you could also change it to RESUMED when observing `Flow`.
                .observe(repeatOnResumed = true) {  }
        }
    }
}