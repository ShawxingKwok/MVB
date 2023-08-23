package pers.shawxingkwok.myapplication

import android.app.Application
import android.app.KeyguardManager.KeyguardLockedStateListener
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.lifecycleScope
import kotlinx.parcelize.Parcelize
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.myapplication.ui.main.MainFragment
import pers.shawxingkwok.myapplication.ui.main.MainViewModel

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance(1))
                .commitNow()
        }

        if (savedInstanceState == null)
            KLog.d("on first start")
        else {
            val m = savedInstanceState.getParcelable("m", M::class.java)!!
            KLog.d(m.i)
            val p = savedInstanceState.getParcelable("p_", P::class.java)!!
            KLog.d(p.ms)
            KLog.d((p.ms.last() as SubM).a)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("m", M(1))
        outState.putParcelable("p_", P(arrayOf(M(1), SubM("fdfo", 2))))
    }
}

@Parcelize
open class M(val i: Int) : Parcelable

@Parcelize
class SubM(val a: String, val s: Int) : M(1)

class P(val ms: Array<M>) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // dest.writeParcelableArray(ms, flags)
        dest.writeValue(ms)
    }

    companion object CREATOR : Parcelable.Creator<P> {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun createFromParcel(parcel: Parcel): P {
            KLog.d("on create from parcel")
            val ms = parcel.readValue(M::class.java.classLoader) as Array<Parcelable>
            // val ms = parcel.readParcelableArray(M::class.java.classLoader, M::class.java)!!
            return P(ms.map { it as M }.toTypedArray())
        }

        override fun newArray(size: Int): Array<P?> {
            return arrayOfNulls(size)
        }
    }
}