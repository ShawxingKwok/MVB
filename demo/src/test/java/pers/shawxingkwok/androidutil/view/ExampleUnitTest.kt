package pers.shawxingkwok.androidutil.view

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        var x = 0
        val job = CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.Default) {
                while (true) x++
            }
        }
        runBlocking {
            delay(100)
            job.cancel()
            println(x)
        }
    }
}