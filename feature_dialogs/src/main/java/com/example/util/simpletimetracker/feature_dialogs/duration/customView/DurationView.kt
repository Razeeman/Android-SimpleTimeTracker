package com.example.util.simpletimetracker.feature_dialogs.duration.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.example.util.simpletimetracker.feature_dialogs.R
import kotlin.math.min

class DurationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr
) {

    // Attrs
    private var textColor: Int = 0
    // End of attrs

    private var data: ViewData = ViewData()
    private val textPaint: Paint = Paint()
    private val legendTextPaint: Paint = Paint()
    private var legendTextSize: Float = 0f
    private val bounds: Rect = Rect()

    init {
        initArgs(context, attrs, defStyleAttr)
        initPaint()
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
        drawText(canvas, w, h)
    }

    fun setData(data: ViewData) {
        this.data = data
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
                R.styleable.DurationView, defStyleAttr, 0
            )
            .run {
                textColor = getColor(
                    R.styleable.DurationView_durationTextColor, Color.BLACK
                )
                legendTextSize = getDimensionPixelSize(
                    R.styleable.DurationView_durationLegendTextSize, 14
                ).toFloat()

                recycle()
            }
    }

    private fun initPaint() {
        textPaint.apply {
            isAntiAlias = true
            color = textColor
        }
        legendTextPaint.apply {
            isAntiAlias = true
            color = textColor
            textSize = legendTextSize
        }
    }

    private fun calculateDimensions(w: Float, h: Float) {
        legendTextPaint.getTextBounds("m", 0, 1, bounds)
        val legendTextWidth = bounds.width()
        val desiredSegmentWidth = min(w / 3 - legendTextWidth, h)
        setTextSizeForWidth(textPaint, desiredSegmentWidth, "00")
    }

    private fun drawText(canvas: Canvas, w: Float, h: Float) {
        fun format(value: Int): String = min(value, 99).toString().padStart(2, '0')

        var text = format(data.hours)
        canvas.drawText(format(data.hours), 0f, h, textPaint)
        canvas.translate(textPaint.measureText(text), 0f)
        canvas.drawText("h", 0f, h, legendTextPaint)
        canvas.translate(legendTextPaint.measureText("h"), 0f)

        text = format(data.minutes)
        canvas.drawText(format(data.minutes), 0f, h, textPaint)
        canvas.translate(textPaint.measureText(text), 0f)
        canvas.drawText("m", 0f, h, legendTextPaint)
        canvas.translate(legendTextPaint.measureText("m"), 0f)

        text = format(data.hours)
        canvas.drawText(format(data.seconds), 0f, h, textPaint)
        canvas.translate(textPaint.measureText(text), 0f)
        canvas.drawText("s", 0f, h, legendTextPaint)
        canvas.translate(legendTextPaint.measureText("s"), 0f)
    }

    private fun setTextSizeForWidth(paint: Paint, desiredWidth: Float, text: String) {
        val testTextSize = 48f
        paint.textSize = testTextSize

        paint.getTextBounds(text, 0, text.length, bounds)

        val desiredTextSize = testTextSize * desiredWidth / bounds.width()
        paint.textSize = desiredTextSize
    }

    data class ViewData(
        val hours: Int = 0,
        val minutes: Int = 0,
        val seconds: Int = 0
    )
}