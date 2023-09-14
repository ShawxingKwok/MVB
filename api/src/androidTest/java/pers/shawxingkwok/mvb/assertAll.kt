package pers.shawxingkwok.mvb

internal inline fun <T> assertAll(vararg args: T, act: (T) -> Unit){
    val throwables = mutableListOf<Pair<T, Throwable>>()

    args.forEach {
        try {
            act(it)
        }catch (t: Throwable){
            throwables += it to t
        }
    }

    assert(throwables.none()){
        throwables.joinToString(prefix = "\n", separator = "\n")
    }
}