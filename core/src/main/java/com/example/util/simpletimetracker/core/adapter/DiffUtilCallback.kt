package com.example.util.simpletimetracker.core.adapter

import androidx.recyclerview.widget.DiffUtil

class DiffUtilCallback(
    private val oldList: List<ViewHolderType>,
    private val newList: List<ViewHolderType>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int =
        oldList.size

    override fun getNewListSize(): Int =
        newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].areItemsTheSame(newList[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].areContentsTheSame(newList[newItemPosition])

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
        oldList[oldItemPosition].getChangePayload(newList[newItemPosition])
}