package pers.shawxingkwok.restoretest

import java.io.Serializable

interface Q : Serializable{
    var i: Int
}
class Q1(override var i: Int = 0) : Q

class Q2(override var i: Int = 0) : Q