package pers.shawxingkwok.mvb

import kotlin.reflect.KProperty1

public class PivotArg<T>(
    internal val prop: KProperty1<out MVBFragment, T>,
    internal val value: T,
)