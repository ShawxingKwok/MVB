package pers.shawxingkwok.myapplication.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.myapplication.R
import kotlin.concurrent.thread

class MainFragment : Fragment() {

    companion object {
        fun newInstance(i: Int) = MainFragment().also { it.arguments = bundleOf("i" to i) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewLifecycleOwnerLiveData.observe(this){
            KLog.d(it.lifecycle.currentState)
        }

        arguments = bundleOf("s" to "S")
        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
}