package com.example.util.simpletimetracker.feature_views.extension

import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.DynamicDrawableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.view.View
import androidx.annotation.ColorInt

fun Spanned.toSpannableString(): SpannableString {
    return SpannableString(this)
}

fun String.toSpannableString(): SpannableString {
    return SpannableString(this)
}

fun SpannableString.setBackgroundSpan(
    start: Int,
    length: Int,
    @ColorInt color: Int,
): SpannableString {
    setSpan(
        BackgroundColorSpan(color),
        start,
        start + length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
    return this
}

fun SpannableString.setForegroundSpan(
    start: Int,
    length: Int,
    @ColorInt color: Int,
): SpannableString {
    setSpan(
        ForegroundColorSpan(color),
        start,
        start + length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
    return this
}

fun SpannableString.setClickableSpan(
    start: Int,
    length: Int,
    onClick: () -> Unit,
): SpannableString {
    setSpan(
        createClickableSpan(onClick),
        start,
        start + length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
    return this
}

fun SpannableString.setImageSpan(
    start: Int,
    length: Int,
    drawable: Drawable,
    sizeDp: Int,
): SpannableString {
    setSpan(
        ImageSpan(
            drawable.apply {
                setBounds(
                    0,
                    0,
                    sizeDp.dpToPx(),
                    sizeDp.dpToPx(),
                )
            },
            DynamicDrawableSpan.ALIGN_BASELINE,
        ),
        start,
        start + length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
    return this
}

private fun createClickableSpan(
    action: () -> Unit,
): ClickableSpan {
    return object : ClickableSpan() {
        override fun onClick(widget: View) {
            action()
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
        }
    }
}