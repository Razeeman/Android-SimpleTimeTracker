package com.example.util.simpletimetracker.feature_statistics_detail.customView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.utils.SwipeDetector
import com.example.util.simpletimetracker.core.utils.isHorizontal
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlinx.parcelize.Parcelize

class SeriesCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(
    context,
    attrs,
    defStyleAttr
) {

    private val columnsCount: Int = 26
    private val rowsCount: Int = 7
    private val cellPadding: Float = 0.5f.dpToPx().toFloat()
    private val cellRadius: Float = 4f.dpToPx().toFloat()
    private var cellSize: Float = 0f
    private var cellColor: Int = Color.BLACK
    private var data: List<ViewData> = emptyList()
    private var dataColumnCount: Int = 0
    private var panFactor: Float = 0f
    private var lastPanFactor: Float = 0f
    private var chartFullWidth: Float = 0f

    private val cellPresentPaint: Paint = Paint()
    private val cellNotPresentPaint: Paint = Paint()

    private val swipeDetector = SwipeDetector(
        context = context,
        onSlide = ::onSwipe,
        onSlideStop = ::onSwipeStop
    )

    init {
        initPaint()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(
            superSavedState = superState,
            panFactor = panFactor,
            lastPanFactor = lastPanFactor,
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? SavedState
        super.onRestoreInstanceState(savedState?.superSavedState ?: state)
        panFactor = savedState?.panFactor.orZero()
        lastPanFactor = savedState?.lastPanFactor.orZero()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = resolveSize(0, widthMeasureSpec)
        cellSize = w / columnsCount.toFloat()
        val height: Float = cellSize * rowsCount
        val h = resolveSize(height.roundToInt(), heightMeasureSpec)

        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return

        val w = width.toFloat()
        val h = height.toFloat()

        calculateDimensions()
        drawCells(canvas, w, h)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> handled = true
        }

        return handled or swipeDetector.onTouchEvent(event)
    }

    fun setData(data: List<ViewData>) {
        if (this.data.size != data.size) {
            panFactor = 0f
            lastPanFactor = 0f
        }
        this.data = data
        invalidate()
    }

    fun setCellColor(@ColorInt color: Int) {
        cellColor = color
        initPaint()
        invalidate()
    }

    private fun initPaint() {
        cellPresentPaint.apply {
            isAntiAlias = true
            color = cellColor
            style = Paint.Style.FILL
        }
        cellNotPresentPaint.apply {
            isAntiAlias = true
            color = cellColor
            style = Paint.Style.FILL
            alpha = (255 * 0.3f).toInt()
        }
    }

    private fun calculateDimensions() {
        dataColumnCount = ceil(data.size.toFloat() / rowsCount).toInt()
        chartFullWidth = dataColumnCount * cellSize
    }

    private fun drawCells(canvas: Canvas, w: Float, h: Float) {
        val finalWidth = if (chartFullWidth < w) {
            (w + chartFullWidth) / 2
        } else {
            w
        }

        // Draw from bottom right corner so that current day would be in there.
        canvas.save()
        canvas.translate(finalWidth + panFactor, h)

        data.forEachIndexed { index, point ->
            if (point is ViewData.Dummy) return@forEachIndexed

            val column = index / rowsCount
            val row = index % rowsCount
            canvas.save()
            canvas.translate(
                -(column + 1) * cellSize,
                -(row + 1) * cellSize,
            )
            canvas.drawRoundRect(
                cellPadding,
                cellPadding,
                cellSize - cellPadding,
                cellSize - cellPadding,
                cellRadius,
                cellRadius,
                if (point is ViewData.Present) cellPresentPaint else cellNotPresentPaint,
            )
            canvas.restore()
        }

        canvas.restore()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onSwipe(offset: Float, direction: SwipeDetector.Direction, event: MotionEvent) {
        if (direction.isHorizontal()) {
            parent.requestDisallowInterceptTouchEvent(true)
            panFactor = lastPanFactor + offset
            coercePan()
            invalidate()
        }
    }

    private fun onSwipeStop() {
        parent.requestDisallowInterceptTouchEvent(false)
        lastPanFactor = panFactor
    }

    private fun coercePan() {
        panFactor = panFactor.coerceIn(0f, (chartFullWidth - width).coerceAtLeast(0f))
    }

    sealed interface ViewData {
        object Present : ViewData
        object NotPresent : ViewData
        object Dummy : ViewData
    }

    @Parcelize
    private class SavedState(
        val superSavedState: Parcelable?,
        val panFactor: Float,
        val lastPanFactor: Float,
    ) : BaseSavedState(superSavedState)
}