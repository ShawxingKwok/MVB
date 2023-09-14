package pers.shawxingkwok.mvb

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.junit.Test
import pers.shawxingkwok.mvb.android.convertToActual

internal class ConvertToActual {
    @Parcelize
    class P : Parcelable

    @Test
    fun start(){
        val arr = arrayOf<Parcelable>()
        val newArr = arr.convertToActual(P::class.java)
        assert(newArr.javaClass.componentType == P::class.java)
    }
}