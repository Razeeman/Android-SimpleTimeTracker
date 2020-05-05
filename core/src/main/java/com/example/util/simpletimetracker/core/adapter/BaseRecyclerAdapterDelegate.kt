package com.example.util.simpletimetracker.core.adapter

import android.view.ViewGroup

abstract class BaseRecyclerAdapterDelegate {

    abstract fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder
}