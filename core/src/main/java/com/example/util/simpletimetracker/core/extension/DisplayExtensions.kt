package com.example.util.simpletimetracker.core.extension

import android.content.res.Resources

fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Float.spToPx(): Float = (this * Resources.getSystem().displayMetrics.scaledDensity)