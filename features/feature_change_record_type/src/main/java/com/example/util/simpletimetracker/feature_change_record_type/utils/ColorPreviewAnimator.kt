package com.example.util.simpletimetracker.feature_change_record_type.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import androidx.core.animation.doOnEnd
import com.example.util.simpletimetracker.feature_views.viewData.ColorStyle

class ColorPreviewAnimator(
    private val onUpdate: (ColorStyle) -> Unit,
) {

    private var animator: ValueAnimator? = null
    private var targetColor: ColorStyle? = null
    private val colorEvaluator = ArgbEvaluator()

    fun animate(from: ColorStyle, to: ColorStyle) {
        val isGradient = from is ColorStyle.LinearGradient || to is ColorStyle.LinearGradient
        val fromGradient = from.asGradient()
        val toGradient = to.asGradient()
        // Keep the requested target separate from the displayed, animated color to detect unchanged color requests.
        val previousTarget = targetColor?.asGradient()
        cancel()
        targetColor = to

        val requestedColorsHaveNotChanged =
            previousTarget?.start == toGradient.start && previousTarget.end == toGradient.end
        val displayedColorsAlreadyApplied =
            fromGradient.start == toGradient.start && fromGradient.end == toGradient.end
        if (requestedColorsHaveNotChanged || displayedColorsAlreadyApplied) {
            onUpdate(to)
            return
        }

        animator = ValueAnimator.ofFloat(0f, 1f)
        animator?.duration = ANIMATION_DURATION_MS
        animator?.addUpdateListener {
            val fraction = it.animatedFraction
            val start = colorEvaluator
                .evaluate(fraction, fromGradient.start, toGradient.start) as? Int
                ?: return@addUpdateListener
            val end = colorEvaluator
                .evaluate(fraction, fromGradient.end, toGradient.end) as? Int
                ?: return@addUpdateListener
            val frameColor = if (isGradient) {
                ColorStyle.LinearGradient(
                    start = start,
                    end = end,
                    // Preserve the existing angle when transitioning to a solid color to avoid a sudden rotation.
                    angleDegrees = (targetColor as? ColorStyle.LinearGradient)?.angleDegrees
                        ?: fromGradient.angleDegrees,
                )
            } else {
                ColorStyle.Solid(start)
            }
            onUpdate(frameColor)
        }
        animator?.doOnEnd {
            if (animator === it) {
                animator = null
                targetColor?.let(onUpdate)
            }
        }
        animator?.start()
    }

    fun cancel() {
        val currentAnimator = animator
        animator = null
        targetColor = null
        currentAnimator?.cancel()
    }

    private fun ColorStyle.asGradient(): ColorStyle.LinearGradient {
        return when (this) {
            is ColorStyle.Solid -> ColorStyle.LinearGradient(color, color, 0)
            is ColorStyle.LinearGradient -> this
        }
    }

    companion object {
        private const val ANIMATION_DURATION_MS = 300L
    }
}
