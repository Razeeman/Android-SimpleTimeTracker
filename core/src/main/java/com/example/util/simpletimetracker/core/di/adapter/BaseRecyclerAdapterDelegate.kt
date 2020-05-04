package com.example.util.simpletimetracker.core.di.adapter

import android.view.ViewGroup

abstract class BaseRecyclerAdapterDelegate {

    abstract fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder
}