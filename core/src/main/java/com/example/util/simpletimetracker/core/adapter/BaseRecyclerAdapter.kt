package com.example.util.simpletimetracker.core.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class BaseRecyclerAdapter(
    vararg delegatesList: RecyclerAdapterDelegate
) : RecyclerView.Adapter<BaseRecyclerViewHolder>() {

    private val delegates: List<RecyclerAdapterDelegate> = delegatesList.toList()
    private val items: MutableList<ViewHolderType> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder =
        delegates.getOrNull(viewType)?.onCreateViewHolder(parent)
            ?: throw IllegalStateException(getErrorMessage(viewType))

    override fun onBindViewHolder(
        holder: BaseRecyclerViewHolder,
        position: Int
    ) = holder.bind(items[position], emptyList())

    override fun onBindViewHolder(
        holder: BaseRecyclerViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) = holder.bind(items[position], payloads)

    override fun getItemCount(): Int =
        items.size

    override fun getItemViewType(position: Int): Int =
        delegates.indexOfFirst { it.isForValidType(items[position]) }

    fun getItem(position: Int): ViewHolderType? =
        items.getOrNull(position)

    fun onMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
    }

    fun replace(newItems: List<ViewHolderType>) {
        val oldItems = items.toList()
        items.clear()
        items.addAll(newItems)
        DiffUtil.calculateDiff(DiffUtilCallback(oldItems, items))
            .dispatchUpdatesTo(this)
    }

    fun replaceAsNew(newItems: List<ViewHolderType>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun getErrorMessage(viewType: Int): String {
        return "No delegate found for viewType: $viewType items: ${items.map { it::class.java.simpleName }
            .toSet()} delegates: ${delegates.map { it.getViewHolderTypeName() }}"
    }
}