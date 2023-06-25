package com.example.util.simpletimetracker.feature_statistics_detail.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import kotlin.math.ceil
import kotlin.math.roundToInt

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

    private val cellPresentPaint: Paint = Paint()
    private val cellNotPresentPaint: Paint = Paint()

    init {
        initPaint()
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

        drawCells(canvas, width.toFloat(), height.toFloat())
    }

    fun setData(data: List<ViewData>) {
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

    private fun drawCells(canvas: Canvas, w: Float, h: Float) {
        val dataColumnCount = ceil(data.size.toFloat() / rowsCount).toInt()
        val finalWidth = if (dataColumnCount < columnsCount) {
            (w + dataColumnCount * cellSize) / 2
        } else {
            w
        }

        // Draw from bottom right corner so that current day would be in there.
        canvas.save()
        canvas.translate(finalWidth, h)

        data.forEachIndexed { index, point ->
            if (point is ViewData.Dummy) return@forEachIndexed

            val column = index / rowsCount
            val row = index % rowsCount
            canvas.save()
            canvas.translate(
                - (column + 1) * cellSize,
                - (row + 1) * cellSize,
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

    sealed interface ViewData {
        object Present : ViewData
        object NotPresent : ViewData
        object Dummy : ViewData
    }
}