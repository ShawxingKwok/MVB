package pers.shawxingkwok.mvb.demo

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.reflect.KFunction3

@Parcelize
data class Msg(val id: Long, val fromMe: Boolean, val text: String) : Parcelable