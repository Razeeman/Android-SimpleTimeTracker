package com.example.util.simpletimetracker.feature_dialogs.colorSelection.customView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.FloatRange
import com.example.util.simpletimetracker.core.utils.SingleTapDetector
import com.example.util.simpletimetracker.core.utils.SwipeDetector
import com.example.util.simpletimetracker.feature_dialogs.R

class ColorSelectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(
    context,
    attrs,
    defStyleAttr,
) {

    interface ColorSelectedListener {
        fun onColorSelected(saturation: Float, value: Float)
    }

    // Attrs
    @FloatRange(from = 0.0, to = 360.0)
    private var colorHue: Float = 0f
    @FloatRange(from = 0.0, to = 1.0)
    private var colorSaturation: Float = 1f
    @FloatRange(from = 0.0, to = 1.0)
    private var colorValue: Float = 1f
    private var selectedColorRadius: Int = 0
    private var selectedColorStrokeWidth: Int = 0
    private var selectedColorStrokeColor: Int = Color.WHITE
    // End of attrs

    private var listener: ColorSelectedListener? = null

    private var pixelTopBound: Float = 0f
    private var pixelBottomBound: Float = 0f
    private var pixelRightBound: Float = 0f
    private var pixelLeftBound: Float = 0f
    private var pixelWidthBound: Float = 0f
    private var pixelHeightBound: Float = 0f

    private val bounds: RectF = RectF(0f, 0f, 0f, 0f)
    private val mainPaint: Paint = Paint()
    private val gradientPaint1: Paint = Paint()
    private val gradientPaint2: Paint = Paint()
    private val selectedColorPaint: Paint = Paint()

    private val singleTapDetector = SingleTapDetector(
        context = context,
        onSingleTap = { onTouch(it) },
    )

    // TODO fix swipe in bottom sheet
    private val swipeDetector = SwipeDetector(
        context = context,
        onSlide = ::onSwipe,
        onSlideStop = ::onSwipeStop,
    )

    init {
        initArgs(context, attrs, defStyleAttr)
        initPaint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = resolveSize(0, widthMeasureSpec)
        val h = resolveSize(0, heightMeasureSpec)

        setMeasuredDimension(w, h)
        calculateDimensions(w.toFloat(), h.toFloat())
        initGradientPaint(w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        // Draw base color.
        mainPaint.color = floatArrayOf(colorHue, 1f, 1f)
            .let(Color::HSVToColor)
        canvas.drawRect(pixelLeftBound, pixelTopBound, pixelRightBound, pixelBottomBound, mainPaint)

        // Draw gradients.
        canvas.drawRect(pixelLeftBound, pixelTopBound, pixelRightBound, pixelBottomBound, gradientPaint1)
        canvas.drawRect(pixelLeftBound, pixelTopBound, pixelRightBound, pixelBottomBound, gradientPaint2)

        // Draw selected color indicator.
        val selectedColorCenterX = pixelLeftBound + pixelWidthBound * colorSaturation
        val selectedColorCenterY = pixelTopBound + pixelHeightBound * (1 - colorValue)
        bounds.set(
            selectedColorCenterX - selectedColorRadius, selectedColorCenterY - selectedColorRadius,
            selectedColorCenterX + selectedColorRadius, selectedColorCenterY + selectedColorRadius,
        )
        canvas.drawArc(
            bounds,
            0f,
            360f,
            false,
            selectedColorPaint,
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> handled = true
        }

        return handled or singleTapDetector.onTouchEvent(event) or swipeDetector.onTouchEvent(event)
    }

    fun setHue(hue: Float, saturation: Float, value: Float) {
        colorHue = hue.coerceIn(0f, 360f)
        colorSaturation = saturation.coerceIn(0f, 1f)
        colorValue = value.coerceIn(0f, 1f)
        invalidate()
    }

    fun setListener(listener: ColorSelectedListener) {
        this.listener = listener
    }

    private fun initArgs(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) {
        context
            .obtainStyledAttributes(
                attrs,
                R.styleable.ColorSelectionView, defStyleAttr, 0,
            )
            .run {
                colorHue =
                    getFloat(R.styleable.ColorSelectionView_colorSelectionHue, 0f).coerceIn(0f, 360f)
                colorSaturation =
                    getFloat(R.styleable.ColorSelectionView_colorSelectionSaturation, 1f).coerceIn(0f, 1f)
                colorValue =
                    getFloat(R.styleable.ColorSelectionView_colorSelectionValue, 1f).coerceIn(0f, 1f)
                selectedColorRadius =
                    getDimensionPixelSize(R.styleable.ColorSelectionView_colorSelectionSelectedColorRadius, 0)
                selectedColorStrokeWidth =
                    getDimensionPixelSize(R.styleable.ColorSelectionView_colorSelectionSelectedColorStrokeWidth, 0)
                selectedColorStrokeColor =
                    getColor(R.styleable.ColorSelectionView_colorSelectionSelectedColorStrokeColor, Color.WHITE)
                recycle()
            }
    }

    private fun initPaint() {
        selectedColorPaint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = selectedColorStrokeWidth.toFloat()
            color = selectedColorStrokeColor
        }
    }

    private fun initGradientPaint(w: Float, h: Float) {
        val colors = IntArray(2)
        colors[0] = Color.WHITE
        colors[1] = Color.TRANSPARENT
        LinearGradient(
            0f, 0f, w, 0f, colors, null, Shader.TileMode.CLAMP,
        ).let(gradientPaint1::setShader)

        val colors2 = IntArray(2)
        colors2[0] = Color.TRANSPARENT
        colors2[1] = Color.BLACK
        LinearGradient(
            0f, 0f, 0f, h, colors2, null, Shader.TileMode.CLAMP,
        ).let(gradientPaint2::setShader)
    }

    private fun calculateDimensions(w: Float, h: Float) {
        val selectedColorFullRadius = selectedColorRadius + selectedColorStrokeWidth / 2

        pixelLeftBound = 0f + selectedColorFullRadius
        pixelRightBound = w - selectedColorFullRadius
        pixelTopBound = 0f + selectedColorFullRadius
        pixelBottomBound = h - selectedColorFullRadius

        pixelWidthBound = pixelRightBound - pixelLeftBound
        pixelHeightBound = pixelBottomBound - pixelTopBound
    }

    private fun onTouch(event: MotionEvent) {
        val x = event.x.coerceIn(pixelLeftBound, pixelRightBound) - pixelLeftBound
        val y = event.y.coerceIn(pixelTopBound, pixelBottomBound) - pixelTopBound

        val newSaturation = (x / pixelWidthBound).coerceIn(0f, 1f)
        val newValue = (1 - y / pixelHeightBound).coerceIn(0f, 1f)

        listener?.onColorSelected(
            saturation = newSaturation,
            value = newValue,
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onSwipe(offset: Float, direction: SwipeDetector.Direction, event: MotionEvent) {
        parent.requestDisallowInterceptTouchEvent(true)
        onTouch(event)
    }

    private fun onSwipeStop() {
        parent.requestDisallowInterceptTouchEvent(false)
    }
}