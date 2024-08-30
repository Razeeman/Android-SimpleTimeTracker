package com.example.util.simpletimetracker.core.view.dayCalendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.util.simpletimetracker.core.utils.CalendarIntersectionCalculator
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import java.util.concurrent.TimeUnit

class DayCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(
    context,
    attrs,
    defStyleAttr,
) {

    private var chartLeftBound: Float = 0f
    private var chartRightBound: Float = 0f
    private var chartTopBound: Float = 0f
    private var chartBottomBound: Float = 0f
    private var chartHeight: Float = 0f
    private var chartWidth: Float = 0f

    private val recordCornerRadius: Float = 2.dpToPx().toFloat()
    private val dayInMillis = TimeUnit.DAYS.toMillis(1)
    private val hourInMillis = TimeUnit.HOURS.toMillis(1)
    private val recordPaint: Paint = Paint()
    private val recordBounds: RectF = RectF(0f, 0f, 0f, 0f)
    private var data: List<Data> = emptyList()

    init {
        initPaint()
        initEditMode()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = resolveSize(0, widthMeasureSpec)
        val h = resolveSize(w, heightMeasureSpec)

        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()

        calculateDimensions(w, h)
        drawData(canvas, data)
    }

    fun setData(viewData: DayCalendarViewData) {
        data = viewData.data.let(::processData)
        invalidate()
    }

    private fun initPaint() {
        recordPaint.apply {
            isAntiAlias = true
        }
    }

    private fun calculateDimensions(w: Float, h: Float) {
        chartLeftBound = 0f
        chartRightBound = w
        chartWidth = chartRightBound - chartLeftBound

        chartTopBound = 0f
        chartBottomBound = h
        chartHeight = chartBottomBound - chartTopBound
    }

    private fun drawData(
        canvas: Canvas,
        data: List<Data>,
    ) {
        var boxHeight: Float
        var boxShift: Float
        var boxWidth: Float
        var boxLeft: Float
        var boxRight: Float
        var boxTop: Float
        var boxBottom: Float

        data.forEach { item ->
            recordPaint.color = item.point.data.color
            boxHeight = chartHeight / item.columnCount
            boxWidth = chartWidth * (item.point.end - item.point.start) / dayInMillis
            boxShift = chartWidth * item.point.start / dayInMillis
            boxLeft = chartLeftBound + boxShift
            boxRight = boxLeft + boxWidth
            boxTop = chartTopBound + boxHeight * (item.columnNumber - 1)
            boxBottom = boxTop + boxHeight

            recordBounds.set(
                boxLeft,
                boxTop,
                boxRight,
                boxBottom,
            )
            canvas.drawRoundRect(
                recordBounds,
                recordCornerRadius,
                recordCornerRadius,
                recordPaint,
            )
        }
    }

    private fun initEditMode() {
        if (isInEditMode) {
            var currentStart = 0L
            (0 until 5)
                .map {
                    currentStart += hourInMillis * it
                    val start = currentStart
                    val end = currentStart + hourInMillis * (it + 1)
                    DayCalendarViewData.Point(
                        start = start,
                        end = end,
                        data = DayCalendarViewData.Point.Data(
                            color = Color.RED,
                        ),
                    )
                }.let {
                    DayCalendarViewData(data = it)
                }.let(::setData)
        }
    }

    private fun processData(data: List<DayCalendarViewData.Point>): List<Data> {
        val res = mutableListOf<Data>()

        // Raw data.
        data.forEach { point ->
            res += Data(point)
        }

        // Calculate intersections.
        res.map {
            CalendarIntersectionCalculator.Data(
                start = it.point.start,
                end = it.point.end,
                point = it,
            )
        }.let(
            CalendarIntersectionCalculator::execute,
        ).forEach {
            it.point.columnCount = it.columnCount
            it.point.columnNumber = it.columnNumber
        }

        return res
    }

    private inner class Data(
        val point: DayCalendarViewData.Point,
        // Set after the fact.
        var columnCount: Int = 1,
        var columnNumber: Int = 1,
    )
}