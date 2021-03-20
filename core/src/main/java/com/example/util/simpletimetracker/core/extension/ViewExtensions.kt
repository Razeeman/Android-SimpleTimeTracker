package com.example.util.simpletimetracker.core.extension

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.AdapterView
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSpinner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.google.android.material.tabs.TabLayout

var View.visible: Boolean
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }
    get() {
        return visibility == View.VISIBLE
    }

fun View.flipVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

fun View.rotate(from: Float, to: Float, duration: Long = 300) {
    ObjectAnimator.ofFloat(this, "rotation", from, to).apply {
        this.duration = duration
        repeatCount = 0
        interpolator = LinearInterpolator()
        start()
    }
}

fun View.rotateDown() {
    this.rotate(from = 0f, to = 180f)
}

fun View.rotateUp() {
    this.rotate(from = 180f, to = 360f)
}

fun View.setOnClick(listener: (() -> Unit)) {
    setOnClickListener { listener.invoke() }
}

inline fun <T> View.setOnClickWith(item: T, crossinline listener: ((T) -> Unit)) {
    setOnClickListener { listener.invoke(item) }
}

fun View.setOnLongClick(listener: (() -> Unit)) {
    setOnLongClickListener { listener.invoke(); true }
}

inline fun <T> View.setOnLongClickWith(item: T, crossinline listener: ((T) -> Unit)) {
    setOnLongClickListener { listener.invoke(item); true }
}

fun TabLayout.onTabSelected(func: (TabLayout.Tab) -> Unit) {
    this.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
            // Do nothing
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            // Do nothing
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            tab?.let(func::invoke)
        }
    })
}

fun AppCompatSpinner.onItemSelected(func: (Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            // Do nothing
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            func(position)
        }
    }
}

fun SeekBar.onProgressChanged(func: (Int) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) func(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            // Do nothing
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            // Do nothing
        }
    })
}

fun RecyclerView.onItemMoved(
    onSelected: (RecyclerView.ViewHolder?) -> Unit = {},
    onClear: (RecyclerView.ViewHolder) -> Unit = {},
    onMoved: (Int, Int) -> Unit
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
                notifyItemMoved(fromPosition, toPosition)
            }

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Do nothing
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ACTION_STATE_DRAG) onSelected(viewHolder)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            onClear(viewHolder)
        }
    }).attachToRecyclerView(this)
}

fun View.setMargins(
    top: Int? = null,
    bottom: Int? = null,
    start: Int? = null,
    end: Int? = null
) {
    layoutParams = layoutParams.also { params ->
        (params as? ViewGroup.MarginLayoutParams)?.let {
            top?.dpToPx()?.let { params.topMargin = it }
            bottom?.dpToPx()?.let { params.bottomMargin = it }
            start?.dpToPx()?.let { params.marginStart = it }
            end?.dpToPx()?.let { params.marginEnd = it }
        }
    }
}

fun View.updatePadding(
    left: Int = paddingLeft,
    top: Int = paddingTop,
    right: Int = paddingRight,
    bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
}

fun View.getBitmapFromView(): Bitmap {
    return Bitmap.createBitmap(
        measuredWidth,
        measuredHeight,
        Bitmap.Config.ARGB_8888
    ).also {
        draw(Canvas(it))
    }
}