package com.example.util.simpletimetracker.feature_records.customView

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.example.util.simpletimetracker.core.utils.ScaleDetector
import com.example.util.simpletimetracker.core.utils.SingleTapDetector
import com.example.util.simpletimetracker.core.utils.SwipeDetector
import com.example.util.simpletimetracker.core.utils.isHorizontal
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_views.ColorUtils
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
    defStyleAttr,
) {

    // Attrs
    private var nameTextSize: Float = 0f
    private var nameTextColor: Int = 0
    private var itemTagColor: Int = 0
    private var legendTextSize: Float = 0f
    private var legendTextColor: Int = 0
    private var legendLineColor: Int = 0
    private var legendLineSecondaryColor: Int = 0
    private var currentTimeLegendColor: Int = 0
    private var currentTimeLegendWidth: Float = 0f
    private var iconMaxSize: Int = 0
    private var reverseOrder: Boolean = false
    private var recordsCount: Int = 0
    private var columnsCount: Int = 0
    // End of attrs

    private var isScaling: Boolean = false
    private var scaleFactor: Float = 1f
    private var lastScaleFactor: Float = 1f
    private var panFactor: Float = 0f
    private var lastPanFactor: Float = 0f
    private var legendTextWidth: Float = 0f
    private var legendTextHeight: Float = 0f
    private var legendTopTextHeight: Float = 0f
    private var legendMinutesTextHeight: Float = 0f
    private var chartLeftBound: Float = 0f
    private var chartRightBound: Float = 0f
    private var chartTopBound: Float = 0f
    private var chartBottomBound: Float = 0f
    private var chartHeight: Float = 0f
    private var columnWidth: Float = 0f
    private val legendTextPadding: Float = 2.dpToPx().toFloat()
    private val legendTopTextPadding: Float = 4.dpToPx().toFloat()
    private val legendMinutesTextPadding: Float = 4.dpToPx().toFloat()
    private val recordCornerRadius: Float = 8.dpToPx().toFloat()
    private val recordVerticalPadding: Float = 2.dpToPx().toFloat()
    private val recordHorizontalPadding: Float = 4.dpToPx().toFloat()
    private val paddingBetweenDays: Float = 1.dpToPx().toFloat()
    private val dayInMillis = TimeUnit.DAYS.toMillis(1)
    private val hourInMillis = TimeUnit.HOURS.toMillis(1)
    private var selectedRecord: RecordsCalendarViewData.Point.Data? = null
    private var selectedRecordColor: Int = 0

    private val recordPaint: Paint = Paint()
    private val legendTextPaint: Paint = Paint()
    private val legendTopTextPaint: Paint = Paint()
    private val legendMinutesTextPaint: Paint = Paint()
    private val linePaint: Paint = Paint()
    private val lineSecondaryPaint: Paint = Paint()
    private val currentTimelinePaint: Paint = Paint()

    private val bounds: Rect = Rect(0, 0, 0, 0)
    private val textBounds: Rect = Rect(0, 0, 0, 0)
    private val recordBounds: RectF = RectF(0f, 0f, 0f, 0f)
    private var data: List<Column> = emptyList()
    private val dataSize: Int get() = data.size.takeUnless { it == 0 } ?: 1
    private var shouldDrawTopLegends: Boolean = false
    private var currentTime: Long? = null
    private var startOfDayShift: Long = 0
    private val iconView: IconView = IconView(ContextThemeWrapper(context, R.style.AppTheme))
    private var listener: (ViewHolderType) -> Unit = {}

    private val nameTextView: AppCompatTextView by lazy {
        getTextView(
            textColor = nameTextColor,
            typeface = Typeface.DEFAULT_BOLD,
            widthLayoutParams = ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }
    private val durationTextView: AppCompatTextView by lazy {
        getTextView(
            textColor = itemTagColor,
            typeface = Typeface.DEFAULT_BOLD,
            widthLayoutParams = ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }
    private val timeTextView: AppCompatTextView by lazy {
        getTextView(
            textColor = itemTagColor,
            typeface = Typeface.DEFAULT,
            widthLayoutParams = ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }
    private val commentTextView: AppCompatTextView by lazy {
        getTextView(
            textColor = itemTagColor,
            typeface = Typeface.DEFAULT,
            widthLayoutParams = ViewGroup.LayoutParams.MATCH_PARENT,
        )
    }

    private val availableMinutesRanges: List<List<Int>> = listOf(
        (0..60).step(1),
        (0..60).step(5),
        (0..60).step(10),
        (0..60).step(15),
        (0..60).step(20),
        (0..60).step(30),
    ).map {
        it.toList().drop(1).dropLast(1)
    }

    private val singleTapDetector = SingleTapDetector(
        context = context,
        onSingleTap = ::onEventTouch,
    )
    private val scaleDetector = ScaleDetector(
        context = context,
        onScaleStart = ::onEventScaleStart,
        onScaleChanged = ::onEventScaleChanged,
        onScaleStop = ::onEventScaleStop,
    )
    private val swipeDetector = SwipeDetector(
        context = context,
        onSlide = ::onEventSwipe,
        onSlideStop = ::onEventSwipeStop,
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
            lastScaleFactor = lastScaleFactor,
            panFactor = panFactor,
            lastPanFactor = lastPanFactor,
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? SavedState
        super.onRestoreInstanceState(savedState?.superSavedState ?: state)
        scaleFactor = savedState?.scaleFactor ?: 1f
        lastScaleFactor = savedState?.lastScaleFactor ?: 1f
        panFactor = savedState?.panFactor.orZero()
        lastPanFactor = savedState?.lastPanFactor.orZero()
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
        drawTopLegend(canvas)
        drawSideLegend(canvas, w)
        data.forEachIndexed { index, column ->
            drawData(
                canvas = canvas,
                data = column.data,
                index = index,
            )
        }
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

    fun setClickListener(listener: (ViewHolderType) -> Unit) {
        this.listener = listener
    }

    fun setData(viewData: RecordsCalendarViewData) {
        currentTime = viewData.currentTime
        startOfDayShift = viewData.startOfDayShift
        reverseOrder = viewData.reverseOrder
        shouldDrawTopLegends = viewData.shouldDrawTopLegends
        data = viewData.points.map(::processData)
        invalidate()
    }

    fun reset() {
        scaleFactor = 1f
        lastScaleFactor = 1f
        panFactor = 0f
        lastPanFactor = 0f
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
                nameTextSize =
                    getDimensionPixelSize(R.styleable.RecordsCalendarView_calendarTextSize, 14).toFloat()
                nameTextColor =
                    getColor(R.styleable.RecordsCalendarView_calendarTextColor, Color.WHITE)
                itemTagColor =
                    getColor(R.styleable.RecordsCalendarView_calendarTagColor, Color.WHITE)
                legendTextSize =
                    getDimensionPixelSize(R.styleable.RecordsCalendarView_calendarLegendTextSize, 14).toFloat()
                legendTextColor =
                    getColor(R.styleable.RecordsCalendarView_calendarLegendTextColor, Color.BLACK)
                legendLineColor =
                    getColor(R.styleable.RecordsCalendarView_calendarLegendLineColor, Color.BLACK)
                legendLineSecondaryColor =
                    getColor(R.styleable.RecordsCalendarView_calendarLegendLineSecondaryColor, Color.BLACK)
                currentTimeLegendColor =
                    getColor(R.styleable.RecordsCalendarView_calendarCurrentTimeLegendColor, Color.RED)
                currentTimeLegendWidth =
                    getDimensionPixelSize(R.styleable.RecordsCalendarView_calendarCurrentTimeLegendWidth, 0).toFloat()
                iconMaxSize =
                    getDimensionPixelSize(R.styleable.RecordsCalendarView_calendarIconMaxSize, 0)

                if (hasValue(R.styleable.RecordsCalendarView_calendarReverseOrder)) {
                    reverseOrder = getBoolean(R.styleable.RecordsCalendarView_calendarReverseOrder, false)
                }

                if (hasValue(R.styleable.RecordsCalendarView_calendarRecordsCount)) {
                    recordsCount = getInt(R.styleable.RecordsCalendarView_calendarRecordsCount, 0)
                }

                if (hasValue(R.styleable.RecordsCalendarView_calendarColumnsCount)) {
                    columnsCount = getInt(R.styleable.RecordsCalendarView_calendarColumnsCount, 0)
                }

                recycle()
            }
    }

    private fun initPaint() {
        recordPaint.apply {
            isAntiAlias = true
        }
        legendTextPaint.apply {
            isAntiAlias = true
            color = legendTextColor
            textSize = legendTextSize
        }
        legendTopTextPaint.apply {
            isAntiAlias = true
            color = legendTextColor
            textSize = legendTextSize
            typeface = Typeface.DEFAULT_BOLD
        }
        legendMinutesTextPaint.apply {
            isAntiAlias = true
            color = legendTextColor
            textSize = legendTextSize * 0.8f
            textAlign = Paint.Align.RIGHT
        }
        linePaint.apply {
            isAntiAlias = true
            color = legendLineColor
        }
        lineSecondaryPaint.apply {
            isAntiAlias = true
            color = legendLineSecondaryColor
        }
        currentTimelinePaint.apply {
            isAntiAlias = true
            color = currentTimeLegendColor
            strokeWidth = currentTimeLegendWidth
        }
    }

    private fun calculateDimensions(w: Float, h: Float) {
        val defaultLegendText = "00:00"
        legendTextWidth = legendTextPaint.measureText(defaultLegendText)

        legendTextPaint.getTextBounds(defaultLegendText, 0, defaultLegendText.length, textBounds)
        legendTextHeight = textBounds.height().toFloat()

        legendTopTextPaint.getTextBounds(defaultLegendText, 0, defaultLegendText.length, textBounds)
        legendTopTextHeight = textBounds.height().toFloat()

        legendMinutesTextPaint.getTextBounds(defaultLegendText, 0, defaultLegendText.length, textBounds)
        legendMinutesTextHeight = textBounds.height().toFloat()

        // Chart dimensions
        chartLeftBound = legendTextWidth + 2 * legendTextPadding
        chartRightBound = w - legendTextPadding
        columnWidth = (chartRightBound - chartLeftBound) / dataSize

        chartTopBound = if (shouldDrawTopLegends) {
            legendTopTextHeight + 2 * legendTopTextPadding
        } else {
            0f
        }
        chartBottomBound = h
        chartHeight = chartBottomBound - chartTopBound
    }

    private fun drawData(
        canvas: Canvas,
        data: List<Data>,
        index: Int,
    ) {
        val ellipsizedNameCutoff = iconMaxSize * 2

        var boxHeight: Float
        var boxShift: Float
        var boxWidth: Float
        var boxLeft: Float
        var boxRight: Float
        var boxTop: Float
        var boxBottom: Float

        var iconLeft: Int
        var iconRight: Int
        var iconTop: Int
        var iconBottom: Int

        var textWidth: Float
        var textHeight: Int
        var timesTextHeight: Int
        var textLeft: Float
        var textTop: Float
        var availableHeight: Float
        var availableWidth: Float
        var newMaxLines: Int

        data.forEach { item ->
            var iconDrawn = false
            var timesDrawn = false
            var durationInSeparateLine = false
            var nameIsEllipsized = false

            /************
             * Draw box *
             ************/
            recordPaint.color = item.point.data.color.let {
                if (selectedRecord == item.point.data) {
                    selectedRecordColor
                } else {
                    it
                }
            }
            boxHeight = chartHeight * (item.point.end - item.point.start) / dayInMillis
            boxShift = chartHeight * item.point.start / dayInMillis
            boxWidth = columnWidth / item.columnCount
            boxLeft = chartLeftBound +
                columnWidth * index +
                boxWidth * (item.columnNumber - 1)
            boxRight = boxLeft + boxWidth
            boxBottom = if (reverseOrder) {
                chartTopBound + (boxShift + boxHeight) * scaleFactor
            } else {
                chartTopBound + (chartHeight - boxShift) * scaleFactor
            }.let { it + panFactor }
            boxTop = boxBottom - boxHeight * scaleFactor

            // Save coordinates for click event.
            item.boxLeft = boxLeft
            item.boxTop = boxTop
            item.boxRight = boxRight
            item.boxBottom = boxBottom

            recordBounds.set(
                boxLeft + (paddingBetweenDays / 2),
                boxTop,
                boxRight - (paddingBetweenDays / 2),
                boxBottom,
            )
            canvas.drawRoundRect(
                recordBounds,
                recordCornerRadius,
                recordCornerRadius,
                recordPaint,
            )

            availableHeight = recordBounds.height() - 2 * recordVerticalPadding
            availableWidth = recordBounds.width() - 2 * recordHorizontalPadding

            /*************
             * Draw icon *
             *************/
            // If can fit into box.
            if (iconMaxSize < availableHeight && iconMaxSize < availableWidth) {
                iconLeft = (recordBounds.left + recordHorizontalPadding).toInt()
                iconRight = iconLeft + iconMaxSize
                iconTop = (recordBounds.top + recordVerticalPadding).toInt()
                iconBottom = iconTop + iconMaxSize

                bounds.set(
                    iconLeft, iconTop,
                    iconRight, iconBottom,
                )
                item.drawable?.bounds = bounds
                item.drawable?.draw(canvas)

                iconDrawn = true
                availableWidth = (availableWidth - iconMaxSize - recordHorizontalPadding)
                    .coerceAtLeast(0f)
                availableHeight = (availableHeight - iconMaxSize - recordVerticalPadding)
                    .coerceAtLeast(0f)
            }

            /*************
             * Draw name *
             *************/
            nameTextView.text = getItemName(item.point.data)
            nameTextView.measureText(
                width = 0,
                widthSpec = MeasureSpec.UNSPECIFIED,
            )
            if (nameTextView.measuredWidth > availableWidth) {
                nameIsEllipsized = true
                nameTextView.measureText(
                    width = availableWidth.toInt(),
                    widthSpec = MeasureSpec.EXACTLY,
                )
            }
            // If can fit into box.
            if (
                iconDrawn &&
                (nameTextView.measuredWidth > ellipsizedNameCutoff || !nameIsEllipsized) &&
                (nameTextView.measuredWidth < availableWidth || nameIsEllipsized)
            ) {
                textLeft = recordBounds.left + iconMaxSize + 2 * recordHorizontalPadding
                textTop = recordBounds.top + recordVerticalPadding

                canvas.save()
                canvas.translate(textLeft, textTop)
                nameTextView.draw(canvas)
                canvas.restore()

                availableWidth = (availableWidth - nameTextView.measuredWidth - recordHorizontalPadding)
                    .coerceAtLeast(0f)
            }

            /*****************
             * Draw duration *
             *****************/
            durationTextView.text = item.point.data.duration
            durationTextView.measureText(
                width = 0,
                widthSpec = MeasureSpec.UNSPECIFIED,
            )
            textHeight = durationTextView.measuredHeight
            // If can fit into box.
            if (
                iconDrawn &&
                durationTextView.measuredWidth < availableWidth
            ) {
                textLeft = recordBounds.right - recordHorizontalPadding - durationTextView.measuredWidth
                textTop = recordBounds.top + recordVerticalPadding

                canvas.save()
                canvas.translate(textLeft, textTop)
                durationTextView.draw(canvas)
                canvas.restore()
            } else if (
                iconDrawn &&
                textHeight < availableHeight
            ) {
                // Try to draw on separate line.
                val newAvailableWidth = recordBounds.width() - 2 * recordHorizontalPadding
                durationTextView.measureText(
                    width = 0,
                    widthSpec = MeasureSpec.UNSPECIFIED,
                )
                textHeight = durationTextView.measuredHeight
                if (durationTextView.measuredWidth < newAvailableWidth) {
                    durationInSeparateLine = true
                    textLeft = recordBounds.left + recordHorizontalPadding
                    textTop = recordBounds.top + recordVerticalPadding + iconMaxSize

                    canvas.save()
                    canvas.translate(textLeft, textTop)
                    durationTextView.draw(canvas)
                    canvas.restore()

                    availableHeight = (availableHeight - textHeight - recordVerticalPadding)
                        .coerceAtLeast(0f)
                }
            }

            /**************
             * Draw times *
             **************/
            availableWidth = recordBounds.width() - 2 * recordHorizontalPadding
            timeTextView.text = getItemTimes(item.point.data)
            timeTextView.measureText(
                width = 0,
                widthSpec = MeasureSpec.UNSPECIFIED,
            )
            timesTextHeight = timeTextView.measuredHeight
            // If can fit into box.
            if (
                iconDrawn &&
                timeTextView.measuredWidth < availableWidth &&
                timesTextHeight < availableHeight
            ) {
                textLeft = recordBounds.left + recordHorizontalPadding
                textTop = recordBounds.top + recordVerticalPadding + iconMaxSize +
                    textHeight.takeIf { durationInSeparateLine }.orZero()

                canvas.save()
                canvas.translate(textLeft, textTop)
                timeTextView.draw(canvas)
                canvas.restore()

                timesDrawn = true
                availableHeight = (availableHeight - timesTextHeight - recordVerticalPadding)
                    .coerceAtLeast(0f)
            }

            /****************
             * Draw comment *
             ****************/
            if (item.point.data.comment.isNotEmpty()) {
                textWidth = recordBounds.width() - 2 * recordHorizontalPadding
                commentTextView.text = item.point.data.comment
                commentTextView.apply { maxLines = 1 }.measureText(
                    width = textWidth.toInt(),
                    widthSpec = MeasureSpec.EXACTLY,
                    height = 0,
                    heightSpec = MeasureSpec.UNSPECIFIED,
                )
                newMaxLines = (availableHeight / commentTextView.measuredHeight).toInt()
                commentTextView.apply { maxLines = newMaxLines }.measureText(
                    width = textWidth.toInt(),
                    widthSpec = MeasureSpec.EXACTLY,
                    height = availableHeight.toInt(),
                    heightSpec = MeasureSpec.AT_MOST,
                )
                // If can fit into box.
                if (
                    iconDrawn &&
                    commentTextView.measuredHeight < availableHeight
                ) {
                    textLeft = recordBounds.left + recordHorizontalPadding
                    textTop = recordBounds.top + recordVerticalPadding + iconMaxSize +
                        textHeight.takeIf { durationInSeparateLine }.orZero() +
                        timesTextHeight.takeIf { timesDrawn }.orZero()

                    canvas.save()
                    canvas.translate(textLeft, textTop)
                    commentTextView.draw(canvas)
                    canvas.restore()
                }
            }
        }
    }

    private fun drawTopLegend(canvas: Canvas) {
        if (!shouldDrawTopLegends) return

        var currentText: String
        var textWidth: Float
        var textLeft: Float

        canvas.save()
        canvas.translate(0f, panFactor)

        data.forEachIndexed { index, column ->
            currentText = column.legend
            textWidth = legendTopTextPaint.measureText(currentText)
            textLeft = chartLeftBound +
                columnWidth * index +
                columnWidth / 2 -
                textWidth / 2

            canvas.drawText(
                currentText,
                textLeft,
                chartTopBound - legendTopTextPadding,
                legendTopTextPaint,
            )
        }

        canvas.restore()
    }

    private fun drawSideLegend(canvas: Canvas, w: Float) {
        fun Float.checkReverse(): Float {
            return if (reverseOrder) {
                chartTopBound + chartHeight * scaleFactor - (this - chartTopBound)
            } else {
                this
            }
        }

        fun Float.checkOverdraw(): Float {
            // If goes over the end - draw on top, and otherwise.
            return when {
                this > chartTopBound + chartHeight * scaleFactor -> this - chartHeight * scaleFactor
                this < chartTopBound -> this + chartHeight * scaleFactor
                else -> this
            }
        }

        val hours = (24 downTo 0)
            .map { if (it == 24 && startOfDayShift != 0L) 0 else it }
        val lineStep = chartHeight / (hours.size - 1)

        val selectedMinutesRange = availableMinutesRanges.firstOrNull {
            (lineStep * scaleFactor / (it.size + 1)) > (legendMinutesTextHeight + 2 * legendMinutesTextPadding)
        }.orEmpty()
        val minuteLineStep = lineStep / (selectedMinutesRange.size + 1)

        val shift: Float = chartHeight * startOfDayShift / dayInMillis

        canvas.save()
        canvas.translate(0f, panFactor)

        // Draw current time
        currentTime?.let { currentTime ->
            val currentTimeY = (
                chartTopBound +
                    chartHeight * scaleFactor * (dayInMillis - currentTime) / dayInMillis
                ).checkOverdraw()

            canvas.drawLine(
                chartLeftBound - legendTextPadding,
                currentTimeY.checkReverse(),
                w,
                currentTimeY.checkReverse(),
                currentTimelinePaint,
            )
        }

        hours.forEachIndexed { index, hour ->
            val currentY = (
                chartTopBound +
                    index * lineStep * scaleFactor +
                    shift * scaleFactor
                ).checkOverdraw()

            // Draw hour line
            canvas.drawLine(
                chartLeftBound,
                currentY.checkReverse(),
                w,
                currentY.checkReverse(),
                linePaint,
            )

            // Draw hour text
            val textCenterY: Float = (currentY.checkReverse() + legendTextHeight / 2)
                .coerceIn(
                    chartTopBound + legendTextHeight,
                    chartTopBound + chartHeight * scaleFactor,
                )
            val hourText = hour.toString()
                .padStart(2, '0')
            canvas.drawText(
                hourText.let { "$it:00" },
                legendTextPadding,
                textCenterY,
                legendTextPaint,
            )

            if (index == 0) return@forEachIndexed
            // Draw minutes
            selectedMinutesRange.forEachIndexed { minuteIndex, minute ->
                val minuteCurrentY = (currentY - (minuteIndex + 1) * minuteLineStep * scaleFactor).checkOverdraw()

                // Draw minute line
                canvas.drawLine(
                    chartLeftBound,
                    minuteCurrentY.checkReverse(),
                    w,
                    minuteCurrentY.checkReverse(),
                    lineSecondaryPaint,
                )

                // Draw minute text
                val minuteTextCenterY: Float = (minuteCurrentY.checkReverse() + legendMinutesTextHeight / 2)
                    .coerceIn(
                        chartTopBound + legendMinutesTextHeight,
                        chartTopBound + chartHeight * scaleFactor,
                    )
                val minuteText = minute.toString()
                    .padStart(2, '0')
                val fullText = "$hourText:$minuteText"
                canvas.drawText(
                    fullText,
                    chartLeftBound - legendTextPadding,
                    minuteTextCenterY,
                    legendMinutesTextPaint,
                )
            }
        }

        canvas.restore()
    }

    private fun initEditMode() {
        val records = recordsCount.takeIf { it != 0 } ?: 5
        val columns = columnsCount.takeIf { it != 0 } ?: 1
        if (isInEditMode) {
            var currentStart = 0L
            (0 until records)
                .map {
                    currentStart += hourInMillis * it
                    val start = currentStart
                    val end = currentStart + hourInMillis * (it + 1)
                    val record = RecordViewData.Tracked(
                        id = 1,
                        timeStartedTimestamp = start,
                        timeEndedTimestamp = end,
                        name = "Record $it",
                        tagName = "Tag $it",
                        timeStarted = "07:35",
                        timeFinished = "11:58",
                        duration = "5h 23m 3s",
                        iconId = RecordTypeIcon.Image(R.drawable.unknown),
                        color = Color.RED,
                        comment = "Comment $it",
                    )
                    RecordsCalendarViewData.Point(
                        start = start,
                        end = end,
                        data = RecordsCalendarViewData.Point.Data.RecordData(record),
                    )
                }.let {
                    val points = RecordsCalendarViewData.Points("Sun", it)
                    RecordsCalendarViewData(
                        currentTime = 18 * hourInMillis,
                        startOfDayShift = 0,
                        points = List(columns) { points },
                        reverseOrder = reverseOrder,
                        shouldDrawTopLegends = true,
                    )
                }.let(::setData)
        }
    }

    private fun processData(data: RecordsCalendarViewData.Points): Column {
        val res = mutableListOf<Data>()

        // Raw data.
        data.data.forEach { point ->
            res += Data(
                point = point,
                drawable = getIconDrawable(point.data.iconId),
            )
        }

        // Calculate intersections.
        val points: MutableList<Triple<Long, Boolean, Data>> = mutableListOf()
        res.forEach { item ->
            // Start of range marked with true.
            points.add(Triple(item.point.start, true, item))
            points.add(Triple(item.point.end, false, item))
        }

        // Sort by range edge (start or end) when put starts first.
        points.sortWith(compareBy({ it.first }, { it.second }))
        var currentCounter = 0
        var currentColumnCount = 1
        val freeColumns = mutableListOf(1)

        fun calculateColumns(
            point: Pair<Int, Triple<Long, Boolean, Data>>,
        ): Pair<Int, Triple<Long, Boolean, Data>> {
            val (counter, triple) = point

            // New separate column.
            if (counter == 0) {
                currentColumnCount = 1
            } else if (counter > currentColumnCount) {
                currentColumnCount = counter
            }
            if (currentColumnCount > triple.third.columnCount) {
                triple.third.columnCount = currentColumnCount
            }

            return counter to triple
        }

        points.map { (time, isStart, item) ->
            if (isStart) {
                currentCounter++
                val columnNumber = freeColumns.minOrNull()!!
                item.columnNumber = columnNumber
                freeColumns.remove(columnNumber)
                if (freeColumns.isEmpty()) freeColumns.add(columnNumber + 1)
            } else {
                currentCounter--
                freeColumns.add(item.columnNumber)
            }
            currentCounter to Triple(time, isStart, item)
        }
            // Find max column count and pass it further and back down the list.
            .map(::calculateColumns)
            .reversed()
            .map(::calculateColumns)

        return Column(
            legend = data.legend,
            data = res,
        )
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

    private fun getItemName(item: RecordsCalendarViewData.Point.Data): CharSequence {
        return if (item.tagName.isEmpty()) {
            item.name
        } else {
            val name = "${item.name} - ${item.tagName}"
            val spannable = SpannableString(name)
            spannable.setSpan(
                ForegroundColorSpan(itemTagColor),
                item.name.length, name.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            spannable
        }
    }

    private fun getItemTimes(item: RecordsCalendarViewData.Point.Data): String {
        return when (val value = item.value) {
            is RecordViewData -> "${value.timeStarted} - ${value.timeFinished}"
            is RunningRecordViewData -> value.timeStarted
            else -> ""
        }
    }

    private fun onEventTouch(event: MotionEvent) {
        val selected = findDataPoint(x = event.x, y = event.y)?.point?.data
        selectedRecord = selected
        selected?.let(::animateSelectedRecord)
        selected?.value?.let(listener)
    }

    private fun onEventScaleStart() {
        isScaling = true
    }

    private fun onEventScaleChanged(newScale: Float) {
        parent.requestDisallowInterceptTouchEvent(true)
        scaleFactor *= newScale
        scaleFactor = scaleFactor.coerceAtLeast(1f)
        val currentScale = scaleFactor / lastScaleFactor
        panFactor = lastPanFactor * currentScale - (chartHeight * currentScale - chartHeight) / 2
        coercePan()
        invalidate()
    }

    private fun onEventScaleStop() {
        parent.requestDisallowInterceptTouchEvent(false)
        lastScaleFactor = scaleFactor
        lastPanFactor = panFactor
        isScaling = false
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onEventSwipe(
        offset: Float,
        direction: SwipeDetector.Direction,
        event: MotionEvent,
    ) {
        if (!direction.isHorizontal() && !isScaling) {
            parent.requestDisallowInterceptTouchEvent(true)
            panFactor = lastPanFactor + offset
            coercePan()
            invalidate()
        }
    }

    private fun onEventSwipeStop() {
        if (isScaling) return
        parent.requestDisallowInterceptTouchEvent(false)
        lastPanFactor = panFactor
    }

    private fun coercePan() {
        val maxPanAvailable = chartHeight * scaleFactor - chartHeight
        panFactor = panFactor.coerceIn(-maxPanAvailable, 0f)
    }

    private fun findDataPoint(
        x: Float,
        y: Float,
    ): Data? {
        return data.map(Column::data).flatten().firstOrNull {
            it.boxLeft < x && it.boxTop < y && it.boxRight > x && it.boxBottom > y
        }
    }

    private fun getTextView(
        textColor: Int,
        typeface: Typeface,
        widthLayoutParams: Int,
    ): AppCompatTextView {
        return AppCompatTextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, nameTextSize)
            setTextColor(textColor)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            this.typeface = typeface
            layoutParams = ViewGroup.LayoutParams(
                widthLayoutParams, ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    private fun animateSelectedRecord(
        selectedRecord: RecordsCalendarViewData.Point.Data,
    ) {
        val from = selectedRecord.color
        val to = ColorUtils.normalizeLightness(selectedRecord.color)
        val animator = ValueAnimator.ofObject(ArgbEvaluator(), from, to)

        animator.duration = CLICK_ANIMATION_DURATION_MS
        animator.repeatCount = 1
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener {
            selectedRecordColor = it.animatedValue as? Int
                ?: return@addUpdateListener
            invalidate()
        }
        animator.start()
    }

    private fun View.measureText(
        width: Int,
        widthSpec: Int,
        height: Int = 0,
        heightSpec: Int = MeasureSpec.UNSPECIFIED,
    ) {
        val specWidth = MeasureSpec.makeMeasureSpec(width, widthSpec)
        val specHeight = MeasureSpec.makeMeasureSpec(height, heightSpec)
        measure(specWidth, specHeight)
        layout(0, 0, measuredWidth, measuredHeight)
    }

    private inner class Column(
        val legend: String,
        val data: List<Data>,
    )

    private inner class Data(
        val point: RecordsCalendarViewData.Point,
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
        val lastScaleFactor: Float,
        val panFactor: Float,
        val lastPanFactor: Float,
    ) : BaseSavedState(superSavedState)

    companion object {
        private const val CLICK_ANIMATION_DURATION_MS: Long = 250L
    }
}