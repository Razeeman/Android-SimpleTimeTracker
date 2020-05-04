package com.example.util.simpletimetracker.core.di.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewHolder(
    parent: ViewGroup,
    @LayoutRes layoutId: Int
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
) {

    abstract fun bind(item: ViewHolderType)
}