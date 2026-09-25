package com.example.util.simpletimetracker.feature_views.barChart

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import com.example.util.simpletimetracker.feature_views.ColorUtils
import com.example.util.simpletimetracker.feature_views.R
import com.example.util.simpletimetracker.feature_views.SingleTapDetector
import com.example.util.simpletimetracker.feature_views.SwipeDetector
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.ifNull
import com.example.util.simpletimetracker.feature_views.isHorizontal
import kotlinx.parcelize.Parcelize
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(
    context,
    attrs,
    defStyleAttr,
) {
    // Attrs
    private var barCountInEdit: Int = 0
    private var barDividerMaxWidth: Int = 0
    private var barCornerRadius: Float = 0f
    private var legendTextSuffix = ""
    private var legendTextSize: Float = 0f
    private var legendTextColor: Int = 0
    private var legendLineColor: Int = 0
    private var selectedBarBackgroundColor: Int = 0
    private var selectedBarTextColor: Int = 0
    private var selectedBarColor: Int = 0
    private var showSelectedBarOnStart: Boolean = false
    private var addLegendToSelectedBar: Boolean = false
    private var shouldDrawHorizontalLegends: Boolean = true
    private var goalValues: List<Float> = emptyList()
    private var yAxisZoomed: Boolean = false
    // End of attrs

    private val bounds: RectF = RectF(0f, 0f, 0f, 0f)
    private val textBounds: Rect = Rect(0, 0, 0, 0)
    private var radiusArr: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private var radiusArrNegative: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private var barPath: Path = Path()
    private var bars: List<ViewData> = emptyList()
    private var maxPositiveValue: Float = 0f
    private var maxNegativeValue: Float = 0f
    private var maxTagValue: Float = 0f
    private var minTagValue: Float = 0f
    private var valueUpperBound: Float = 0f
    private var valueDownerBound: Float = 0f
    private var nearestValueStep: Float = 0f
    private var legendValues: List<Float> = emptyList()
    private var legendValuesPrecision: Int = 0
    private var pixelTopBound: Float = 0f
    private var pixelBottomBound: Float = 0f
    private var pixelLeftBound: Float = 0f
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
    private var selectedBarPosition: Int? = null
    private val selectedBarTextPadding: Int = 6.dpToPx()
    private val selectedBarBackgroundPadding: Int = 4.dpToPx()
    private val selectedBarBackgroundRadius: Float = 4.dpToPx().toFloat()
    private val selectedBarArrowWidth: Float = 6.dpToPx().toFloat()
    private var barAnimationScale: Float = 1f
    private val barAnimationDuration: Long = 300L // ms
    private var selectedBarWasShownOnStart: Boolean = false
    private var singleColor: Int? = null
    private var drawRoundCaps: Boolean = true
    private var onBarClickListener: ((Long?) -> Unit)? = null

    private val barPaint: Paint = Paint()
    private val selectedBarPaint: Paint = Paint()
    private val selectedBarBackgroundPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private val selectedBarTextPaint: Paint = Paint()
    private val linePaint: Paint = Paint()
    private val goalLinePaint: Paint = Paint()
    private val barPartsDividerPaint: Paint = Paint()

    private val singleTapDetector = SingleTapDetector(
        context = context,
        onSingleTap = { onTouch(it, isClick = true) },
    )
    private val swipeDetector = SwipeDetector(
        context = context,
        onSlide = ::onSwipe,
        onSlideStop = ::onSwipeStop,
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
        drawGoalValues(canvas)
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

    fun setBars(
        data: List<ViewData>,
        selectedBarPosition: Int?,
        animate: Boolean,
    ) {
        bars = data.takeUnless { it.isEmpty() } ?: listOf(
            ViewData(
                id = 0,
                value = listOf(0f to Color.BLACK),
                legend = "",
                selectedBarLegend = "",
            ),
        )

        val barTotals = bars
            .filter { bar -> bar.value.isNotEmpty() }
            .map { bar -> bar.value.sumOf { it.first.toDouble() }.toFloat() }

        minTagValue = barTotals.minOrNull() ?: 0f
        maxTagValue = barTotals.maxOrNull() ?: 0f

        maxPositiveValue = data
            .map { barData -> barData.value.map { it.first }.sum() }
            .filter { it >= 0f }
            .maxOrNull() ?: 1f
        maxNegativeValue = data
            .map { barData -> barData.value.map { it.first }.sum() }
            .filter { it < 0f }
            .minOrNull() ?: 0f

        if (data.isNotEmpty() && showSelectedBarOnStart && !selectedBarWasShownOnStart) {
            this.selectedBarPosition = bars.size - 1
            selectedBarWasShownOnStart = true
        } else {
            this.selectedBarPosition = selectedBarPosition
        }

        invalidate()
        if (!isInEditMode && animate) animateBars()
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

    fun setGoalValues(values: List<Float>) {
        goalValues = values
        invalidate()
    }

    fun setYAxisZoomed(value: Boolean) {
        yAxisZoomed = value
        invalidate()
    }

    fun showSelectedBarOnStart(shouldShow: Boolean) {
        showSelectedBarOnStart = shouldShow
    }

    fun setSingleColor(@ColorInt singleColor: Int?) {
        this.singleColor = singleColor
        invalidate()
    }

    fun setDrawRoundCaps(drawRoundCaps: Boolean) {
        this.drawRoundCaps = drawRoundCaps
        invalidate()
    }

    fun setOnBarClickListener(listener: (Long?) -> Unit) {
        onBarClickListener = listener
    }

    private fun initArgs(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) {
        context
            .withStyledAttributes(
                attrs,
                R.styleable.BarChartView, defStyleAttr, 0,
            ) {
                barCountInEdit =
                    getInt(R.styleable.BarChartView_barCount, 0)
                barDividerMaxWidth =
                    getDimensionPixelSize(R.styleable.BarChartView_dividerMaxWidth, 0)
                barCornerRadius =
                    getDimensionPixelSize(R.styleable.BarChartView_barCornerRadius, 0).toFloat()
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
                selectedBarColor =
                    getColor(R.styleable.BarChartView_selectedBarColor, Color.BLACK)
                showSelectedBarOnStart =
                    getBoolean(R.styleable.BarChartView_showSelectedBarOnStart, false)
                addLegendToSelectedBar =
                    getBoolean(R.styleable.BarChartView_addLegendToSelectedBar, false)
                shouldDrawHorizontalLegends =
                    getBoolean(R.styleable.BarChartView_shouldDrawHorizontalLegends, true)
                goalValues =
                    listOf(getFloat(R.styleable.BarChartView_goalValue, 0f))
            }
    }

    private fun initPaint() {
        barPaint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        selectedBarPaint.apply {
            isAntiAlias = true
            color = ColorUtils.changeAlpha(selectedBarColor, 0.10f)
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
            style = Paint.Style.STROKE
            strokeWidth = 1.dpToPx().toFloat()
        }
        barPartsDividerPaint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1.dpToPx().toFloat()
        }
    }

    private fun calculateDimensions(w: Float, h: Float) {
        if (yAxisZoomed) {
            calculateDimensionsFromData(h)
        } else {
            calculateDimensionsFromZero(h)
        }

        legendValues = buildLegendValues(
            lowerBound = valueDownerBound,
            upperBound = valueUpperBound,
            step = nearestValueStep,
        )

        legendValuesPrecision = calculateLegendPrecision(
            step = nearestValueStep,
        )

        val maxBound = listOf(valueUpperBound, valueDownerBound)
            .map { formatLegendValue(it, legendValuesPrecision) }
            .maxByOrNull { it.length }.orEmpty()
        longestTextWidth = textPaint.measureText("$maxBound$legendTextSuffix")

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
        pixelLeftBound = 0f
        pixelRightBound = w - longestTextWidth - legendTextStartPadding

        // Bar chart size
        chartWidth = pixelRightBound - pixelLeftBound
        chartHeight = pixelBottomBound - pixelTopBound
        barWidth = chartWidth / bars.size
        barDividerWidth = barDividerMaxWidth.takeIf { it < barWidth / 2 }?.toFloat().ifNull { 0f }

        // How many legend lines need to draw
        val legendLinesCount = legendValues.size

        // Pixel step between legend lines
        legendLinesPixelStep = if (legendLinesCount > 1) {
            chartHeight / (legendLinesCount - 1)
        } else {
            chartHeight
        }

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

        val gradientColor = singleColor
        if (gradientColor != null) {
            barPaint.shader = LinearGradient(
                0f,
                chartHeight,
                0f,
                chartHeight / 2,
                ColorUtils.changeAlpha(gradientColor, 0.75f),
                gradientColor,
                Shader.TileMode.CLAMP,
            )
        } else {
            barPaint.shader = null
        }
    }

    private fun drawText(canvas: Canvas, w: Float) {
        legendValues.forEach { point ->
            val formattedLegendValue = formatLegendValue(point, legendValuesPrecision)
            val pointText = "$formattedLegendValue$legendTextSuffix"
            canvas.drawText(
                pointText,
                w - textPaint.measureText(pointText) / 2 - longestTextWidth / 2,
                valueToPixel(point),
                textPaint,
            )
        }

        if (!shouldDrawHorizontalLegends) return
        bars.map(ViewData::legend).reversed().forEachIndexed { index, legend ->
            if (legend.isNotEmpty() && index % horizontalLegendsSkipCount == 0) {
                val textStart = pixelRightBound -
                    barWidth * index - barWidth / 2 -
                    textPaint.measureText(legend) / 2

                if (textStart >= 0) {
                    canvas.drawText(
                        legend,
                        textStart,
                        pixelBottomBound + legendTextSize,
                        textPaint,
                    )
                }
            }
        }
    }

    @SuppressLint("UseKtx")
    private fun drawBars(canvas: Canvas) {
        var prevColor = 0
        radiusArr = floatArrayOf(
            barCornerRadius, barCornerRadius,
            barCornerRadius, barCornerRadius,
            0f, 0f,
            0f, 0f,
        )
        radiusArrNegative = floatArrayOf(
            0f, 0f,
            0f, 0f,
            barCornerRadius, barCornerRadius,
            barCornerRadius, barCornerRadius,
        )
        if (singleColor != null) barPaint.color = singleColor.ifNull { 0 }

        canvas.save()

        bars.forEachIndexed { index, bar ->
            val drawFromValue = when {
                valueDownerBound > 0f -> valueDownerBound
                valueDownerBound == 0f -> 0f
                valueUpperBound > 0f && valueDownerBound < 0f -> 0f
                else -> valueUpperBound
            }
            var positiveValue = drawFromValue
            var negativeValue = drawFromValue
            val size = bar.value.size
            bar.value.forEachIndexed { partIndex, (value, color) ->
                if (singleColor == null) barPaint.color = color

                // If values are positive - count from lowerBound to the top,
                // if negative - from upperBound to bottom.
                val startValue = if (value >= 0f) {
                    positiveValue
                } else {
                    negativeValue
                }
                val endValue = startValue + (value - drawFromValue) * barAnimationScale

                bounds.set(
                    0f + barDividerWidth / 2,
                    min(valueToPixel(startValue), valueToPixel(endValue)),
                    barWidth - barDividerWidth / 2,
                    max(valueToPixel(startValue), valueToPixel(endValue)),
                )
                val radius = if (value >= 0) radiusArr else radiusArrNegative
                barPath = Path().apply {
                    // Draw round caps only if single colored,
                    // otherwise it wouldn't be visible on some small bar parts.
                    if (partIndex == size - 1 && drawRoundCaps) {
                        addRoundRect(bounds, radius, Path.Direction.CW)
                    } else {
                        addRect(bounds, Path.Direction.CW)
                    }
                }
                canvas.drawPath(barPath, barPaint)

                // Draw divider
                if (partIndex != 0 && prevColor == color) {
                    barPartsDividerPaint.color = ColorUtils.darkenColor(color)
                    val dividerY = valueToPixel(startValue)
                    canvas.drawLine(
                        0f + barDividerWidth / 2,
                        dividerY,
                        barWidth - barDividerWidth / 2,
                        dividerY,
                        barPartsDividerPaint,
                    )
                }

                prevColor = color
                if (value >= 0f) {
                    positiveValue = endValue
                } else {
                    negativeValue = endValue
                }
            }

            // Draw selected bar
            if (index == selectedBarPosition) {
                bounds.set(
                    0f + barDividerWidth / 2,
                    pixelTopBound,
                    barWidth - barDividerWidth / 2,
                    pixelBottomBound,
                )
                barPath = Path().apply {
                    addRect(bounds, Path.Direction.CW)
                }
                canvas.drawPath(barPath, selectedBarPaint)
            }

            canvas.translate(barWidth, 0f)
        }

        canvas.restore()
    }

    private fun drawLines(canvas: Canvas) {
        legendValues.forEach { point ->
            canvas.drawLine(
                0f,
                valueToPixel(point),
                pixelRightBound,
                valueToPixel(point),
                linePaint,
            )
        }
    }

    private fun drawGoalValues(canvas: Canvas) {
        goalLinePaint.color = singleColor
            ?.let(ColorUtils::darkenColor)
            ?: Color.BLACK
        goalValues.filter { it != 0f }.forEach { goalValue ->
            val y = valueToPixel(goalValue)
            canvas.drawLine(
                0f,
                y,
                pixelRightBound,
                y,
                goalLinePaint,
            )
        }
    }

    private fun drawSelectedBarIcon(canvas: Canvas) {
        val selectedBar = selectedBarPosition ?: return
        val bar = bars.getOrNull(selectedBar) ?: return
        val barValue = bar.value.map { it.first }.sum().coerceIn(valueDownerBound, valueUpperBound)
        val isOnPositiveChart = barValue >= 0f || valueDownerBound >= 0f
        val barTop = valueToPixel(barValue)
        val barCenterX = barWidth * selectedBar + barWidth / 2
        val pointText = getSelectedBarText(bar)
        val textWidth = selectedBarTextPaint.measureText(pointText)
        // Use one static text to avoid height jumps between bars.
        val textToMeasure = getSelectedBarText(bars.firstOrNull() ?: bar)
        selectedBarTextPaint.getTextBounds(textToMeasure, 0, textToMeasure.length, textBounds)
        val textHeight = textBounds.height().toFloat()

        val backgroundWidth = textWidth + 2 * selectedBarTextPadding
        val backgroundHeight = textHeight + 2 * selectedBarTextPadding
        val backgroundCenterX = max(
            min(barCenterX, pixelRightBound - backgroundWidth / 2),
            backgroundWidth / 2,
        )
        val backgroundCenterY = if (isOnPositiveChart) {
            max(
                barTop - selectedBarBackgroundPadding - backgroundHeight / 2,
                backgroundHeight / 2,
            )
        } else {
            min(
                barTop + selectedBarBackgroundPadding + backgroundHeight / 2,
                pixelBottomBound - backgroundHeight / 2,
            )
        }

        canvas.save()

        canvas.translate(backgroundCenterX, backgroundCenterY)

        // Draw background
        bounds.set(
            -backgroundWidth / 2,
            -backgroundHeight / 2,
            backgroundWidth / 2,
            backgroundHeight / 2,
        )
        canvas.drawRoundRect(
            bounds,
            selectedBarBackgroundRadius,
            selectedBarBackgroundRadius,
            selectedBarBackgroundPaint,
        )

        // Draw text
        canvas.drawText(
            pointText,
            bounds.left + selectedBarTextPadding,
            bounds.bottom - selectedBarTextPadding,
            selectedBarTextPaint,
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
        if (isOnPositiveChart) {
            path.moveTo(max(arrowLeft, pixelLeftBound), bounds.bottom - selectedBarBackgroundRadius)
            path.lineTo(min(arrowRight, pixelRightBound), bounds.bottom - selectedBarBackgroundRadius)
            path.lineTo(min(arrowRight, pixelRightBound), bounds.bottom)
            path.lineTo(barCenterX, bounds.bottom + selectedBarBackgroundPadding)
            path.lineTo(max(arrowLeft, pixelLeftBound), bounds.bottom)
        } else {
            path.moveTo(max(arrowLeft, pixelLeftBound), bounds.top + selectedBarBackgroundRadius)
            path.lineTo(min(arrowRight, pixelRightBound), bounds.top + selectedBarBackgroundRadius)
            path.lineTo(min(arrowRight, pixelRightBound), bounds.top)
            path.lineTo(barCenterX, bounds.top - selectedBarBackgroundPadding)
            path.lineTo(max(arrowLeft, pixelLeftBound), bounds.top)
        }
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

    private fun calculateDimensionsFromZero(h: Float) {
        // Total value span
        val totalAbsValue = maxPositiveValue + abs(maxNegativeValue)
        // Min value change between legend lines
        val minDivider = if (totalAbsValue > 5f) 5L else 1L

        // Coerce max/min value to min divider
        val maxValue: Float = maxPositiveValue.takeIf { it > minDivider.toFloat() }
            ?: minDivider.toFloat()
        val minValue: Float = maxNegativeValue.takeIf { it < -minDivider.toFloat() }
            ?: (if (maxNegativeValue != 0f) -minDivider.toFloat() else 0f)
        val totalCoercedValue = maxValue + abs(minValue)

        // How many legend texts with padding can be fit into height
        val canFitNumberOfTexts = max((h / (legendTextSize + 2 * legendTextPadding)).toLong(), 2L)

        // Value step between legend lines
        val valueStep: Float = totalCoercedValue / canFitNumberOfTexts.toFloat()

        // Coerce value step between legend lines to multiple of min divider
        nearestValueStep = nearestUpper(divider = minDivider, value = valueStep).toFloat()

        // Max legend line value
        valueUpperBound = nearestUpper(divider = nearestValueStep.toLong(), value = maxValue).toFloat()
        valueDownerBound = if (minValue != 0f) {
            -nearestUpper(divider = nearestValueStep.toLong(), value = abs(minValue)).toFloat()
        } else {
            0f
        }
    }

    private fun calculateDimensionsFromData(h: Float) {
        val minValue = minTagValue
        val maxValue = maxTagValue

        // How many legend texts with padding can be fit into height.
        val canFitNumberOfTexts = max((h / (legendTextSize + 2 * legendTextPadding)).toInt(), 2)
        // If range is zero (flat line bars) - take some small range instead, to expand it.
        val range = max(maxValue - minValue, 0.01f)
        val tentativeStep = range / max((canFitNumberOfTexts - 1), 1)
        nearestValueStep = findNiceStep(tentativeStep)

        // Expand final lower/upper bounds to exact multiples of that step.
        valueDownerBound = floor(minValue / nearestValueStep) * nearestValueStep
        valueUpperBound = ceil(maxValue / nearestValueStep) * nearestValueStep

        // Expand range.
        valueUpperBound += nearestValueStep
        valueDownerBound = if (minValue > 0f) {
            valueDownerBound - nearestValueStep
        } else {
            valueDownerBound
        }
    }

    private fun buildLegendValues(
        lowerBound: Float,
        upperBound: Float,
        step: Float,
    ): List<Float> {
        if (step <= 0f || upperBound <= lowerBound) {
            return listOf(lowerBound, upperBound)
        }
        val linesCount = (((upperBound - lowerBound) / step).roundToInt() + 1).coerceAtLeast(2)
        return (0 until linesCount).map { index ->
            lowerBound + step * index
        }.toMutableList().apply {
            // Replace last by upper bound.
            this[lastIndex] = upperBound
        }
    }

    private fun findNiceStep(value: Float): Float {
        if (value <= 0f) return 1f
        // For example:
        // 370 = 3.7 * 10^2 - exponent = 2, fraction = 3.7
        val exponent = floor(log10(value.toDouble())).toInt()
        val fraction = value / 10f.pow(exponent)
        // More readable fraction.
        val niceFraction = when {
            fraction <= 1f -> 1f
            fraction <= 2f -> 2f
            fraction <= 5f -> 5f
            else -> 10f
        }
        // Rebuild step from exponent and fraction.
        return niceFraction * 10f.pow(exponent)
    }

    // Uses legend step to preserve distinct adjacent legend labels.
    // Precision 0 - 90, 1 - 90.1, 2 - 90.01
    private fun calculateLegendPrecision(step: Float): Int {
        val normalizedStep = abs(step)
        val epsilon = 0.0001f
        val maxPrecision = 3

        for (precision in 0..maxPrecision) {
            val scaled = normalizedStep * 10f.pow(precision)
            if (abs(scaled - scaled.roundToInt()) < epsilon) {
                return precision
            }
        }
        return maxPrecision
    }

    private fun formatLegendValue(value: Float, precision: Int): String {
        val normalized = if (abs(value) < 0.0001f) 0f else value
        return "%.${precision}f".format(normalized)
    }

    private fun valueToPixel(value: Float): Float {
        val range = valueUpperBound - valueDownerBound
        if (range <= 0f) return pixelBottomBound
        val scaled = (value - valueDownerBound) / range
        return pixelBottomBound - chartHeight * scaled
    }

    private fun initEditMode() {
        if (isInEditMode) {
            val segments = barCountInEdit.takeIf { it != 0 } ?: 5
            (segments downTo 1).toList()
                .map {
                    ViewData(
                        id = it.toLong(),
                        value = listOf(it.toFloat() to Color.BLACK),
                        legend = it.toString(),
                        selectedBarLegend = it.toString(),
                    )
                }
                .let {
                    setBars(
                        data = it,
                        selectedBarPosition = barCountInEdit / 2,
                        animate = false,
                    )
                }
            singleColor = Color.BLACK
        }
    }

    private fun onTouch(event: MotionEvent, isClick: Boolean) {
        val x = event.x
        val y = event.y

        val clickedAroundBar = floor(x / barWidth).toInt()
        val clickedBar = bars.getOrNull(clickedAroundBar)

        if (clickedBar != null && y > pixelTopBound && y < pixelBottomBound) {
            // If clicked on the same bar - clear selection
            if (isClick && selectedBarPosition == clickedAroundBar) {
                onBarClickListener?.invoke(null)
                selectedBarPosition = null
            } else {
                onBarClickListener?.invoke(clickedBar.id)
                selectedBarPosition = clickedAroundBar
            }
            invalidate()
        } else {
            selectedBarPosition = null
            onBarClickListener?.invoke(null)
            invalidate()
        }
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
        val barValue = bar.value.map { it.first }.sum()
        val barLegend = bar.selectedBarLegend

        // Increase precision for selected bar.
        val formattedLegendValue = formatLegendValue(barValue, legendValuesPrecision + 1)

        return formattedLegendValue.let {
            if (addLegendToSelectedBar && barLegend.isNotEmpty()) {
                "$barLegend $it"
            } else {
                it
            } + legendTextSuffix
        }
    }

    data class ViewData(
        val id: Long,
        // Value to Color.
        // For empty durations will be listOf(0), for empty tag values will be emptyList().
        val value: List<Pair<Float, Int>>,
        val legend: String,
        val selectedBarLegend: String = legend,
    )

    @Parcelize
    private class SavedState(
        val superSavedState: Parcelable?,
        val selectedBarWasShownOnStart: Boolean,
    ) : BaseSavedState(superSavedState)
}