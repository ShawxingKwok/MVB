package pers.shawxingkwok.mvb

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import pers.shawxingkwok.ktutil.KReadWriteProperty
import pers.shawxingkwok.ktutil.updateIf
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class E : MVBFragment(){
    companion object{
        fun navigate(){
            pivotArgs = listOf(
                PivotArg(E::s, 2)
            )
        }
    }

    val s by save<Int>()
}

public abstract class MVBFragment : Fragment {
    public companion object{
        public var pivotArgs: List<PivotArg<*>> = emptyList()
    }

    public constructor() : super()
    public constructor(@LayoutRes id: Int) : super(id)

    private val allMVBData = mutableListOf<MVBData<*>>()

    protected fun <T> rmb(initialize: () -> T): MVBData<T> =
        MVBData.Rmb(initialize).also { allMVBData += it }

    protected fun <T> save(initialize: (() -> T)? = null): MVBData<T> =
        MVBData.Save(initialize).also { allMVBData += it }

    protected fun <T, S> save(
        initialize: (() -> T)? = null,
        convert: (T) -> S,
        recover: (S) -> T,
    ): MVBData<T> =
        MVBData.ConvertibleSave(initialize, convert, recover).also { allMVBData += it }

    protected fun <T> MVBData<Flow<T>>.observe(collector: FlowCollector<T>): KReadWriteProperty<MVBFragment, Flow<T>> =
        also{
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    it.value!!.collect(collector)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm = ViewModelProvider(this)[MVBViewModel::class.java]

        allMVBData.forEach { mvbData ->
            @Suppress("UNCHECKED_CAST")
            (mvbData as MVBData<Any?>).value = when(mvbData) {
                is MVBData.Rmb ->
                    if (vm.data.none())
                        mvbData.initialize()
                    else
                        vm.data[mvbData.name]

                is MVBData.Save ->
                    if (savedInstanceState == null)
                        if (mvbData.initialize == null) {
                            val pivotArg = pivotArgs.firstOrNull { it.prop == mvbData.prop }
                                ?: error("${mvbData.name} needs to receive an argument when ${this.javaClass.canonicalName} starts.")

                            pivotArg.value
                        } else
                            mvbData.initialize.invoke()
                    else
                        savedInstanceState.get(mvbData.name)
                        .updateIf({ mvbData is MVBData.ConvertibleSave<*, *> }){
                            (mvbData as MVBData.ConvertibleSave<Any?, Any?>).recover(it)
                        }
            }
        }
        pivotArgs = emptyList()
    }

    private lateinit var outState: Bundle

    private fun save(){
        val vm = ViewModelProvider(this)[MVBViewModel::class.java]

        allMVBData.forEach { mvbData ->
            when(mvbData){
                is MVBData.Rmb -> vm.data[mvbData.name] = mvbData.value

                is MVBData.Save -> {
                    val v = when (mvbData) {
                        is MVBData.ConvertibleSave<*, *> ->
                            @Suppress("UNCHECKED_CAST")
                            (mvbData as MVBData.ConvertibleSave<Any?, Any?>)
                                .convert(mvbData.value)

                        else -> mvbData.value
                    }

                    when(v){
                        null -> {}
                        is String -> outState.putString(mvbData.name, v)
                        is Boolean -> outState.putBoolean(mvbData.name, v)
                        is Int -> outState.putInt(mvbData.name, v)
                        is Long -> outState.putLong(mvbData.name, v)
                        is Float -> outState.putFloat(mvbData.name, v)
                        is Double -> outState.putDouble(mvbData.name, v)
                        is Parcelable -> outState.putParcelable(mvbData.name, v)
                        is Serializable -> outState.putSerializable(mvbData.name, v)
                        else -> error("Type of ${mvbData.name} is not supported for saving.")
                    }
                }
            }
        }
    }

    /**
     * Use [save] instead of overriding.
     */
    final override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        this.outState = outState
        save()
    }

    override fun onDetach() {
        super.onDetach()
        save()
    }
}