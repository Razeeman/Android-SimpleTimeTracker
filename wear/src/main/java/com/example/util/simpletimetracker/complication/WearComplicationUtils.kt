/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.complication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.TypedValue
import android.view.View
import android.view.View.MeasureSpec
import kotlin.math.roundToInt

fun View.measureExactly(width: Int, height: Int = width) {
    val specWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
    val specHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
    measure(specWidth, specHeight)
    layout(0, 0, measuredWidth, measuredHeight)
}

fun View.getBitmapFromView(): Bitmap {
    val defaultSize by lazy { 20.dpToPx(context) }
    fun Int.checkValue(): Int = this.takeUnless { it <= 0 } ?: defaultSize

    return Bitmap.createBitmap(
        measuredWidth.checkValue(),
        measuredHeight.checkValue(),
        Bitmap.Config.ARGB_8888,
    ).also {
        draw(Canvas(it))
    }
}

fun Int.dpToPx(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics,
    ).roundToInt()
}