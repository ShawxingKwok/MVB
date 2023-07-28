package pers.shawxingkwok.myapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.MVBFragment
import pers.shawxingkwok.myapplication.R

class HomeFragment : MVBFragment(R.layout.fragment_home) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewLifecycleOwnerLiveData.observe(this){
            KLog.d("")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        KLog.d("")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        KLog.d("")
    }

    override fun onStart() {
        super.onStart()
        KLog.d("")
    }

    override fun onResume() {
        super.onResume()
        KLog.d("")

        // BlankFragment.navigate(findNavController(), "a", "b")
        // printValue(FragmentHomeBinding::inflate)
    }
}