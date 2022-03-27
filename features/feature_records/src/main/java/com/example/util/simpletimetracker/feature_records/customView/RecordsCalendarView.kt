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
import android.os.Parcelable
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.utils.ScaleDetector
import com.example.util.simpletimetracker.core.utils.SingleTapDetector
import com.example.util.simpletimetracker.core.utils.SwipeDetector
import com.example.util.simpletimetracker.core.utils.isHorizontal
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_views.IconView
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import kotlinx.parcelize.Parcelize
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

    private var scaleFactor: Float = 1f
    private var panFactor: Float = 0f
    private var lastPanFactor: Float = 0f
    private var legendTextWidth: Float = 0f
    private var legendTextHeight: Float = 0f
    private var pixelLeftBound: Float = 0f
    private var pixelRightBound: Float = 0f
    private var canvasHeight: Float = 0f
    private var chartWidth: Float = 0f
    private val legendTextPadding: Float = 2.dpToPx().toFloat()
    private val recordCornerRadius: Float = 8.dpToPx().toFloat()
    private val recordVerticalPadding: Float = 2.dpToPx().toFloat()
    private val iconStartPadding: Float = 4.dpToPx().toFloat()
    private val dayInMillis = TimeUnit.DAYS.toMillis(1)
    private val hourInMillis = TimeUnit.HOURS.toMillis(1)

    private val recordPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private val linePaint: Paint = Paint()

    private val bounds: Rect = Rect(0, 0, 0, 0)
    private val boundsF: RectF = RectF(0f, 0f, 0f, 0f)
    private var originalData: List<RecordViewData.Tracked> = emptyList()
    private var data: List<Data> = emptyList()
    private val iconView: IconView = IconView(ContextThemeWrapper(context, R.style.AppTheme))
    private var listener: (RecordViewData.Tracked) -> Unit = {}

    private val singleTapDetector = SingleTapDetector(
        context = context,
        onSingleTap = ::onTouch
    )
    private val scaleDetector = ScaleDetector(
        context = context,
        onScaleChanged = ::onScaleChanged,
        onScaleStop = ::onScaleStop
    )
    private val swipeDetector = SwipeDetector(
        context = context,
        onSlide = ::onSwipe,
        onSlideStop = ::onSwipeStop
    )

    init {
        initArgs(context, attrs, defStyleAttr)
        initPaint()
        initEditMode()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(
            superSavedState = superState,
            scaleFactor = scaleFactor,
            panFactor = panFactor,
            lastPanFactor = lastPanFactor,
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? SavedState
        super.onRestoreInstanceState(savedState?.superSavedState ?: state)
        scaleFactor = savedState?.scaleFactor ?: 1f
        panFactor = savedState?.panFactor.orZero()
        lastPanFactor = savedState?.lastPanFactor.orZero()
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
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> handled = true
        }

        return handled or
            singleTapDetector.onTouchEvent(event) or
            swipeDetector.onTouchEvent(event) or
            scaleDetector.onTouchEvent(event)
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
            points.add(Triple(item.start, true, item))
            points.add(Triple(item.end, false, item))
        }
        points.sortWith(compareBy({ it.first }, { it.second }))
        var counter = 0
        var currentColumnCount = 1
        val freeColumns = mutableListOf(1)
        points.map { (time, isStart, item) ->
            if (isStart) {
                counter++
                val columnNumber = freeColumns.minOrNull()!!
                item.columnNumber = columnNumber
                freeColumns.remove(columnNumber)
                if (freeColumns.isEmpty()) freeColumns.add(columnNumber + 1)
            } else {
                counter--
                freeColumns.add(item.columnNumber)
            }

            counter to Triple(time, isStart, item)
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
        canvasHeight = h

        val defaultLegendText = "00:00"
        legendTextWidth = textPaint.measureText(defaultLegendText)

        textPaint.getTextBounds(defaultLegendText, 0, defaultLegendText.length, bounds)
        legendTextHeight = textPaint.fontMetrics.let { it.descent - it.ascent }
        legendTextHeight = bounds.height().toFloat()

        // Chart dimensions
        pixelLeftBound = legendTextWidth + 2 * legendTextPadding
        pixelRightBound = w - legendTextPadding
        chartWidth = pixelRightBound - pixelLeftBound
    }

    private fun drawData(canvas: Canvas, w: Float, h: Float) {
        canvas.save()

        data.forEach { item ->
            recordPaint.color = item.colorInt

            val boxHeight: Float = h * (item.end - item.start) / dayInMillis
            val boxShift: Float = h * item.start / dayInMillis
            val boxWidth: Float = chartWidth / item.columnCount

            val boxLeft: Float = pixelLeftBound + boxWidth * (item.columnNumber - 1)
            val boxRight: Float = boxLeft + boxWidth
            val boxTop: Float = (h - boxShift - boxHeight) * scaleFactor + panFactor
            val boxBottom: Float = (h - boxShift) * scaleFactor + panFactor

            item.boxLeft = boxLeft
            item.boxTop = boxTop
            item.boxRight = boxRight
            item.boxBottom = boxBottom

            // Draw box
            boundsF.set(
                boxLeft, boxTop,
                boxRight, boxBottom,
            )
            canvas.drawRoundRect(
                boundsF,
                recordCornerRadius,
                recordCornerRadius,
                recordPaint
            )

            val iconSize: Int? = iconMaxSize.takeIf { it < boundsF.height() - 2 * recordVerticalPadding }
            if (iconSize != null) {

                val iconLeft: Int = (boundsF.left + iconStartPadding).toInt()
                val iconRight: Int = iconLeft + iconSize
                val iconTop: Int = (boundsF.top + boundsF.height() / 2 - iconSize / 2).toInt()
                val iconBottom: Int = iconTop + iconSize

                // Draw icon
                bounds.set(
                    iconLeft, iconTop,
                    iconRight, iconBottom
                )
                item.drawable?.bounds = bounds
                item.drawable?.draw(canvas)
            }
        }

        canvas.restore()
    }

    private fun drawLegend(canvas: Canvas, w: Float, h: Float) {
        val legendTexts = (24 downTo 0)
            .map { it.toString().padStart(2, '0') }
            .map { "$it:00" }
        val lineStep = h / (legendTexts.size - 1)

        canvas.save()
        canvas.translate(0f, panFactor)

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

            canvas.translate(0f, lineStep * scaleFactor)
        }

        canvas.restore()
    }

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
                        timeEndedTimestamp = currentStart + hourInMillis * (it + 1),
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

    private fun onScaleChanged(newScale: Float) {
        parent.requestDisallowInterceptTouchEvent(true)
        scaleFactor *= newScale
        scaleFactor = scaleFactor.coerceAtLeast(1f)
        panFactor = lastPanFactor - (canvasHeight * scaleFactor - canvasHeight) / 2
        invalidate()
    }

    private fun onScaleStop() {
        parent.requestDisallowInterceptTouchEvent(false)
        lastPanFactor = panFactor
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onSwipe(offset: Float, direction: SwipeDetector.Direction, event: MotionEvent) {
        if (!direction.isHorizontal()) {
            parent.requestDisallowInterceptTouchEvent(true)
            panFactor = lastPanFactor + offset
            invalidate()
        }
    }

    private fun onSwipeStop() {
        parent.requestDisallowInterceptTouchEvent(false)
        lastPanFactor = panFactor
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

    @Parcelize
    private class SavedState(
        val superSavedState: Parcelable?,
        val scaleFactor: Float,
        val panFactor: Float,
        val lastPanFactor: Float,
    ) : View.BaseSavedState(superSavedState)
}