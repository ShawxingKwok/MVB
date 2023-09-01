@file:Suppress("LeakingThis")

package pers.shawxingkwok.mvb.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import pers.shawxingkwok.androidutil.KLog
import pers.shawxingkwok.ktutil.updateIf
import kotlin.math.max

/**
 * See [doc](https://shawxingkwok.github.io/ITWorks/docs/android/util-view/#krecyclerviewadapter)
 */
public abstract class KRecyclerViewAdapter
    : RecyclerView.Adapter<KRecyclerViewAdapter.ViewBindingHolder<ViewBinding>>()
{
    private class DiffCallback(
        private val oldBinders: List<HolderBinder<*, *>>,
        private val newBinders: List<HolderBinder<*, *>>
    )
        : DiffUtil.Callback()
    {
        override fun getOldListSize() = oldBinders.size

        override fun getNewListSize(): Int = newBinders.size

        override fun areItemsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int,
        ): Boolean {
            val oldBinder = oldBinders[oldItemPosition]
            val newBinder = newBinders[newItemPosition]
            return oldBinder.inflate == newBinder.inflate
                    && oldBinder.id == newBinder.id
        }

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int,
        ): Boolean =
            oldBinders[oldItemPosition].contentId == newBinders[newItemPosition].contentId

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return oldBinders[oldItemPosition].contentId
        }
    }

    private val updateCallback = AdapterListUpdateCallback(this)

    private val creators = mutableListOf<HolderCreator<ViewBinding>>()
    private var oldBinders = listOf<HolderBinder<ViewBinding, Any?>>()
    private var newBinders = mutableListOf<HolderBinder<ViewBinding, Any?>>()

    private var isInitialized = false
    private fun initialize(){
        isInitialized = true
        registerProcessRequiredHolderCreators()
        arrangeHolderBinders()
        oldBinders = newBinders
    }

    /**
     * [HolderCreator] could be automatically created for building [ViewBindingHolder]
     * which is the subclass of [ViewHolder]。But there is no initial process. You could
     * register process-required [HolderCreator]s in this function to do some fixed tasks only once.
     * It's more efficient but not essential. I suggest only doing time-consuming tasks here.
     */
    protected open fun registerProcessRequiredHolderCreators(){}

    /**
     * Build [HolderBinder] in this function according to the order.
     */
    protected abstract fun arrangeHolderBinders()

    /**
     * Notifies [KRecyclerViewAdapter] to update.
     *
     * If items are massive or vary quite frequently like stopwatch, and
     * there is no moved item at the moment, you could set [movesDetected]
     * false to accelerate the calculation.
     */
    @MainThread
    public open fun update(movesDetected: Boolean = true) {
        if (!isInitialized) {
            initialize()
            return
        }

        newBinders = mutableListOf()
        arrangeHolderBinders()

        when {
            // fast simple remove all
            newBinders.none() -> updateCallback.onRemoved(0, oldBinders.size)

            // fast simple insert first
            oldBinders.none() -> updateCallback.onInserted(0, newBinders.size)

            else -> {
                val diffCallback = DiffCallback(oldBinders, newBinders)
                val result = DiffUtil.calculateDiff(diffCallback, movesDetected)
                result.dispatchUpdatesTo(updateCallback)
            }
        }

        oldBinders = newBinders
    }

    final override fun getItemViewType(position: Int): Int {
        val binder = newBinders[position]

        return creators.indexOfFirst {
            it.inflate == binder.inflate
        }
        .updateIf({ i ->  i == -1 }) {
            creators += HolderCreator(binder.inflate){}
            return creators.lastIndex
        }
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingHolder<ViewBinding> {
        return creators[viewType].create(parent)
    }

    final override fun onBindViewHolder(holder: ViewBindingHolder<ViewBinding>, position: Int) {}

    final override fun onBindViewHolder(
        holder: ViewBindingHolder<ViewBinding>,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        newBinders[position].onBind(holder, payloads.firstOrNull())
    }

    final override fun getItemCount(): Int {
        if (!isInitialized) initialize()
        return newBinders.size
    }

    public open class ViewBindingHolder<out VB : ViewBinding>(public val binding: VB) : ViewHolder(binding.root)

    /**
     * Usage example
     * ```
     * HolderCreator(ItemXxBinding::inflate){
     *     ...
     * }
     */
    protected open inner class HolderCreator<out VB : ViewBinding> (
        internal val inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB,
        private val process: (holder: ViewBindingHolder<@UnsafeVariance VB>) -> Unit,
    ){
        init {
            require(creators.none{ it.inflate == inflate }){
                "HolderCreators are distinct by 'inflate', but you register repeatedly."
            }
            creators += this
        }

        internal fun create(parent: ViewGroup): ViewBindingHolder<VB> {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = inflate(layoutInflater, parent, false)
            return ViewBindingHolder(binding).also(process)
        }
    }

    /**
     * Usage example
     * ```
     * HolderBinder(ItemXxBinding::inflate, id, contentId){
     *     ...
     * }
     * ```
     * @param inflate is the function inflate in the corresponding ViewBinding subclass.
     * @param id distinguishes among HolderBinders sharing same [inflate].
     * This is suggested null if [inflate] is unique.
     * @param contentId notifies content to update. This is suggested null if the content is fixed.
     * @param onBind does work those in [onBindViewHolder] before.
     */
    protected open inner class HolderBinder<out VB : ViewBinding, out T>(
        internal val inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB,
        internal val id: Any?,
        internal val contentId: T,
        internal val onBind: (holder: ViewBindingHolder<@UnsafeVariance VB>, @UnsafeVariance T?) -> Unit
    ){
        init {
            newBinders.add(this)
        }
    }
}