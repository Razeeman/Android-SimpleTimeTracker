package com.example.util.simpletimetracker.feature_views.extension

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.AdapterView
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSpinner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

fun View.measureExactly(size: Int) {
    val specWidth = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
    val specHeight = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
    measure(specWidth, specHeight)
    layout(0, 0, measuredWidth, measuredHeight)
}

fun GridLayoutManager.setSpanSizeLookup(getSpanSize: (position: Int) -> Int) {
    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return getSpanSize(position)
        }
    }
}

fun RecyclerView.addOnScrollListenerAdapter(
    onScrolled: (recyclerView: RecyclerView, dx: Int, dy: Int) -> Unit = { _, _, _ -> Unit },
    onScrollStateChanged: (recyclerView: RecyclerView, newState: Int) -> Unit = { _, _ -> Unit }
) = object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        onScrolled(recyclerView, dx, dy)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        onScrollStateChanged(recyclerView, newState)
    }
}.let(this::addOnScrollListener)