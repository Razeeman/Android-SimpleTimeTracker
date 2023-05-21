package com.example.util.simpletimetracker.feature_base_adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import java.util.Collections

class BaseRecyclerAdapter(
    vararg delegatesList: RecyclerAdapterDelegate,
    diffUtilCallback: DiffUtilCallback = DiffUtilCallback(),
) : ListAdapter<ViewHolderType, BaseRecyclerViewHolder>(diffUtilCallback) {

    private val delegates: List<RecyclerAdapterDelegate> = delegatesList.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder =
        delegates.getOrNull(viewType)?.onCreateViewHolder(parent)
            ?: createLoaderAdapterDelegate().onCreateViewHolder(parent)

    override fun onBindViewHolder(
        holder: BaseRecyclerViewHolder,
        position: Int
    ) = holder.bind(currentList[position], emptyList())

    override fun onBindViewHolder(
        holder: BaseRecyclerViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) = holder.bind(currentList[position], payloads)

    override fun getItemViewType(position: Int): Int =
        delegates.indexOfFirst { it.isForValidType(currentList[position]) }

    fun getItemByPosition(position: Int): ViewHolderType? =
        currentList.getOrNull(position)

    fun onMove(fromPosition: Int, toPosition: Int) {
        val newList = currentList.toList()

        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(newList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(newList, i, i - 1)
            }
        }

        submitList(newList)
    }

    fun replace(newItems: List<ViewHolderType>) {
        submitList(newItems)
    }

    fun replaceAsNew(newItems: List<ViewHolderType>) {
        submitList(emptyList())
        submitList(newItems)
    }
}