package com.example.util.simpletimetracker.feature_statistics_detail.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.util.simpletimetracker.feature_statistics_detail.R

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr
) {

    private val bounds: RectF = RectF(0f, 0f, 0f, 0f)
    private var dividerWidth: Int = 0
    private var barCornerRadius: Float = 0f
    private var barColor: Int = 0
    private var bars: List<Float> = emptyList()
    private var barCount: Int = 0

    private val barPaint: Paint = Paint()

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
        val barWidth = width / bars.size.toFloat()

        drawBars(canvas, w, h, barWidth)
    }

    fun setBars(data: List<Long>) {
        if (data.isEmpty()) return

        val res = mutableListOf<Float>()
        val maxValue = data.max()
            ?.takeIf { it != 0L }?.toFloat()
            ?: 1F

        data.forEach { value ->
            res.add(value / maxValue)
        }

        bars = res
        invalidate()
    }

    fun setBarColor(color: Int) {
        barColor = color
        initPaint()
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
                barCount =
                    getInt(R.styleable.BarChartView_barCount, 0)
                dividerWidth =
                    getDimensionPixelSize(R.styleable.BarChartView_dividerWidth, 0)
                barCornerRadius =
                    getDimensionPixelSize(R.styleable.BarChartView_barCornerRadius, 0).toFloat()
                barColor =
                    getColor(R.styleable.BarChartView_barColor, Color.BLACK)
                recycle()
            }
    }

    private fun initPaint() {
        barPaint.apply {
            isAntiAlias = true
            color = barColor
            style = Paint.Style.FILL
        }
    }

    private fun drawBars(canvas: Canvas, w: Float, h: Float, barWidth: Float) {
        canvas.save()

        bounds.set(
            0f, 0f,
            barWidth, h
        )
        bars.forEach {
            bounds.set(
                0f + dividerWidth / 2, h * (1f - it),
                barWidth - dividerWidth / 2, h
            )
            canvas.drawRoundRect(
                bounds, barCornerRadius, barCornerRadius, barPaint
            )
            canvas.translate(barWidth, 0f)
        }

        canvas.restore()
    }

    private fun initEditMode() {
        if (isInEditMode) {
            val segments = barCount.takeIf { it != 0 } ?: 5
            (segments downTo 1L).toList().let(::setBars)
        }
    }
}