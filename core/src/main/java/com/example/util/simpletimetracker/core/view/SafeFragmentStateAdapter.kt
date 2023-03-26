package com.example.util.simpletimetracker.core.view

import android.os.Parcelable
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import androidx.viewpager2.adapter.StatefulAdapter
import timber.log.Timber

// Wrapper around FragmentStateAdapter to catch exceptions in restoreState().
class SafeFragmentStateAdapter(
    private val adapter: FragmentStateAdapter,
) : RecyclerView.Adapter<FragmentViewHolder>(), StatefulAdapter {

    init {
        super.setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FragmentViewHolder {
        return adapter.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: FragmentViewHolder, position: Int) {
        adapter.onBindViewHolder(holder, position)
    }

    override fun getItemCount(): Int {
        return adapter.itemCount
    }

    override fun onBindViewHolder(holder: FragmentViewHolder, position: Int, payloads: MutableList<Any>) {
        adapter.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemViewType(position: Int): Int {
        return adapter.getItemViewType(position)
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        adapter.setHasStableIds(hasStableIds)
    }

    override fun getItemId(position: Int): Long {
        return adapter.getItemId(position)
    }

    override fun onViewRecycled(holder: FragmentViewHolder) {
        adapter.onViewRecycled(holder)
    }

    override fun onFailedToRecycleView(holder: FragmentViewHolder): Boolean {
        return adapter.onFailedToRecycleView(holder)
    }

    override fun onViewAttachedToWindow(holder: FragmentViewHolder) {
        adapter.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: FragmentViewHolder) {
        adapter.onViewDetachedFromWindow(holder)
    }

    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        adapter.registerAdapterDataObserver(observer)
    }

    override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        adapter.unregisterAdapterDataObserver(observer)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        adapter.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        adapter.onDetachedFromRecyclerView(recyclerView)
    }

    override fun saveState(): Parcelable {
        return adapter.saveState()
    }

    override fun restoreState(savedState: Parcelable) {
        try {
            adapter.restoreState(savedState)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}