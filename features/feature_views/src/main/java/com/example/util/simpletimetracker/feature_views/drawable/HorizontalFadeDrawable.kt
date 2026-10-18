package com.example.util.simpletimetracker.feature_views.drawable

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import androidx.appcompat.graphics.drawable.DrawableWrapperCompat
import android.graphics.Color as AndroidColor

class HorizontalFadeDrawable(drawable: Drawable) : DrawableWrapperCompat(drawable) {

    private val fadePaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        fadePaint.shader = if (bounds.isEmpty) {
            null
        } else {
            LinearGradient(
                bounds.left.toFloat(), bounds.top.toFloat(),
                bounds.right.toFloat(), bounds.top.toFloat(),
                AndroidColor.TRANSPARENT, AndroidColor.WHITE,
                Shader.TileMode.CLAMP,
            )
        }
    }

    override fun draw(canvas: Canvas) {
        if (bounds.isEmpty) return

        // Isolate the mask so it only fades this preview.
        val saveCount = canvas.saveLayer(
            bounds.left.toFloat(), bounds.top.toFloat(),
            bounds.right.toFloat(), bounds.bottom.toFloat(),
            null,
        )
        try {
            super.draw(canvas)
            canvas.drawRect(bounds, fadePaint)
        } finally {
            canvas.restoreToCount(saveCount)
        }
    }
}