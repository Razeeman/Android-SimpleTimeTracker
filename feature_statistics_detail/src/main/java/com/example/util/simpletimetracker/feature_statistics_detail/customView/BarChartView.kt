package com.example.util.simpletimetracker.feature_statistics_detail.customView

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.util.simpletimetracker.core.extension.dpToPx
import com.example.util.simpletimetracker.core.utils.SingleTapDetector
import com.example.util.simpletimetracker.feature_statistics_detail.R
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

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
    private var barDividerWidth: Int = 0
    private var barCornerRadius: Float = 0f
    private var barColor: Int = 0
    private var legendTextSuffix = ""
    private var legendTextSize: Float = 0f
    private var legendTextColor: Int = 0
    private var legendLineColor: Int = 0
    private var selectedBarBackgroundColor: Int = 0
    private var selectedBarTextColor: Int = 0
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
    private val legendTextPadding = 8.dpToPx()
    private val legendTextStartPadding = 4.dpToPx()
    private var longestTextWidth: Float = 0f
    private var legendLinesPixelStep: Float = 0f
    private var selectedBar: Int = -1 // -1 nothing is selected
    private val selectedBarTextPadding: Int = 6.dpToPx()
    private val selectedBarBackgroundPadding: Int = 4.dpToPx()
    private val selectedBarBackgroundRadius: Float = 4.dpToPx().toFloat()
    private var barAnimationScale: Float = 1f
    private val barAnimationDuration: Long = 300L // ms

    private val barPaint: Paint = Paint()
    private val selectedBarBackgroundPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private val selectedBarTextPaint: Paint = Paint()
    private val linePaint: Paint = Paint()

    private val singleTapDetector = SingleTapDetector(context) { event ->
        onClick(event.x, event.y)
    }

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

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null || bars.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()

        calculateDimensions(w, h)
        drawText(canvas, w, h)
        drawLines(canvas)
        drawBars(canvas)
        drawSelectedBarIcon(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> handled = true
        }

        return handled or singleTapDetector.onTouchEvent(event)
    }

    fun setBars(data: List<ViewData>) {
        bars = data.takeUnless { it.isEmpty() } ?: listOf(ViewData(0f))
        maxValue = data.map(ViewData::value).max() ?: 1f
        selectedBar = -1
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
                barDividerWidth =
                    getDimensionPixelSize(R.styleable.BarChartView_dividerWidth, 0)
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
                legendLineColor =
                    getColor(R.styleable.BarChartView_legendLineColor, Color.BLACK)
                selectedBarBackgroundColor =
                    getColor(R.styleable.BarChartView_selectedBarBackgroundColor, Color.WHITE)
                selectedBarTextColor =
                    getColor(R.styleable.BarChartView_selectedBarTextColor, Color.BLACK)
                recycle()
            }
    }

    private fun initPaint() {
        barPaint.apply {
            isAntiAlias = true
            color = barColor
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

        // Bar chart bounds
        pixelBottomBound = if (bars.mapNotNull(ViewData::legend).isEmpty()) {
            h.toFloat()
        } else {
            h - legendTextSize
        }
        pixelTopBound = legendTextSize
        pixelRightBound = w - longestTextWidth - legendTextStartPadding

        // Bar chart size
        chartWidth = pixelRightBound
        chartHeight = pixelBottomBound - pixelTopBound
        barWidth = chartWidth / bars.size

        // Legend lines value points
        val points = (0..valueUpperBound step nearestUpperStep).toList()

        // How many legend lines need to draw
        val legendLinesCount = points.size

        // Pixel step between legend lines
        legendLinesPixelStep = chartHeight / (legendLinesCount - 1)
    }

    private fun drawText(canvas: Canvas, w: Float, h: Float) {
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

        bars.map(ViewData::legend).forEachIndexed { index, legend ->
            if (legend.isNullOrEmpty()) return@forEachIndexed

            canvas.drawText(
                legend,
                barWidth * index + barWidth / 2 - textPaint.measureText(legend) / 2,
                h,
                textPaint
            )
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

        bars.forEach {
            // Normalize bar values to max legend line value
            val scaled = it.value * barAnimationScale / valueUpperBound
            bounds.set(
                0f + barDividerWidth / 2,
                pixelTopBound + chartHeight * (1f - scaled),
                barWidth - barDividerWidth / 2,
                pixelBottomBound
            )
            barPath = Path().apply {
                addRoundRect(bounds, radiusArr, Path.Direction.CW)
            }
            canvas.drawPath(barPath, barPaint)
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

    private fun drawSelectedBarIcon(canvas: Canvas) {
        val barValue = bars.getOrNull(selectedBar)?.value ?: return

        val scaled = barValue / valueUpperBound
        val barTop = pixelTopBound + chartHeight * (1f - scaled)
        val pointText = barValue.toInt().toString()
        val textWidth = selectedBarTextPaint.measureText(pointText)
        val textHeight = selectedBarTextPaint.fontMetrics.let { it.descent - it.ascent }

        val backgroundWidth = textWidth + 2 * selectedBarTextPadding
        val backgroundHeight = textHeight + 2 * selectedBarTextPadding
        val backgroundCenterX = barWidth * selectedBar + barWidth / 2
        val backgroundCenterY = max(barTop - selectedBarBackgroundPadding - backgroundHeight / 2, backgroundHeight / 2)

        canvas.save()

        canvas.translate(backgroundCenterX, backgroundCenterY)

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
        canvas.drawText(
            pointText,
            bounds.left + selectedBarTextPadding,
            bounds.bottom - selectedBarTextPadding,
            selectedBarTextPaint
        )

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
            (segments downTo 1).toList().map { ViewData(it.toFloat(), it.toString()) }.let(::setBars)
            selectedBar = barCountInEdit / 2
        }
    }

    private fun onClick(x: Float, y: Float) {
        val clickedAroundBar = floor(x / barWidth).toInt()

        bars.getOrNull(clickedAroundBar)?.let {
            // Normalize bar values to max legend line value
            val scaled = it.value / valueUpperBound
            val barTop = pixelTopBound + chartHeight * (1f - scaled)

            if (y > barTop && y < pixelBottomBound) {
                selectedBar = clickedAroundBar
                invalidate()
                return
            }
        }

        selectedBar = -1
        invalidate()
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

    data class ViewData(
        val value: Float,
        val legend: String? = null
    )
}