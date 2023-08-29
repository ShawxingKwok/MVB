package pers.shawxingkwok.mvb

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pers.shawxingkwok.mvb.android.*

private val _flow = flowOf(1)
private val _livedata = MutableLiveData(1)

internal class TestActivity : AppCompatActivity() {
    private fun assert(v: Int){
        assert(v == 1)
        assert(lifecycle.currentState == Lifecycle.State.STARTED)
    }

    private val x by rmb { flowOf(1) }.observe { assert(it) }
    private val y by saveMutableStateFlow { 1 }.observe { assert(it) }
    private val z by saveMutableSharedFlow<_, Int>().observe { assert(it) }
    private val a by saveMutableLiveData { 1 }.observe { assert(it) }

    private val flow by ::_flow.observe { assert(it) }
    private val livedata by ::_livedata.observe { assert(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        z.tryEmit(1)
    }
}

@RunWith(AndroidJUnit4::class)
internal class ActivityObserveTest {

    @get:Rule
    val rule = ActivityScenarioRule(TestActivity::class.java)

    @Test
    fun start(){
        rule.scenario.onActivity {
        }
    }
}