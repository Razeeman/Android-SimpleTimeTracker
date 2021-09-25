package com.example.util.simpletimetracker.core.extension

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter

fun RecyclerView.onItemMoved(
    onSelected: (RecyclerView.ViewHolder?) -> Unit = {},
    onClear: (RecyclerView.ViewHolder) -> Unit = {},
    onMoved: (Int, Int) -> Unit = { _, _ -> }
) {
    val dragDirections =
        ItemTouchHelper.DOWN or ItemTouchHelper.UP or ItemTouchHelper.START or ItemTouchHelper.END

    ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(dragDirections, 0) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            onMoved(fromPosition, toPosition)
            (adapter as? BaseRecyclerAdapter)?.apply {
                onMove(fromPosition, toPosition)
            }

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Do nothing
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) onSelected(viewHolder)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            onClear(viewHolder)
        }
    }).attachToRecyclerView(this)
}