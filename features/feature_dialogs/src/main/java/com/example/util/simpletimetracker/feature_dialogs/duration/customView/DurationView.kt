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
    private var legendTextColor: Int = 0
    private var legendTextSize: Float = 0f
    private var legendPadding: Float = 0f
    // End of attrs

    private var data: ViewData = ViewData()
    private val textPaint: Paint = Paint()
    private val legendTextPaint: Paint = Paint()
    private var textStartHorizontal: Float = 0f
    private var textStartVertical: Float = 0f
    private val bounds: Rect = Rect()

    private val hourString: String by lazy { context.getString(R.string.time_hour) }
    private val minuteString: String by lazy { context.getString(R.string.time_minute) }
    private val secondString: String by lazy { context.getString(R.string.time_second) }

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
                legendTextColor = getColor(
                    R.styleable.DurationView_durationLegendTextColor, Color.BLACK
                )
                legendTextSize = getDimensionPixelSize(
                    R.styleable.DurationView_durationLegendTextSize, 14
                ).toFloat()
                legendPadding = getDimensionPixelSize(
                    R.styleable.DurationView_durationLegendPadding, 0
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
            color = legendTextColor
            textSize = legendTextSize
        }
    }

    private fun calculateDimensions(w: Float, h: Float) {
        val legendsTextWidth =
            listOf(hourString, minuteString, secondString).map(legendTextPaint::measureText).sum()
        val desiredSegmentWidth =
            min((w - legendsTextWidth - 2 * legendPadding) / 3, h)
        textStartHorizontal =
            (w - desiredSegmentWidth * 3 - legendsTextWidth - 2 * legendPadding) / 2

        setTextSizeForWidth(textPaint, desiredSegmentWidth)
        textPaint.getTextBounds("0", 0, 1, bounds)
        val textHeight = bounds.height()
        textStartVertical = textHeight + (h - textHeight) / 2
    }

    private fun drawText(canvas: Canvas, w: Float, h: Float) {
        fun format(value: Int): String = min(value, 99).toString().padStart(2, '0')

        // Center text
        canvas.translate(textStartHorizontal, textStartVertical)

        var text = format(data.hours)
        canvas.drawText(format(data.hours), 0f, 0f, textPaint)
        canvas.translate(textPaint.measureText(text), 0f)
        canvas.drawText(hourString, 0f, 0f, legendTextPaint)
        canvas.translate(legendTextPaint.measureText(hourString) + legendPadding, 0f)

        text = format(data.minutes)
        canvas.drawText(format(data.minutes), 0f, 0f, textPaint)
        canvas.translate(textPaint.measureText(text), 0f)
        canvas.drawText(minuteString, 0f, 0f, legendTextPaint)
        canvas.translate(legendTextPaint.measureText(minuteString) + legendPadding, 0f)

        text = format(data.hours)
        canvas.drawText(format(data.seconds), 0f, 0f, textPaint)
        canvas.translate(textPaint.measureText(text), 0f)
        canvas.drawText(secondString, 0f, 0f, legendTextPaint)
        canvas.translate(legendTextPaint.measureText(secondString), 0f)
    }

    private fun setTextSizeForWidth(paint: Paint, desiredWidth: Float) {
        val text = "00"
        val testTextSize = 48f
        paint.textSize = testTextSize
        val width = paint.measureText(text)

        val desiredTextSize = testTextSize * desiredWidth / width
        paint.textSize = desiredTextSize
    }

    data class ViewData(
        val hours: Int = 0,
        val minutes: Int = 0,
        val seconds: Int = 0
    )
}