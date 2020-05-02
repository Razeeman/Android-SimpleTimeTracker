package com.example.util.simpletimetracker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.domain.TimePeriod
import kotlinx.android.synthetic.main.item_layout.view.*

class MainAdapter : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    private val items: MutableList<TimePeriod> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(parent)
    }

    override fun getItemCount(): Int =
        items.size

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun replace(newItems: List<TimePeriod>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun add(newItem: TimePeriod) {
        items.add(newItem)
        notifyItemInserted(items.lastIndex)
    }

    class MainViewHolder(
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
    ) {
        fun bind(item: TimePeriod) = with(itemView) {
            tvItemId.text = item.id.toString()
            tvItemName.text = item.name
            tvItemTimeStarted.text = item.timeStarted.toString()
            tvItemTimeEnded.text = item.timeEnded.toString()
        }
    }
}