package com.example.util.simpletimetracker.core.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

open class BaseRecyclerAdapter : RecyclerView.Adapter<BaseRecyclerViewHolder>() {

    protected val delegates: MutableMap<Int, BaseRecyclerAdapterDelegate> =
        mutableMapOf()

    private val items: MutableList<ViewHolderType> =
        mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder =
        delegates[viewType]!!.onCreateViewHolder(parent)

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
        items[position].getViewType()

    fun replace(newItems: List<ViewHolderType>) {
        val oldItems = items.toList()
        items.clear()
        items.addAll(newItems)
        DiffUtil.calculateDiff(DiffUtilCallback(oldItems, items))
            .dispatchUpdatesTo(this)
    }
}