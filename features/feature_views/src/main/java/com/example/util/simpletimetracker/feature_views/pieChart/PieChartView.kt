package com.example.util.simpletimetracker.feature_views.pieChart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import androidx.annotation.FloatRange
import com.example.util.simpletimetracker.feature_views.IconView
import com.example.util.simpletimetracker.feature_views.R
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

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
    private var drawParticles: Boolean = false
    // End of attrs

    private val segmentPaint: Paint = Paint()
    private val shadowPaint: Paint = Paint()
    private val dividerPaint: Paint = Paint()
    private val particlePaint: Paint = Paint()

    private var attachedListener: ((Boolean) -> Unit)? = null
    private val animator = ValueAnimator.ofFloat(0f, 1f)
    private val bounds: RectF = RectF(0f, 0f, 0f, 0f)
    private val layerBounds: RectF = RectF(0f, 0f, 0f, 0f)
    private val particleBounds: RectF = RectF(0f, 0f, 0f, 0f)
    private var segments: List<Arc> = emptyList()
    private var shadowColor: Int = 0x40000000
    private var segmentAnimationScale: Float = 1f
    private var shouldAnimateOpen: Boolean = true
    private var animateParticles: Boolean = false
    private var animateParticlesPaused: Boolean = false
    private val iconView: IconView = IconView(ContextThemeWrapper(context, R.style.AppTheme))

    init {
        initArgs(context, attrs, defStyleAttr)
        initPaint()
        initEditMode()
        setLayerType(LAYER_TYPE_HARDWARE, null)
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachedListener?.invoke(true)
    }

    override fun onDetachedFromWindow() {
        attachedListener?.invoke(false)
        super.onDetachedFromWindow()
    }

    fun setAnimateParticles(animate: Boolean) {
        animateParticles = animate
        if (animateParticles) invalidate()
    }

    fun setAttachedListener(listener: (Boolean) -> Unit) {
        attachedListener = listener
    }

    fun setSegments(
        data: List<PiePortion>,
        animateOpen: Boolean,
    ) {
        val res = mutableListOf<Arc>()
        val valuesSum = data.map(PiePortion::value).sum()
        var segmentPercent: Float
        var drawable: Bitmap? = null

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
        if (!isInEditMode && animateOpen) animateSegmentsAppearing()
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
                drawParticles =
                    getBoolean(R.styleable.PieChartView_drawParticles, false)
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
        particlePaint.apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
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

    @Suppress("DEPRECATION")
    private fun drawSegments(canvas: Canvas, w: Float, h: Float, r: Float) {
        val segmentWidth = r - r * innerRadiusRatio
        val segmentCenterLine = r - segmentWidth / 2f
        var currentSweepAngle = -90f
        var sweepAngle: Float
        segmentPaint.strokeWidth = segmentWidth

        canvas.save()
        canvas.translate(w / 2f, h / 2f)
        bounds.set(
            -segmentCenterLine, -segmentCenterLine,
            segmentCenterLine, +segmentCenterLine
        )
        layerBounds.set(-r, -r, r, r)
        segments.forEach {
            canvas.saveLayerAlpha(layerBounds, 0xFF, Canvas.ALL_SAVE_FLAG)
            sweepAngle = it.arcPercent * 360f * segmentAnimationScale
            segmentPaint.color = it.color
            canvas.drawArc(
                bounds,
                currentSweepAngle,
                sweepAngle,
                false,
                segmentPaint
            )
            drawParticles(
                segment = it,
                canvas = canvas,
                currentSweepAngle = currentSweepAngle,
                sweepAngle = sweepAngle,
                r = r,
            )
            currentSweepAngle += sweepAngle
            canvas.restore()
        }
        canvas.restore()
    }

    private fun drawParticles(
        segment: Arc,
        canvas: Canvas,
        currentSweepAngle: Float,
        sweepAngle: Float,
        r: Float,
    ) {
        if (!drawParticles) return
        if (segment.drawable == null) return
        if (sweepAngle < PARTICLES_ANGLE_CUTOFF) return

        val now = System.currentTimeMillis()
        if (animateParticlesStartTime == -1L) {
            animateParticlesStartTime = now
        }
        if (animateParticles) {
            if (animateParticlesPaused) {
                animateParticlesStartTime = now - animateParticlesFrameTime
                animateParticlesPaused = false
            }
            animateParticlesFrameTime = now - animateParticlesStartTime
        } else {
            animateParticlesPaused = true
        }
        val time = animateParticlesFrameTime / PARTICLES_CYCLE

        val iconSizeHalfSize = calculateIconSize(r) / 2f
        particleBounds.set(
            -iconSizeHalfSize, -iconSizeHalfSize,
            iconSizeHalfSize, iconSizeHalfSize
        )

        val fromAngle = floor(currentSweepAngle / PARTICLES_ANGLE_STEP).toInt()
        val toAngle = ceil((currentSweepAngle + sweepAngle) / PARTICLES_ANGLE_STEP).toInt()

        for (i in fromAngle..toAngle) {
            val angle = i * PARTICLES_ANGLE_STEP

            val startTimeVariation = PARTICLES_CYCLE * 100 * pseudoRandom(angle)
            val speedVariation = PARTICLES_BASE_SPEED + PARTICLES_SPEED_VARIATION * pseudoRandom(angle)
            val distanceVariation = ((time + startTimeVariation) * speedVariation) % 1.0

            val innerParticleSpanDistance = r * innerRadiusRatio - iconSizeHalfSize
            val outerParticleSpanDistance = r + iconSizeHalfSize
            val particleDistance = interpolate(
                0f, outerParticleSpanDistance, distanceVariation
            )

            if (
                particleDistance > innerParticleSpanDistance &&
                particleDistance < outerParticleSpanDistance
            ) {
                val alpha = PARTICLES_BASE_ALPHA + PARTICLES_ALPHA_VARIATION * pseudoRandom(angle + 1)
                val scale = PARTICLES_BASE_SCALE + PARTICLES_SCALE_VARIATION * pseudoRandom(angle + 2)
                val x = particleDistance * cos(Math.toRadians(angle))
                val y = particleDistance * sin(Math.toRadians(angle))
                particlePaint.alpha = (0xFF * alpha).toInt()

                canvas.save()
                canvas.translate(x.toFloat(), y.toFloat())
                canvas.scale(scale.toFloat(), scale.toFloat())
                canvas.drawBitmap(segment.drawable, null, particleBounds, particlePaint)
                canvas.restore()
            }
        }

        if (animateParticles) invalidate()
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
            it.drawable ?: return@forEach

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
            canvas.drawBitmap(it.drawable, null, bounds, null)

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
                }.let {
                    setSegments(
                        data = it,
                        animateOpen = false,
                    )
                }
        }
    }

    private fun getIconDrawable(iconId: RecordTypeIcon): Bitmap {
        return iconView
            .apply {
                itemIcon = iconId
                measureExactly(iconMaxSize)
            }
            .getBitmapFromView()
    }

    private fun animateSegmentsAppearing() {
        if (shouldAnimateOpen) {
            animator.duration = SEGMENT_OPEN_ANIMATION_DURATION_MS
            animator.addUpdateListener { animation ->
                segmentAnimationScale = animation.animatedValue as Float
                invalidate()
            }
            animator.start()
            shouldAnimateOpen = false
        }
    }

    @FloatRange(from = 0.0, to = 1.0)
    private fun pseudoRandom(seed: Double): Double {
        return sin(seed) / 2.0 + 0.5
    }

    @Suppress("SameParameterValue")
    private fun interpolate(a: Float, b: Float, f: Double): Double {
        return a + f * (b - a)
    }

    private inner class Arc(
        val color: Int,
        val drawable: Bitmap? = null,
        val arcPercent: Float
    )

    companion object {
        private var animateParticlesStartTime: Long = -1
        private var animateParticlesFrameTime: Long = -1

        private const val SEGMENT_OPEN_ANIMATION_DURATION_MS: Long = 250L

        private const val PARTICLES_ANGLE_STEP: Double = 5.0
        private const val PARTICLES_ANGLE_CUTOFF: Double = 7.0
        private const val PARTICLES_CYCLE: Double = 30000.0
        private const val PARTICLES_BASE_SPEED: Double = 1.0
        private const val PARTICLES_SPEED_VARIATION: Double = 1.0
        private const val PARTICLES_BASE_ALPHA: Double = 0.35
        private const val PARTICLES_ALPHA_VARIATION: Double = -0.25
        private const val PARTICLES_BASE_SCALE: Double = 0.7
        private const val PARTICLES_SCALE_VARIATION: Double = -0.25
    }
}