package com.example.util.simpletimetracker.feature_statistics_detail.customView

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.util.simpletimetracker.core.utils.SingleTapDetector
import com.example.util.simpletimetracker.core.utils.SwipeDetector
import com.example.util.simpletimetracker.core.utils.isHorizontal
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_views.ColorUtils
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import kotlinx.parcelize.Parcelize
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr
) {
    // Attrs
    private var barCountInEdit: Int = 0
    private var barDividerMaxWidth: Int = 0
    private var barCornerRadius: Float = 0f
    private var barColor: Int = 0
    private var legendTextSuffix = ""
    private var legendTextSize: Float = 0f
    private var legendTextColor: Int = 0
    private var legendLineColor: Int = 0
    private var selectedBarBackgroundColor: Int = 0
    private var selectedBarTextColor: Int = 0
    private var showSelectedBarOnStart: Boolean = false
    private var addLegendToSelectedBar: Boolean = false
    private var shouldDrawHorizontalLegends: Boolean = true
    private var goalValue: Float = 0f
    // End of attrs

    private val bounds: RectF = RectF(0f, 0f, 0f, 0f)
    private var radiusArr: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private var barPath: Path = Path()
    private var bars: List<ViewData> = emptyList()
    private var maxValue: Float = 0f
    private var valueUpperBound: Long = 0
    private var nearestUpperStep: Long = 0
    private var pixelTopBound: Float = 0f
    private var pixelBottomBound: Float = 0f
    private var pixelRightBound: Float = 0f
    private var chartWidth: Float = 0f
    private var chartHeight: Float = 0f
    private var barWidth: Float = 0f
    private var barDividerWidth: Float = 0f
    private val legendTextPadding = 8.dpToPx()
    private val legendTextStartPadding = 4.dpToPx()
    private val legendHorizontalTextPadding = 2.dpToPx()
    private var longestTextWidth: Float = 0f
    private var legendLinesPixelStep: Float = 0f
    private var horizontalLegendsSkipCount: Int = 1
    private var selectedBar: Int = -1 // -1 nothing is selected
    private val selectedBarTextPadding: Int = 6.dpToPx()
    private val selectedBarBackgroundPadding: Int = 4.dpToPx()
    private val selectedBarBackgroundRadius: Float = 4.dpToPx().toFloat()
    private val selectedBarArrowWidth: Float = 4.dpToPx().toFloat()
    private var barAnimationScale: Float = 1f
    private val barAnimationDuration: Long = 300L // ms
    private var selectedBarWasShownOnStart: Boolean = false

    private val barPaint: Paint = Paint()
    private val selectedBarPaint: Paint = Paint()
    private val selectedBarBackgroundPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private val selectedBarTextPaint: Paint = Paint()
    private val linePaint: Paint = Paint()
    private val goalLinePaint: Paint = Paint()

    private val singleTapDetector = SingleTapDetector(
        context = context,
        onSingleTap = { onTouch(it, isClick = true) }
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
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = resolveSize(0, widthMeasureSpec)
        val h = resolveSize(0, heightMeasureSpec)

        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        if (bars.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()

        calculateDimensions(w, h)
        drawText(canvas, w)
        drawLines(canvas)
        drawBars(canvas)
        drawGoalValue(canvas)
        drawSelectedBarIcon(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> handled = true
        }

        return handled or singleTapDetector.onTouchEvent(event) or swipeDetector.onTouchEvent(event)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(
            superSavedState = superState,
            selectedBarWasShownOnStart = selectedBarWasShownOnStart,
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? SavedState
        super.onRestoreInstanceState(savedState?.superSavedState ?: state)
        selectedBarWasShownOnStart = savedState?.selectedBarWasShownOnStart ?: true
    }

    fun setBars(data: List<ViewData>) {
        bars = data.takeUnless { it.isEmpty() } ?: listOf(ViewData(0f, "", ""))
        maxValue = data.maxOfOrNull(ViewData::value) ?: 1f
        if (showSelectedBarOnStart && !selectedBarWasShownOnStart) {
            selectedBar = bars.size - 1
            selectedBarWasShownOnStart = true
        } else {
            selectedBar = -1
        }
        invalidate()
        if (!isInEditMode) animateBars()
    }

    fun setBarColor(color: Int) {
        barColor = color
        initPaint()
        invalidate()
    }

    fun setLegendTextSuffix(suffix: String) {
        legendTextSuffix = suffix
        invalidate()
    }

    fun shouldAddLegendToSelectedBar(shouldAdd: Boolean) {
        addLegendToSelectedBar = shouldAdd
        invalidate()
    }

    fun shouldDrawHorizontalLegends(shouldDraw: Boolean) {
        shouldDrawHorizontalLegends = shouldDraw
        invalidate()
    }

    fun setGoalValue(value: Float) {
        goalValue = value
        invalidate()
    }

    private fun initArgs(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) {
        context
            .obtainStyledAttributes(
                attrs,
                R.styleable.BarChartView, defStyleAttr, 0
            )
            .run {
                barCountInEdit =
                    getInt(R.styleable.BarChartView_barCount, 0)
                barDividerMaxWidth =
                    getDimensionPixelSize(R.styleable.BarChartView_dividerMaxWidth, 0)
                barCornerRadius =
                    getDimensionPixelSize(R.styleable.BarChartView_barCornerRadius, 0).toFloat()
                barColor =
                    getColor(R.styleable.BarChartView_barColor, Color.BLACK)
                legendTextSuffix =
                    getString(R.styleable.BarChartView_legendTextSuffix).orEmpty()
                legendTextSize =
                    getDimensionPixelSize(R.styleable.BarChartView_legendTextSize, 14).toFloat()
                legendTextColor =
                    getColor(R.styleable.BarChartView_legendTextColor, Color.BLACK)
                legendLineColor =
                    getColor(R.styleable.BarChartView_legendLineColor, Color.BLACK)
                selectedBarBackgroundColor =
                    getColor(R.styleable.BarChartView_selectedBarBackgroundColor, Color.WHITE)
                selectedBarTextColor =
                    getColor(R.styleable.BarChartView_selectedBarTextColor, Color.BLACK)
                showSelectedBarOnStart =
                    getBoolean(R.styleable.BarChartView_showSelectedBarOnStart, false)
                addLegendToSelectedBar =
                    getBoolean(R.styleable.BarChartView_addLegendToSelectedBar, false)
                shouldDrawHorizontalLegends =
                    getBoolean(R.styleable.BarChartView_shouldDrawHorizontalLegends, true)
                goalValue =
                    getFloat(R.styleable.BarChartView_goalValue, 0f)
                recycle()
            }
    }

    private fun initPaint() {
        barPaint.apply {
            isAntiAlias = true
            color = barColor
            style = Paint.Style.FILL
        }
        selectedBarPaint.apply {
            isAntiAlias = true
            color = ColorUtils.darkenColor(barColor)
            style = Paint.Style.FILL
        }
        selectedBarBackgroundPaint.apply {
            isAntiAlias = true
            color = selectedBarBackgroundColor
            style = Paint.Style.FILL
        }
        textPaint.apply {
            isAntiAlias = true
            color = legendTextColor
            textSize = legendTextSize
        }
        selectedBarTextPaint.apply {
            isAntiAlias = true
            color = selectedBarTextColor
            textSize = legendTextSize
        }
        linePaint.apply {
            isAntiAlias = true
            color = legendLineColor
        }
        goalLinePaint.apply {
            isAntiAlias = true
            color = ColorUtils.darkenColor(barColor)
            style = Paint.Style.STROKE
            strokeWidth = 1.dpToPx().toFloat()
        }
    }

    private fun calculateDimensions(w: Float, h: Float) {
        // Min value change between legend lines
        val minDivider = if (maxValue > 5f) 5L else 1L

        // Coerce max value to min divider
        val maxValue: Float = maxValue.takeIf { it > minDivider.toFloat() } ?: minDivider.toFloat()

        // How many legend texts with padding can be fit into height
        val canFitNumberOfTexts: Long = (h / (legendTextSize + 2 * legendTextPadding)).toLong()

        // Value step between legend lines
        val step: Float = maxValue / canFitNumberOfTexts.toFloat()

        // Coerce value step between legend lines to multiple of min divider
        nearestUpperStep = nearestUpper(minDivider, step)

        // Max legend line value
        valueUpperBound = nearestUpper(nearestUpperStep, maxValue)

        longestTextWidth = textPaint.measureText("$valueUpperBound$legendTextSuffix")

        // Horizontal legends
        val legends = bars.map(ViewData::legend).filter { it.isNotEmpty() }

        // Bar chart bounds
        val textHeight = textPaint.fontMetrics.let { it.descent - it.ascent }
        pixelBottomBound = if (shouldDrawHorizontalLegends) {
            h - textHeight
        } else {
            h
        }
        pixelTopBound = legendTextSize
        pixelRightBound = w - longestTextWidth - legendTextStartPadding

        // Bar chart size
        chartWidth = pixelRightBound
        chartHeight = pixelBottomBound - pixelTopBound
        barWidth = chartWidth / bars.size
        barDividerWidth = barDividerMaxWidth.takeIf { it < barWidth / 2 }?.toFloat().orZero()

        // Legend lines value points
        val points = (0..valueUpperBound step nearestUpperStep).toList()

        // How many legend lines need to draw
        val legendLinesCount = points.size

        // Pixel step between legend lines
        legendLinesPixelStep = chartHeight / (legendLinesCount - 1)

        // Horizontal legends size
        val maxHorizontalLegendValue: String = legends.maxByOrNull { it.length }.orEmpty()
        val maxHorizontalLegendSize = textPaint.measureText(maxHorizontalLegendValue) +
            2 * legendHorizontalTextPadding
        if (maxHorizontalLegendSize > 0f) {
            val canFit = floor(chartWidth / maxHorizontalLegendSize)
            if (canFit > 0f) {
                horizontalLegendsSkipCount = ceil(bars.size / canFit).toInt()
            }
        }
        if (horizontalLegendsSkipCount == 0) horizontalLegendsSkipCount = 1
    }

    private fun drawText(canvas: Canvas, w: Float) {
        // Legend lines value points
        val points = (0..valueUpperBound step nearestUpperStep).toList()

        points.forEachIndexed { index, point ->
            val pointText = "$point$legendTextSuffix"
            canvas.drawText(
                pointText,
                w - textPaint.measureText(pointText) / 2 - longestTextWidth / 2,
                pixelBottomBound - legendLinesPixelStep * index,
                textPaint
            )
        }

        if (!shouldDrawHorizontalLegends) return
        bars.map(ViewData::legend).reversed().forEachIndexed { index, legend ->
            if (legend.isNotEmpty() && index % horizontalLegendsSkipCount == 0) {
                val textStart = pixelRightBound -
                    barWidth * index - barWidth / 2 -
                    textPaint.measureText(legend) / 2

                if (textStart >= 0) canvas.drawText(
                    legend,
                    textStart,
                    pixelBottomBound + legendTextSize,
                    textPaint
                )
            }
        }
    }

    private fun drawBars(canvas: Canvas) {
        radiusArr = floatArrayOf(
            barCornerRadius, barCornerRadius,
            barCornerRadius, barCornerRadius,
            0f, 0f,
            0f, 0f
        )

        canvas.save()

        bars.forEachIndexed { index, bar ->
            // Normalize bar values to max legend line value
            val scaled = bar.value * barAnimationScale / valueUpperBound
            bounds.set(
                0f + barDividerWidth / 2,
                pixelTopBound + chartHeight * (1f - scaled),
                barWidth - barDividerWidth / 2,
                pixelBottomBound
            )
            barPath = Path().apply {
                addRoundRect(bounds, radiusArr, Path.Direction.CW)
            }
            canvas.drawPath(barPath, if (index == selectedBar) selectedBarPaint else barPaint)
            canvas.translate(barWidth, 0f)
        }

        canvas.restore()
    }

    private fun drawLines(canvas: Canvas) {
        val points = (0..valueUpperBound step nearestUpperStep).toList()
        points.forEachIndexed { index, _ ->
            canvas.drawLine(
                0f,
                pixelBottomBound - legendLinesPixelStep * index,
                pixelRightBound,
                pixelBottomBound - legendLinesPixelStep * index,
                linePaint
            )
        }
    }

    private fun drawGoalValue(canvas: Canvas) {
        if (goalValue == 0f) return

        val scaled = goalValue / valueUpperBound
        canvas.drawLine(
            0f,
            pixelBottomBound - chartHeight * scaled,
            pixelRightBound,
            pixelBottomBound - chartHeight * scaled,
            goalLinePaint,
        )
    }

    private fun drawSelectedBarIcon(canvas: Canvas) {
        val bar = bars.getOrNull(selectedBar) ?: return
        val barValue = bar.value

        val scaled = barValue / valueUpperBound
        val barTop = pixelTopBound + chartHeight * (1f - scaled)
        val barCenterX = barWidth * selectedBar + barWidth / 2
        val pointText = getSelectedBarText(bar)
        val textWidth = selectedBarTextPaint.measureText(pointText)
        val textHeight = selectedBarTextPaint.fontMetrics.let { it.descent - it.ascent }

        val backgroundWidth = textWidth + 2 * selectedBarTextPadding
        val backgroundHeight = textHeight + 2 * selectedBarTextPadding
        val backgroundCenterX = max(
            min(barCenterX, pixelRightBound - backgroundWidth / 2),
            backgroundWidth / 2
        )
        val backgroundCenterY = max(
            barTop - selectedBarBackgroundPadding - backgroundHeight / 2,
            backgroundHeight / 2
        )

        canvas.save()

        canvas.translate(backgroundCenterX, backgroundCenterY)

        // Draw background
        bounds.set(
            -backgroundWidth / 2,
            -backgroundHeight / 2,
            backgroundWidth / 2,
            backgroundHeight / 2
        )
        canvas.drawRoundRect(
            bounds,
            selectedBarBackgroundRadius,
            selectedBarBackgroundRadius,
            selectedBarBackgroundPaint
        )

        // Draw text
        canvas.drawText(
            pointText,
            bounds.left + selectedBarTextPadding,
            bounds.bottom - selectedBarTextPadding,
            selectedBarTextPaint
        )

        canvas.restore()

        canvas.translate(0f, backgroundCenterY)

        // Draw arrow shape
        //      ----
        // _____|  |_____
        //       \/
        val path = Path()
        val arrowLeft = barCenterX - selectedBarArrowWidth / 2
        val arrowRight = barCenterX + selectedBarArrowWidth / 2
        path.fillType = Path.FillType.EVEN_ODD
        path.moveTo(arrowLeft, bounds.bottom - selectedBarBackgroundRadius)
        path.lineTo(min(arrowRight, pixelRightBound), bounds.bottom - selectedBarBackgroundRadius)
        path.lineTo(min(arrowRight, pixelRightBound), bounds.bottom)
        path.lineTo(barCenterX, bounds.bottom + selectedBarBackgroundPadding)
        path.lineTo(arrowLeft, bounds.bottom)
        path.close()
        canvas.drawPath(path, selectedBarBackgroundPaint)

        canvas.restore()
    }

    /**
     * Finds next multiple of divider bigger than value.
     * Ex. value = 31, divider = 5, result 35.
     */
    private fun nearestUpper(divider: Long, value: Float): Long {
        if (value == 0f) return divider
        return divider * (ceil(abs(value / divider.toFloat()))).toLong()
    }

    private fun initEditMode() {
        if (isInEditMode) {
            val segments = barCountInEdit.takeIf { it != 0 } ?: 5
            (segments downTo 1).toList()
                .map { ViewData(it.toFloat(), it.toString(), it.toString()) }
                .let(::setBars)
            selectedBar = barCountInEdit / 2
        }
    }

    private fun onTouch(event: MotionEvent, isClick: Boolean) {
        val x = event.x
        val y = event.y
        val clickedAroundBar = floor(x / barWidth).toInt()

        bars.getOrNull(clickedAroundBar)?.let {
            if (y > pixelTopBound && y < pixelBottomBound) {
                // If clicked on the same bar - clear selection
                selectedBar = if (isClick && selectedBar == clickedAroundBar) {
                    -1
                } else {
                    clickedAroundBar
                }
                invalidate()
                return
            }
        }

        selectedBar = -1
        invalidate()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onSwipe(offset: Float, direction: SwipeDetector.Direction, event: MotionEvent) {
        if (direction.isHorizontal()) {
            parent.requestDisallowInterceptTouchEvent(true)
            onTouch(event, isClick = false)
        }
    }

    private fun onSwipeStop() {
        parent.requestDisallowInterceptTouchEvent(false)
    }

    private fun animateBars() {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = barAnimationDuration
        animator.addUpdateListener { animation ->
            barAnimationScale = animation.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    private fun getSelectedBarText(bar: ViewData): String {
        val barValue = bar.value
        val barLegend = bar.selectedBarLegend

        return "%.1f".format(barValue).let {
            if (addLegendToSelectedBar && barLegend.isNotEmpty()) {
                "$barLegend - $it"
            } else {
                it
            } + legendTextSuffix
        }
    }

    data class ViewData(
        val value: Float,
        val legend: String,
        val selectedBarLegend: String = legend,
    )

    @Parcelize
    private class SavedState(
        val superSavedState: Parcelable?,
        val selectedBarWasShownOnStart: Boolean,
    ) : BaseSavedState(superSavedState)
}