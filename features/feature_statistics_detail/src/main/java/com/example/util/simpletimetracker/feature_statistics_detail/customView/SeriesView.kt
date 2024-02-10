package com.example.util.simpletimetracker.feature_statistics_detail.customView

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_views.ColorUtils
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import java.lang.Float.max

class SeriesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(
    context,
    attrs,
    defStyleAttr
) {
    // Attrs
    private var seriesCountInPreview: Long = 0
    private var seriesMaxValueInPreview: Int = 0
    private var barDividerHeight: Int = 0
    private var barHeight: Int = 0
    private var barCornerRadius: Float = 0f
    private var barColor: Int = 0
    private var legendTextPadding: Float = 0f
    private var legendTextSize: Float = 0f
    private var legendTextColor: Int = 0
    private var valueTextSize: Float = 0f
    private var valueTextColor: Int = 0
    // End of attrs

    private val bounds: RectF = RectF(0f, 0f, 0f, 0f)
    private val textBounds: Rect = Rect(0, 0, 0, 0)
    private var radiusArr: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private var barPath: Path = Path()
    private var data: List<ViewData> = emptyList()
    private var longestBarWidth: Float = 0f
    private var maxValue: Long = 0
    private var legendTextWidth: Float = 0f
    private var legendTextHeight: Float = 0f
    private var valueTextHeight: Float = 0f
    private var barAnimationScale: Float = 1f
    private val barAnimationDuration: Long = 300L // ms
    private val backgroundPadding: Float = 2.dpToPx().toFloat()

    private val barPaint: Paint = Paint()
    private val legendTextPaint: Paint = Paint()
    private val valueTextPaint: Paint = Paint()
    private val valueBackgroundPaint: Paint = Paint()

    init {
        initArgs(context, attrs, defStyleAttr)
        initPaint()
        initEditMode()
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = (data.size.takeIf { it > 0 } ?: 1)
            .let { it * barHeight + (it - 1) * barDividerHeight }
        val w = resolveSize(0, widthMeasureSpec)
        val h = resolveSize(height, heightMeasureSpec)

        setMeasuredDimension(w, h)
    }

    @Suppress("UNUSED_VARIABLE")
    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()

        calculateDimensions(w)
        drawBars(canvas, w)
    }

    fun setData(data: List<ViewData>) {
        this.data = data.takeUnless { it.isEmpty() }
            ?: listOf(ViewData(value = 0, legendStart = "", legendEnd = ""))
        invalidate()
        requestLayout()
        if (!isInEditMode) animateBars()
    }

    fun setBarColor(color: Int) {
        barColor = color
        initPaint()
        invalidate()
    }

    private fun initArgs(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) {
        context
            .obtainStyledAttributes(
                attrs,
                R.styleable.SeriesView, defStyleAttr, 0
            )
            .run {
                seriesCountInPreview =
                    getInt(R.styleable.SeriesView_seriesBarCountInPreview, 0).toLong()
                seriesMaxValueInPreview =
                    getInt(R.styleable.SeriesView_seriesMaxValueInPreview, 0)
                barDividerHeight =
                    getDimensionPixelSize(R.styleable.SeriesView_seriesDividerHeight, 0)
                barHeight =
                    getDimensionPixelSize(R.styleable.SeriesView_seriesBarHeight, 0)
                barCornerRadius =
                    getDimensionPixelSize(R.styleable.SeriesView_seriesBarCornerRadius, 0).toFloat()
                barColor =
                    getColor(R.styleable.SeriesView_seriesBarColor, Color.BLACK)
                legendTextPadding =
                    getDimensionPixelSize(R.styleable.SeriesView_seriesLegendTextPadding, 0).toFloat()
                legendTextSize =
                    getDimensionPixelSize(R.styleable.SeriesView_seriesLegendTextSize, 14).toFloat()
                legendTextColor =
                    getColor(R.styleable.SeriesView_seriesLegendTextColor, Color.BLACK)
                valueTextSize =
                    getDimensionPixelSize(R.styleable.SeriesView_seriesValueTextSize, 14).toFloat()
                valueTextColor =
                    getColor(R.styleable.SeriesView_seriesValueTextColor, Color.BLACK)
                recycle()
            }
    }

    private fun initPaint() {
        barPaint.apply {
            isAntiAlias = true
            color = barColor
            style = Paint.Style.FILL
        }
        legendTextPaint.apply {
            isAntiAlias = true
            color = legendTextColor
            textSize = legendTextSize
        }
        valueTextPaint.apply {
            isAntiAlias = true
            color = valueTextColor
            textSize = valueTextSize
        }
        valueBackgroundPaint.apply {
            isAntiAlias = true
            color = ColorUtils.darkenColor(barColor)
            style = Paint.Style.FILL
        }
    }

    private fun calculateDimensions(w: Float) {
        maxValue = data.maxOfOrNull(ViewData::value).takeUnless { it == 0L } ?: 1

        // Legends
        val legends = data.map { listOf(it.legendStart, it.legendEnd) }
            .flatten()
            .filter { it.isNotEmpty() }

        // Legends text size
        val longestLegendValue: String = legends.maxByOrNull { it.length }.orEmpty()
        legendTextWidth = legendTextPaint.measureText(longestLegendValue)
        legendTextPaint.getTextBounds(longestLegendValue, 0, longestLegendValue.length, textBounds)
        legendTextHeight = textBounds.height().toFloat()

        // Value text size
        valueTextPaint.getTextBounds("0", 0, 1, textBounds)
        valueTextHeight = textBounds.height().toFloat()

        longestBarWidth = w - 4 * legendTextPadding - 2 * legendTextWidth
    }

    private fun drawBars(canvas: Canvas, w: Float) {
        radiusArr = floatArrayOf(
            barCornerRadius, barCornerRadius,
            barCornerRadius, barCornerRadius,
            barCornerRadius, barCornerRadius,
            barCornerRadius, barCornerRadius,
        )

        canvas.save()

        data.forEach { bar ->
            val text = bar.value.toString()
            val textWidth: Float = valueTextPaint.measureText(text)

            /************
             * Draw bar *
             ************/
            val scaled: Float = ((bar.value / maxValue.toFloat()) * longestBarWidth * barAnimationScale)
                .coerceAtLeast(max(barHeight.toFloat(), textWidth + 2 * backgroundPadding))
            bounds.set(
                w / 2f - scaled / 2f,
                0f,
                w / 2f + scaled / 2f,
                barHeight.toFloat(),
            )
            barPath = Path().apply {
                addRoundRect(bounds, radiusArr, Path.Direction.CW)
            }
            canvas.drawPath(barPath, barPaint)

            /*************
             * Draw text *
             *************/
            bounds.set(
                w / 2f - textWidth / 2f - 2 * backgroundPadding,
                barHeight / 2f - valueTextHeight / 2f - 2 * backgroundPadding,
                w / 2f + textWidth / 2f + 2 * backgroundPadding,
                barHeight / 2f + valueTextHeight / 2f + 2 * backgroundPadding,
            )
            barPath = Path().apply {
                addRoundRect(bounds, radiusArr, Path.Direction.CW)
            }
            canvas.drawPath(barPath, valueBackgroundPaint)

            canvas.drawText(
                text,
                w / 2f - textWidth / 2f,
                barHeight / 2f + valueTextHeight / 2f,
                valueTextPaint
            )

            /****************
             * Draw legends *
             ****************/
            legendTextPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                bar.legendStart,
                w / 2f - scaled / 2f - legendTextPadding,
                barHeight / 2f + legendTextHeight / 2f,
                legendTextPaint,
            )
            legendTextPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(
                bar.legendEnd,
                w / 2f + scaled / 2f + legendTextPadding,
                barHeight / 2f + legendTextHeight / 2f,
                legendTextPaint,
            )

            // Shift canvas for next bar
            canvas.translate(0f, barHeight.toFloat() + barDividerHeight.toFloat())
        }

        canvas.restore()
    }

    private fun initEditMode() {
        if (isInEditMode) {
            val bars = seriesCountInPreview.takeIf { it != 0L } ?: 5
            val maxValue = seriesMaxValueInPreview.takeIf { it != 0 } ?: 10
            (bars - 1 downTo 0)
                .toList()
                .map {
                    ViewData(
                        value = maxValue / bars * it,
                        legendStart = "22.05.2022",
                        legendEnd = "03.12.2022",
                    )
                }
                .let(::setData)
        }
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
        val value: Long,
        val legendStart: String,
        val legendEnd: String,
    )
}