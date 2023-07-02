package com.example.util.simpletimetracker.feature_views.extension

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.View.MeasureSpec
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

fun AppCompatSpinner.onItemSelected(
    onNothingSelected: () -> Unit,
    onPositionSelected: (Int) -> Unit,
) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            onNothingSelected()
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long,
        ) {
            onPositionSelected(position)
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
    end: Int? = null,
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

fun View.getBitmapFromView(): Bitmap {
    val defaultSize by lazy { 100.dpToPx() }
    fun Int.checkValue(): Int = this.takeUnless { it <= 0 } ?: defaultSize

    return Bitmap.createBitmap(
        measuredWidth.checkValue(),
        measuredHeight.checkValue(),
        Bitmap.Config.ARGB_8888
    ).also {
        draw(Canvas(it))
    }
}

fun View.measureExactly(width: Int, height: Int = width) {
    val specWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
    val specHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
    measure(specWidth, specHeight)
    layout(0, 0, measuredWidth, measuredHeight)
}

fun View.measureForSharing(): View {
    // Measure with width matched to screen width and height as wrap content.
    val specWidth = MeasureSpec.makeMeasureSpec(context.resources.displayMetrics.widthPixels, MeasureSpec.EXACTLY)
    val specHeight = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
    measure(specWidth, specHeight)
    layout(0, 0, measuredWidth, measuredHeight)
    return this
}

fun GridLayoutManager.setSpanSizeLookup(getSpanSize: (position: Int) -> Int) {
    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return getSpanSize(position)
        }
    }
}

fun RecyclerView.addOnScrollListenerAdapter(
    onScrolled: (recyclerView: RecyclerView, dx: Int, dy: Int) -> Unit = { _, _, _ -> },
    onScrollStateChanged: (recyclerView: RecyclerView, newState: Int) -> Unit = { _, _ -> },
) = object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        onScrolled(recyclerView, dx, dy)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        onScrollStateChanged(recyclerView, newState)
    }
}.let(this::addOnScrollListener)