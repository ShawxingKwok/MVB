package pers.shawxingkwok.mvb.demo

// App.context is static in this case. You could also get it via the dependency injection.
object StopwatchUtil {
    val whiteGrey = App.context.resources.getColor(R.color.white_grey, null)
    val lightRed = App.context.resources.getColor(R.color.light_red, null)
    val lightGreen = App.context.resources.getColor(R.color.light_green, null)
    val white = App.context.resources.getColor(R.color.white, null)

    // format like from 10 to 00:00.01
    fun formatDuration(duration: Int): String{
        val percentSec = duration % 100
        var sec = duration / 100
        val min = sec / 60
        sec %= 60

        // The situation of an hour more is not considered.
        val (minText, secText, percentSecondText) =
            listOf(min, sec, percentSec)
            .map { i -> if (i < 10) "0$i" else "$i" }

        return "$minText:$secText.$percentSecondText"
    }
}