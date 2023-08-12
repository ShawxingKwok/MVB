@file:Suppress("UNREACHABLE_CODE")

package pers.shawxingkwok.androidutil.view

import android.graphics.Color
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor

annotation class ArgsReceiver

const val CONSTRUCTOR_INDEX_KEY = "CONSTRUCTOR_INDEX_KEY"

class CMainFragment : Fragment() {
    private var i: Int = 0
    private lateinit var s: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        i = requireArguments().getInt(TODO("MAIN_I_KEY"))
        s = requireArguments().getString(TODO("MAIN_S_KEY"), "")
    }
}

@ArgsReceiver
class MainFragment(i: Int, s: String) : Fragment()
{
    constructor(i: Int) : this(i, "K") {
        // ...
    }

    constructor(s: String) : this(2, s) {
        // ...
    }
}

@ArgsReceiver
class BMainFragment(i: Int, s: String) : Fragment() {
    constructor(i: Int) : this(i, "K") {
        // ...
        if (arguments == null)
            arguments = bundleOf(CONSTRUCTOR_INDEX_KEY to "0", "0" to i)
    }

    constructor(s: String) : this(2, s) {
        // ...
        if (arguments == null)
            arguments = bundleOf(CONSTRUCTOR_INDEX_KEY to "1", "0" to s)
    }

    init {
        if (arguments == null)
            arguments = bundleOf("0" to i, "1" to s)
    }
}

fun rebuild(className: String, savedArgsBundle: Bundle?){
    val kclass = Class.forName(className).kotlin

    if (savedArgsBundle == null || !kclass.hasAnnotation<ArgsReceiver>()) {
        kclass.createInstance()
        return
    }

    val constructor =
        if (savedArgsBundle.containsKey(CONSTRUCTOR_INDEX_KEY)) {
            val i = savedArgsBundle.getInt(CONSTRUCTOR_INDEX_KEY)
            kclass.constructors.elementAt(i)
        } else
            kclass.primaryConstructor!!

    val argKeys = savedArgsBundle.keySet() - CONSTRUCTOR_INDEX_KEY
    val args = argKeys.sorted().map(savedArgsBundle::get).toTypedArray()

    constructor.call(*args)
}

// constructor() : this(0, "")
// constructor(i: Int) : this(i, "K")


// constructor(i: Int) : super(){
//     if (arguments == null)
//         arguments = bundleOf(CONSTRUCTOR_INDEX_KEY to 0, "0" to i)
// }

// somewhere in the official android library when relaunching after being killed
@Suppress("UNREACHABLE_CODE")
fun foo(){
    val bundle: Bundle = TODO()
    val constructorIndex = bundle.getInt(CONSTRUCTOR_INDEX_KEY)
    val constructor = Class.forName("pers.shawxingkwok.androidutil.view.MainFragment")
        .constructors[constructorIndex]

    val args = (bundle.keySet() - CONSTRUCTOR_INDEX_KEY).sorted().map(bundle::get)
    constructor.newInstance(args)
}