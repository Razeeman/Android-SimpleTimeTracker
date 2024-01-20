package com.example.util.simpletimetracker.feature_settings.adapter

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsTextColor
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr

@ColorInt
fun SettingsTextColor.getColor(context: Context): Int {
    fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }
    return when (this) {
        is SettingsTextColor.Default -> getColor(R.color.textSecondary)
        is SettingsTextColor.Attention -> getColor(R.color.colorSecondary)
        is SettingsTextColor.Success -> context.getThemedAttr(R.attr.appPositiveColor)
    }
}