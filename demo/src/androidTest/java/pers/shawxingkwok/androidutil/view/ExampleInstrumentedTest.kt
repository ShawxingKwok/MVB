package pers.shawxingkwok.androidutil.view

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    lateinit var viewHolder: ViewHolder
    @Test
    fun useAppContext() {
        // Context of the app under test.
    }
}

class Transformer(val bundle: Bundle) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeBundle(Bundle())
    }

    companion object CREATOR : Parcelable.Creator<Transformer> {
        override fun createFromParcel(parcel: Parcel): Transformer {
            return Transformer(parcel.readBundle(Transformer::class.java.classLoader)!!)
        }

        override fun newArray(size: Int): Array<Transformer?> {
            return arrayOfNulls(size)
        }
    }
}