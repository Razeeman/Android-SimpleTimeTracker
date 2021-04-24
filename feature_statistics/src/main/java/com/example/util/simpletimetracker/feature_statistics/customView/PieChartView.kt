package com.example.util.simpletimetracker.feature_statistics.customView

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import com.example.util.simpletimetracker.core.extension.getBitmapFromView
import com.example.util.simpletimetracker.core.extension.measureExactly
import com.example.util.simpletimetracker.core.view.IconView
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
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

    private val animator = ValueAnimator.ofFloat(0f, 1f)
    private val bounds: RectF = RectF(0f, 0f, 0f, 0f)
    private var segments: List<Arc> = emptyList()
    private var shadowColor: Int = 0x40000000
    private var segmentAnimationScale: Float = 1f
    private val segmentAnimationDuration: Long = 200L // ms
    private var shouldAnimate: Boolean = true
    private val iconView: IconView = IconView(ContextThemeWrapper(context, R.style.AppTheme))

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
        var drawable: Drawable? = null

        data.forEach { segment ->
            if (drawIcons && segment.iconId != null) {
                drawable = getIconDrawable(segment.iconId)
            }
            segmentPercent = if (valuesSum != 0L) {
                segment.value.toFloat() / valuesSum
            } else {
                1f / data.size
            }
            res.add(
                Arc(
                    color = segment.colorInt,
                    drawable = drawable,
                    arcPercent = segmentPercent
                )
            )
        }

        segments = res
        invalidate()
        if (!isInEditMode) animateSegments()
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
            -90f,
            360f * segmentAnimationScale,
            false,
            shadowPaint
        )
    }

    private fun drawSegments(canvas: Canvas, w: Float, h: Float, r: Float) {
        val segmentWidth = r - r * innerRadiusRatio
        val segmentCenterLine = r - segmentWidth / 2
        var currentSweepAngle = 0f
        var sweepAngle: Float
        segmentPaint.strokeWidth = segmentWidth

        canvas.save()
        canvas.rotate(-90f, w / 2, h / 2)

        bounds.set(
            w / 2 - segmentCenterLine, h / 2 - segmentCenterLine,
            w / 2 + segmentCenterLine, h / 2 + segmentCenterLine
        )
        segments.forEach {
            sweepAngle = it.arcPercent * 360f * segmentAnimationScale
            segmentPaint.color = it.color
            canvas.drawArc(
                bounds,
                currentSweepAngle,
                sweepAngle,
                false,
                segmentPaint
            )
            currentSweepAngle += sweepAngle
        }

        canvas.restore()
    }

    private fun drawDividers(canvas: Canvas, w: Float, h: Float, r: Float) {
        if (segments.size < 2) return

        var sweepAngle: Float

        canvas.save()
        canvas.translate(w / 2, h / 2)
        segments.forEach {
            sweepAngle = it.arcPercent * 360f * segmentAnimationScale
            canvas.drawLine(
                0f,
                -r * innerRadiusRatio + 1,
                0f,
                -r - 1,
                dividerPaint
            )
            canvas.rotate(sweepAngle)
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
        var currentSweepAngle = 0f
        var sweepAngle: Float

        val initial = canvas.save()
        canvas.translate(w / 2, h / 2)
        var center = canvas.save()

        segments.forEach {
            // circleCircumference = 2 * Math.PI * r
            // segmentRatio = it.sweepAngle / 360f
            // segmentLength = circleCircumference * segmentRatio
            sweepAngle = it.arcPercent * 360f * segmentAnimationScale
            val segmentLength = 2 * Math.PI * iconPositionFromCenter * sweepAngle / 360f
            if (segmentLength < iconSize + 2 * iconPadding) return@forEach

            rotation = currentSweepAngle + sweepAngle / 2f
            canvas.rotate(rotation)
            canvas.translate(0f, -iconPositionFromCenter)
            canvas.rotate(-rotation)
            it.drawable?.bounds = bounds
            it.drawable?.draw(canvas)

            currentSweepAngle += sweepAngle
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
                        iconId = RecordTypeIcon.Image(R.drawable.unknown)
                    )
                }.let(::setSegments)
        }
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

    private fun animateSegments() {
        if (shouldAnimate) {
            animator.duration = segmentAnimationDuration
            animator.addUpdateListener { animation ->
                segmentAnimationScale = animation.animatedValue as Float
                invalidate()
            }
            animator.start()
            shouldAnimate = false
        }
    }

    private inner class Arc(
        val color: Int,
        val drawable: Drawable? = null,
        val arcPercent: Float
    )
}