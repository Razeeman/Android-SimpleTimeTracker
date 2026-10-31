package com.example.util.simpletimetracker.feature_color_selection.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.feature_views.ColorUtils
import javax.inject.Inject

class ColorSelectionMapper @Inject constructor(
    private val colorMapper: ColorMapper,
) {

    fun createDefaultGradient(startColor: AppColor): AppColor.Gradient {
        val endColor = if (startColor.colorInt.isEmpty()) {
            AppColor(
                colorId = (startColor.colorId + 2) % ColorMapper.getAvailableColors().size,
                colorInt = "",
                gradient = null,
            )
        } else {
            val start = colorMapper.mapToColorInt(startColor, isDarkTheme = false)
            AppColor(
                colorId = 0,
                colorInt = ColorUtils.darkenColorByFactor(start).toString(),
                gradient = null,
            )
        }
        return AppColor.Gradient(
            endColorId = endColor.colorId,
            endColorInt = endColor.colorInt,
            angleDegrees = DEFAULT_GRADIENT_ANGLE,
        )
    }

    companion object {
        const val DEFAULT_GRADIENT_ANGLE = 45
    }
}
