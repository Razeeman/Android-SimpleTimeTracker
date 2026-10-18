package com.example.util.simpletimetracker.feature_views.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class AngleGradientDrawable : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var startColor: Int = Color.TRANSPARENT
    private var endColor: Int = Color.TRANSPARENT
    private var angleDegrees: Float = 0f
    private var cornerRadius: Float = 0f

    fun setGradient(
        @ColorInt startColor: Int,
        @ColorInt endColor: Int,
        angleDegrees: Float,
        cornerRadius: Float,
    ) {
        if (this.startColor == startColor &&
            this.endColor == endColor &&
            this.angleDegrees == angleDegrees &&
            this.cornerRadius == cornerRadius
        ) {
            return
        }

        this.startColor = startColor
        this.endColor = endColor
        this.angleDegrees = angleDegrees
        this.cornerRadius = cornerRadius
        updateShader()
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: Rect) {
        updateShader()
    }

    override fun draw(canvas: Canvas) {
        if (!bounds.isEmpty) {
            canvas.drawRoundRect(
                bounds.left.toFloat(),
                bounds.top.toFloat(),
                bounds.right.toFloat(),
                bounds.bottom.toFloat(),
                cornerRadius,
                cornerRadius,
                paint,
            )
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    private fun updateShader() {
        if (bounds.isEmpty) {
            paint.shader = null
            return
        }

        val radians = Math.toRadians((angleDegrees % 360f).toDouble())
        val dx = cos(radians).toFloat()
        val dy = -sin(radians).toFloat()
        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()

        // Project the bounds onto the direction to preserve the angle at any aspect ratio.
        val halfLength = (bounds.width() * abs(dx) + bounds.height() * abs(dy)) / 2f

        paint.shader = LinearGradient(
            cx - dx * halfLength,
            cy - dy * halfLength,
            cx + dx * halfLength,
            cy + dy * halfLength,
            startColor,
            endColor,
            Shader.TileMode.CLAMP,
        )
    }
}
