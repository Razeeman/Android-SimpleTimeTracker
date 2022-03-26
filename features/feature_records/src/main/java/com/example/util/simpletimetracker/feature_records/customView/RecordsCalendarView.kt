package com.example.util.simpletimetracker.feature_records.customView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.utils.SingleTapDetector
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_views.IconView
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import java.util.concurrent.TimeUnit

class RecordsCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(
    context,
    attrs,
    defStyleAttr
) {

    // Attrs
    private var legendTextSize: Float = 0f
    private var legendTextColor: Int = 0
    private var legendLineColor: Int = 0
    private var iconMaxSize: Int = 0
    private var recordsCount: Int = 0
    // End of attrs

    private var legendTextWidth: Float = 0f
    private var legendTextHeight: Float = 0f
    private var pixelLeftBound: Float = 0f
    private var pixelRightBound: Float = 0f
    private var chartWidth: Float = 0f
    private val legendTextPadding: Float = 2.dpToPx().toFloat()
    private val recordCornerRadius: Float = 8.dpToPx().toFloat()
    private val dayInMillis = TimeUnit.DAYS.toMillis(1)
    private val hourInMillis = TimeUnit.HOURS.toMillis(1)

    private val recordPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private val linePaint: Paint = Paint()

    private val textBounds: Rect = Rect(0, 0, 0, 0)
    private val bounds: RectF = RectF(0f, 0f, 0f, 0f)
    private var originalData: List<RecordViewData.Tracked> = emptyList()
    private var data: List<Data> = emptyList()
    private val iconView: IconView = IconView(ContextThemeWrapper(context, R.style.AppTheme))
    private var listener: (RecordViewData.Tracked) -> Unit = {}

    private val singleTapDetector = SingleTapDetector(
        context = context,
        onSingleTap = ::onTouch
    )

    init {
        initArgs(context, attrs, defStyleAttr)
        initPaint()
        initEditMode()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = resolveSize(0, widthMeasureSpec)
        val h = resolveSize(w, heightMeasureSpec)

        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return

        val w = width.toFloat()
        val h = height.toFloat()

        calculateDimensions(w, h)
        drawLegend(canvas, w, h)
        drawData(canvas, w, h)
//        drawIcons(canvas, w, h, r)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> handled = true
        }

        return handled or singleTapDetector.onTouchEvent(event)
    }

    fun setClickListener(listener: (RecordViewData.Tracked) -> Unit) {
        this.listener = listener
    }

    fun setData(data: List<RecordViewData.Tracked>) {
        originalData = data
        val res = mutableListOf<Data>()
        val points: MutableList<Triple<Long, Boolean, Data>> = mutableListOf()

        data.forEach { point ->
            res.add(
                Data(
                    id = point.id,
                    start = point.timeStartedTimestamp,
                    end = point.timeEndedTimestamp,
                    colorInt = point.color,
                    drawable = getIconDrawable(point.iconId),
                )
            )
        }

        res.forEach { item ->
            points.add(Triple(item.start, false, item))
            points.add(Triple(item.end, true, item))
        }
        points.sortWith(compareBy({ it.first }, { it.second }))
        var counter = 0
        var currentColumnCount = 1
        val freeColumns = mutableListOf(1)
        points.map { (time, isEnd, item) ->
            if (!isEnd) {
                counter++
                val columnNumber = freeColumns.minOrNull()!!
                item.columnNumber = columnNumber
                freeColumns.remove(columnNumber)
                if (freeColumns.isEmpty()) freeColumns.add(columnNumber + 1)
            } else {
                counter--
                freeColumns.add(item.columnNumber)
            }

            counter to Triple(time, isEnd, item)
        }.map { (counter, triple) ->
            if (counter == 0) {
                currentColumnCount = 1
            } else if (counter > currentColumnCount) {
                currentColumnCount = counter
            }
            triple.third.columnCount = currentColumnCount

            counter to triple
        }.reversed().map { (counter, triple) ->
            if (counter == 0) {
                currentColumnCount = 1
            } else if (counter > currentColumnCount) {
                currentColumnCount = counter
            }
            triple.third.columnCount = currentColumnCount

            counter to triple
        }

        this.data = res
        invalidate()
    }

    private fun initArgs(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) {
        context
            .obtainStyledAttributes(attrs, R.styleable.RecordsCalendarView, defStyleAttr, 0)
            .run {
                legendTextSize =
                    getDimensionPixelSize(R.styleable.RecordsCalendarView_calendarLegendTextSize, 14).toFloat()
                legendTextColor =
                    getColor(R.styleable.RecordsCalendarView_calendarLegendTextColor, Color.BLACK)
                legendLineColor =
                    getColor(R.styleable.RecordsCalendarView_calendarLegendLineColor, Color.BLACK)
                iconMaxSize =
                    getDimensionPixelSize(R.styleable.RecordsCalendarView_calendarIconMaxSize, 0)
                recordsCount =
                    getInt(R.styleable.RecordsCalendarView_calendarRecordsCount, 0)
                recycle()
            }
    }

    private fun initPaint() {
        recordPaint.apply {
            isAntiAlias = true
        }
        textPaint.apply {
            isAntiAlias = true
            color = legendTextColor
            textSize = legendTextSize
        }
        linePaint.apply {
            isAntiAlias = true
            color = legendLineColor
        }
    }

    private fun calculateDimensions(w: Float, h: Float) {
        val defaultLegendText = "00:00"
        legendTextWidth = textPaint.measureText(defaultLegendText)

        textPaint.getTextBounds(defaultLegendText, 0, defaultLegendText.length, textBounds)
        legendTextHeight = textPaint.fontMetrics.let { it.descent - it.ascent }
        legendTextHeight = textBounds.height().toFloat()

        // Chart dimensions
        pixelLeftBound = legendTextWidth + 2 * legendTextPadding
        pixelRightBound = w - legendTextPadding
        chartWidth = pixelRightBound - pixelLeftBound
    }

    private fun drawData(canvas: Canvas, w: Float, h: Float) {
        canvas.save()

        data.forEach {
            recordPaint.color = it.colorInt

            val boxHeight: Float = h * (it.end - it.start) / dayInMillis
            val boxShift: Float = h * it.start / dayInMillis
            val boxWidth: Float = chartWidth / it.columnCount
            val boxLeft: Float = pixelLeftBound + boxWidth * (it.columnNumber - 1)
            val boxRight: Float = boxLeft + boxWidth
            val boxTop: Float = h - boxShift - boxHeight
            val boxBottom: Float = h - boxShift

            it.boxLeft = boxLeft
            it.boxTop = boxTop
            it.boxRight = boxRight
            it.boxBottom = boxBottom

            bounds.set(
                boxLeft, boxTop,
                boxRight, boxBottom,
            )
            canvas.drawRoundRect(
                bounds,
                recordCornerRadius,
                recordCornerRadius,
                recordPaint
            )
        }

        canvas.restore()
    }

    private fun drawLegend(canvas: Canvas, w: Float, h: Float) {
        val legendTexts = (24 downTo 0)
            .map { it.toString().padStart(2, '0') }
            .map { "$it:00" }

        val lineStep = h / (legendTexts.size - 1)

        canvas.save()

        legendTexts.forEach { text ->
            // Draw line
            canvas.drawLine(
                pixelLeftBound,
                0f,
                w,
                0f,
                linePaint
            )

            // Draw text
            val textCenterY: Float = when (text) {
                legendTexts.first() -> legendTextHeight
                legendTexts.last() -> 0f
                else -> legendTextHeight / 2
            }
            canvas.drawText(
                text,
                legendTextPadding,
                textCenterY,
                textPaint
            )

            canvas.translate(0f, lineStep)
        }

        canvas.restore()
    }

//    private fun drawIcons(canvas: Canvas, w: Float, h: Float, r: Float) {
//        if (data.isEmpty()) return
//
//        val iconSize = calculateIconSize(r)
//        val bounds = Rect(
//            -iconSize / 2, -iconSize / 2,
//            iconSize / 2, iconSize / 2
//        )
//        var rotation: Float
//        val iconPositionFromCenter = r - r * (1 - innerRadiusRatio) / 2
//        var currentSweepAngle = 0f
//        var sweepAngle: Float
//
//        val initial = canvas.save()
//        canvas.translate(w / 2, h / 2)
//        var center = canvas.save()
//
//        data.forEach {
//            // circleCircumference = 2 * Math.PI * r
//            // segmentRatio = it.sweepAngle / 360f
//            // segmentLength = circleCircumference * segmentRatio
//            sweepAngle = it.arcPercent * 360f * segmentAnimationScale
//            val segmentLength = 2 * Math.PI * iconPositionFromCenter * sweepAngle / 360f
//            if (segmentLength < iconSize + 2 * iconPadding) return@forEach
//
//            rotation = currentSweepAngle + sweepAngle / 2f
//            canvas.rotate(rotation)
//            canvas.translate(0f, -iconPositionFromCenter)
//            canvas.rotate(-rotation)
//            it.drawable?.bounds = bounds
//            it.drawable?.draw(canvas)
//
//            currentSweepAngle += sweepAngle
//            canvas.restoreToCount(center)
//            center = canvas.save()
//        }
//
//        canvas.restoreToCount(initial)
//    }

//    private fun calculateIconSize(r: Float): Int {
//        val availableIconSize = max(0, (r - r * innerRadiusRatio - 2 * iconPadding).toInt())
//
//        return if (iconMaxSize == 0) {
//            availableIconSize
//        } else {
//            min(iconMaxSize, availableIconSize)
//        }
//    }

    private fun initEditMode() {
        val records = recordsCount.takeIf { it != 0 } ?: 5
        if (isInEditMode) {
            var currentStart = 0L
            (0 until records)
                .map {
                    currentStart += hourInMillis * it
                    RecordViewData.Tracked(
                        id = 1,
                        timeStartedTimestamp = currentStart,
                        timeEndedTimestamp = hourInMillis * (it + 1),
                        name = "",
                        tagName = "",
                        timeStarted = "",
                        timeFinished = "",
                        duration = "",
                        iconId = RecordTypeIcon.Image(R.drawable.unknown),
                        color = Color.BLACK,
                        comment = ""
                    )
                }.let(::setData)
        }
    }

    private fun getIconDrawable(iconId: RecordTypeIcon): Drawable {
        return iconView
            .apply {
                itemIcon = iconId
                measureExactly(iconMaxSize)
            }
            .getBitmapFromView()
            .let { BitmapDrawable(resources, it) }
    }

    private fun onTouch(event: MotionEvent) {
        val x = event.x
        val y = event.y

        data
            .firstOrNull {
                it.boxLeft < x && it.boxTop < y && it.boxRight > x && it.boxBottom > y
            }
            ?.id
            ?.let { clickedId ->
                originalData.firstOrNull { it.id == clickedId }
            }
            ?.let(listener)
    }

    private inner class Data(
        val id: Long,
        val start: Long,
        val end: Long,
        @ColorInt val colorInt: Int,
        val drawable: Drawable? = null,
        var columnCount: Int = 1,
        var columnNumber: Int = 1,
        var boxLeft: Float = 0f,
        var boxTop: Float = 0f,
        var boxRight: Float = 0f,
        var boxBottom: Float = 0f,
    )
}