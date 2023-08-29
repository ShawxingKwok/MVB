package pers.shawxingkwok.test

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dylanc.viewbinding.nonreflection.binding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.android.observe
import pers.shawxingkwok.mvb.android.rmb
import pers.shawxingkwok.mvb.android.saveMutableSharedFlow
import pers.shawxingkwok.test.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(R.layout.fragment_first) {
    private val binding by binding(FragmentFirstBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        if (savedInstanceState == null) {
            val ps = listOf(arrayOf<P>(P(1), P(2)))
            sharedFlow.tryEmit(ps)
        }else
            KLog.d("on restore ${sharedFlow.replayCache}")
    }

    val sharedFlow by saveMutableSharedFlow<_, List<Array<P>>>(replay = 2, parcelableKClass = P::class)
}

@Parcelize
data class P(val x: Int) : Parcelable