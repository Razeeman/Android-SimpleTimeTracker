package com.example.util.simpletimetracker.feature_statistics.customView

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.example.util.simpletimetracker.feature_statistics.R

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr
) {

    private val bounds: RectF = RectF(0f, 0f, 0f, 0f)
    private val paint: Paint = Paint()
    private var innerRadiusRatio: Float = 0.0f
    private var dividerWidth: Int = 0
    private var segments: List<Arc> = emptyList()
    private var segmentCount: Int = 0
    private var drawIcons: Boolean = false

    init {
        context
            .obtainStyledAttributes(
                attrs,
                R.styleable.PieChartView, defStyleAttr, 0
            )
            .run {
                innerRadiusRatio =
                    getFloat(R.styleable.PieChartView_innerRadiusRatio, 0f)
                dividerWidth =
                    getDimensionPixelSize(R.styleable.PieChartView_dividerWidth, 0)
                segmentCount =
                    getInt(R.styleable.PieChartView_segmentCount, 0)
                drawIcons =
                    getBoolean(R.styleable.PieChartView_drawIcons, false)
                recycle()
            }

        if (segmentCount != 0) {
            val segmentPercent: Float = 360f / segmentCount
            var drawable: VectorDrawableCompat? = null
            if (drawIcons) {
                drawable = VectorDrawableCompat.create(
                    context.resources, R.drawable.ic_unknown, null
                )
                drawable?.setTintList(ColorStateList.valueOf(Color.WHITE))
            }
            segments = (0..segmentCount).map {
                Arc(
                    color = Color.BLACK,
                    drawable = drawable,
                    startAngle = it * segmentPercent,
                    sweepAngle = segmentPercent
                )
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // TODO remove logs
        Log.d("PieChart", "onMeasure w: " + MeasureSpec.toString(widthMeasureSpec))
        Log.d("PieChart", "onMeasure h: " + MeasureSpec.toString(heightMeasureSpec))
        Log.d("PieChart", "onMeasure suggestedMinimumWidth: $suggestedMinimumWidth")
        Log.d("PieChart", "onMeasure suggestedMinimumHeight: $suggestedMinimumHeight")

        val w = resolveSize(0, widthMeasureSpec)
        val h = resolveSize(w, heightMeasureSpec)

        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null || segments.isEmpty()) return

        val w = width.toFloat()
        val r = height / 2f

        drawSegments(canvas, w, r)
        drawDividers(canvas, w, r)
        drawInnerCircle(canvas, w, r)
        drawIcons(canvas, w, r)
    }

    private fun drawSegments(canvas: Canvas, w: Float, r: Float) {
        canvas.save()
        canvas.rotate(-90f, w / 2, r)

        bounds.set(w / 2 - r, 0f, w / 2 + r, 2 * r)
        segments.forEach {
            paint.color = it.color
            canvas.drawArc(
                bounds,
                it.startAngle,
                it.sweepAngle,
                true,
                paint
            )
        }

        canvas.restore()
    }

    private fun drawDividers(canvas: Canvas, w: Float, r: Float) {
        if (segments.size < 2) return
        canvas.save()

        paint.color = Color.WHITE
        paint.strokeWidth = dividerWidth.toFloat()
        segments.forEach {
            canvas.drawLine(
                w / 2,
                r,
                w / 2,
                0f,
                paint
            )
            canvas.rotate(it.sweepAngle, w / 2, r)
        }

        canvas.restore()
    }

    private fun drawIcons(canvas: Canvas, w: Float, r: Float) {
        if (!drawIcons || segments.isEmpty()) return
        val iconWidth = 60
        val iconPaddingTop = 10
        var rotation: Float
        val bounds = Rect(-iconWidth / 2, -iconWidth / 2, iconWidth / 2, iconWidth / 2)

        val initial = canvas.save()
        canvas.translate(w / 2, r)
        var center = canvas.save()

        segments.forEach {
            rotation = it.startAngle + it.sweepAngle / 2f
            canvas.rotate(rotation)
            canvas.translate(0f, -r + iconWidth / 2 + iconPaddingTop)
            canvas.rotate(-rotation)
            it.drawable?.bounds = bounds
            it.drawable?.draw(canvas)

            canvas.restoreToCount(center)
            center = canvas.save()
        }

        canvas.restoreToCount(initial)
    }

    private fun drawInnerCircle(canvas: Canvas, w: Float, r: Float) {
        paint.color = Color.WHITE
        canvas.drawCircle(w / 2f, r, r * innerRadiusRatio, paint)
    }

    fun setSegments(data: List<PiePortion>) {
        val res = mutableListOf<Arc>()
        val valuesSum = data.map(PiePortion::value).sum()
        var segmentPercent: Float
        var startAngle = 0f
        var sweepAngle: Float
        var drawable: VectorDrawableCompat? = null

        data.forEach { segment ->
            if (drawIcons && segment.iconId != null) {
                drawable = VectorDrawableCompat.create(
                    context.resources, segment.iconId, null
                )
                drawable?.setTintList(ColorStateList.valueOf(Color.WHITE))
            }
            segmentPercent = segment.value.toFloat() / valuesSum
            sweepAngle = 360 * segmentPercent
            res.add(
                Arc(
                    color = segment.colorInt,
                    drawable = drawable,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle
                )
            )
            startAngle += sweepAngle
        }

        segments = res
        invalidate()
    }

    inner class Arc(
        val color: Int,
        val drawable: VectorDrawableCompat? = null,
        val startAngle: Float,
        val sweepAngle: Float
    )
}