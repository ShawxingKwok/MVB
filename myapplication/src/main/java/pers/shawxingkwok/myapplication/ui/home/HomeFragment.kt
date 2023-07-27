package pers.shawxingkwok.myapplication.ui.home

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.MVBFragment
import pers.shawxingkwok.myapplication.BlankFragment
import pers.shawxingkwok.myapplication.R

class HomeFragment : MVBFragment() {
    override fun onResume() {
        super.onResume()
        BlankFragment.navigate(findNavController(), "a", "b")
    }
}