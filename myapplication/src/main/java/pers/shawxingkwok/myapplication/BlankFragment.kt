package pers.shawxingkwok.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import pers.shawxingkwok.mvb.MVBFragment
import pers.shawxingkwok.mvb.PivotArg

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BlankFragment : MVBFragment() {
    // TODO: Rename and change types of parameters
    private val param1: String by save()
    private val param2: String by save()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tv_blank).text =
            """
            param1: $param1 
            param2: $param2
            """
            .trimIndent()
    }

    companion object {
        fun navigate(navController: NavController, param1: String, param2: String) {
            navController.navigate(R.id.navigation_blank)

            pivotArgs = listOf(
                PivotArg(BlankFragment::param1, param1),
                PivotArg(BlankFragment::param2, param2)
            )
        }
    }
}