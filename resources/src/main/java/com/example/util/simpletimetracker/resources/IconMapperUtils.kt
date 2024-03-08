package com.example.util.simpletimetracker.resources

import android.annotation.SuppressLint
import android.content.Context

object IconMapperUtils {

    fun mapIcon(
        context: Context,
        icon: String,
    ): CommonActivityIcon {
        return if (icon.startsWith("ic_") || icon.isEmpty()) {
            mapToDrawableResId(context, icon).let(CommonActivityIcon::Image)
        } else {
            CommonActivityIcon.Text(icon)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun mapToDrawableResId(
        context: Context,
        iconName: String,
    ): Int {
        return context.resources
            .getIdentifier(iconName, "drawable", context.packageName)
            .takeIf { it != 0 }
            ?: R.drawable.app_unknown
    }
}