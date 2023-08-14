package pers.shawxingkwok.mvb

import org.junit.Test

import org.junit.Assert.*

fun foo(){}

fun main() {
    Class.forName("pers.shawxingkwok.mvb.ExampleUnitTestKt")
        .declaredMethods
        .toList()
        .joinToString("\n")
        .let(::println)
}