package pers.shawxingkwok.mvb.demo

import android.os.*
import android.util.SparseArray
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.parcelize.Parcelize
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.mvb.demo.databinding.ActivityMainBinding
import java.util.*
import java.util.concurrent.atomic.AtomicReference

// arrayOf<Parcelable> -> Array<Msg> : (toList as List<Msg>).toTypedArray()

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

@Parcelize
data class P(val x: Int) : Parcelable

class Container(val value: Any?) : Parcelable{
    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(value)
    }

    companion object CREATOR : Parcelable.Creator<Container> {
        val classLoaderRef = AtomicReference<ClassLoader>()

        override fun createFromParcel(parcel: Parcel): Container {
            val classLoader = classLoaderRef.getAndSet(null)
            return Container(parcel.readValue(classLoader))
        }

        override fun newArray(size: Int): Array<Container?> {
            return arrayOfNulls(size)
        }
    }

}