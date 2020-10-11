package com.example.util.simpletimetracker.feature_statistics.customView

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.example.util.simpletimetracker.feature_statistics.R
import kotlin.math.max
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr
) {

    // Attrs
    private var innerRadiusRatio: Float = 0.0f
    private var dividerWidth: Int = 0
    private var dividerColor: Int = Color.WHITE
    private var iconPadding: Int = 0
    private var iconMaxSize: Int = 0
    private var segmentCount: Int = 0
    private var drawIcons: Boolean = false
    // End of attrs

    private val segmentPaint: Paint = Paint()
    private val shadowPaint: Paint = Paint()
    private val dividerPaint: Paint = Paint()

    private val bounds: RectF = RectF(0f, 0f, 0f, 0f)
    private var segments: List<Arc> = emptyList()
    private var shadowColor: Int = 0x40000000

    init {
        initArgs(context, attrs, defStyleAttr)
        initPaint()
        initEditMode()
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = resolveSize(0, widthMeasureSpec)
        val h = resolveSize(w, heightMeasureSpec)

        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null || segments.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val r = height / 2f - 12

        drawShadow(canvas, w, h, r)
        drawSegments(canvas, w, h, r)
        drawDividers(canvas, w, h, r)
        drawIcons(canvas, w, h, r)
    }

    fun setSegments(data: List<PiePortion>) {
        val res = mutableListOf<Arc>()
        val valuesSum = data.map(PiePortion::value).sum()
        var segmentPercent: Float
        var startAngle = 0f
        var sweepAngle: Float
        var drawable: Drawable? = null

        data.forEach { segment ->
            if (drawIcons && segment.iconId != null) {
                drawable = getIconDrawable(segment.iconId)
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

    private fun initArgs(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) {
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
                dividerColor =
                    getColor(R.styleable.PieChartView_dividerColor, Color.WHITE)
                iconPadding =
                    getDimensionPixelSize(R.styleable.PieChartView_iconPadding, 0)
                iconMaxSize =
                    getDimensionPixelSize(R.styleable.PieChartView_iconMaxSize, 0)
                segmentCount =
                    getInt(R.styleable.PieChartView_segmentCount, 0)
                drawIcons =
                    getBoolean(R.styleable.PieChartView_drawIcons, false)
                recycle()
            }
    }

    private fun initPaint() {
        segmentPaint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
        shadowPaint.apply {
            isAntiAlias = true
            color = shadowColor
            maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
            style = Paint.Style.STROKE
        }
        dividerPaint.apply {
            isAntiAlias = true
            color = dividerColor
            strokeWidth = dividerWidth.toFloat()
            style = Paint.Style.STROKE
        }
    }

    private fun drawShadow(canvas: Canvas, w: Float, h: Float, r: Float) {
        val segmentWidth = r - r * innerRadiusRatio
        val segmentCenterLine = r - segmentWidth / 2
        shadowPaint.strokeWidth = segmentWidth

        bounds.set(
            w / 2 - segmentCenterLine, h / 2 - segmentCenterLine,
            w / 2 + segmentCenterLine, h / 2 + segmentCenterLine
        )
        canvas.drawArc(
            bounds,
            0f,
            360f,
            false,
            shadowPaint
        )
    }

    private fun drawSegments(canvas: Canvas, w: Float, h: Float, r: Float) {
        // TODO rounded corners?
        val segmentWidth = r - r * innerRadiusRatio
        val segmentCenterLine = r - segmentWidth / 2
        segmentPaint.strokeWidth = segmentWidth

        canvas.save()
        canvas.rotate(-90f, w / 2, h / 2)

        bounds.set(
            w / 2 - segmentCenterLine, h / 2 - segmentCenterLine,
            w / 2 + segmentCenterLine, h / 2 + segmentCenterLine
        )
        segments.forEach {
            segmentPaint.color = it.color
            canvas.drawArc(
                bounds,
                it.startAngle,
                it.sweepAngle,
                false,
                segmentPaint
            )
        }

        canvas.restore()
    }

    private fun drawDividers(canvas: Canvas, w: Float, h: Float, r: Float) {
        if (segments.size < 2) return

        canvas.save()
        canvas.translate(w / 2, h / 2)
        segments.forEach {
            canvas.drawLine(
                0f,
                -r * innerRadiusRatio + 1,
                0f,
                -r - 1,
                dividerPaint
            )
            canvas.rotate(it.sweepAngle)
        }
        canvas.restore()
    }

    private fun drawIcons(canvas: Canvas, w: Float, h: Float, r: Float) {
        if (!drawIcons || segments.isEmpty()) return
        val iconSize = calculateIconSize(r)
        val bounds = Rect(
            -iconSize / 2, -iconSize / 2,
            iconSize / 2, iconSize / 2
        )
        var rotation: Float
        val iconPositionFromCenter = r - r * (1 - innerRadiusRatio) / 2

        val initial = canvas.save()
        canvas.translate(w / 2, h / 2)
        var center = canvas.save()

        segments.forEach {
            // circleCircumference = 2 * Math.PI * r
            // segmentRatio = it.sweepAngle / 360f
            // segmentLength = circleCircumference * segmentRatio
            val segmentLength = 2 * Math.PI * iconPositionFromCenter * it.sweepAngle / 360f
            if (segmentLength < iconSize + 2 * iconPadding) return@forEach

            rotation = it.startAngle + it.sweepAngle / 2f
            canvas.rotate(rotation)
            canvas.translate(0f, -iconPositionFromCenter)
            canvas.rotate(-rotation)
            it.drawable?.bounds = bounds
            it.drawable?.draw(canvas)

            canvas.restoreToCount(center)
            center = canvas.save()
        }

        canvas.restoreToCount(initial)
    }

    private fun calculateIconSize(r: Float): Int {
        val availableIconSize = max(0, (r - r * innerRadiusRatio - 2 * iconPadding).toInt())

        return if (iconMaxSize == 0) {
            availableIconSize
        } else {
            min(iconMaxSize, availableIconSize)
        }
    }

    private fun initEditMode() {
        val segments = segmentCount.takeIf { it != 0 } ?: 5
        if (isInEditMode) {
            (segments downTo 1)
                .map {
                    PiePortion(
                        value = it.toLong(),
                        colorInt = Color.BLACK,
                        iconId = R.drawable.unknown
                    )
                }.let(::setSegments)
        }
    }

    private fun getIconDrawable(iconId: Int): Drawable? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            (AppCompatResources.getDrawable(context, iconId) as? BitmapDrawable)?.apply {
                colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            }
        } else {
            VectorDrawableCompat.create(resources, iconId, context.theme)?.apply {
                setTintList(ColorStateList.valueOf(Color.WHITE))
            }
        }
    }

    private inner class Arc(
        val color: Int,
        val drawable: Drawable? = null,
        val startAngle: Float,
        val sweepAngle: Float
    )
}