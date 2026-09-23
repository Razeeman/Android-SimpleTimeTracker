package com.example.util.simpletimetracker.core.mapper

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.color.mapper.AppColorMapper
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.feature_views.ColorUtils.darkenColorByFactor
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class ColorMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val appColorMapper: AppColorMapper,
) {

    @ColorInt fun mapToColorInt(color: AppColor, isDarkTheme: Boolean): Int {
        return appColorMapper.mapToColorInt(color).let {
            if (isDarkTheme) darkenColorByFactor(it) else it
        }
    }

    fun toUntrackedColor(isDarkTheme: Boolean): Int {
        return resourceRepo.getThemedAttr(R.attr.appUntrackedColor, isDarkTheme)
    }

    fun toIconColor(isDarkTheme: Boolean): Int {
        return resourceRepo.getThemedAttr(R.attr.appIconColor, isDarkTheme)
    }

    fun toIconColor(
        isDarkTheme: Boolean,
        isFiltered: Boolean,
    ): Int {
        return if (isFiltered) {
            toFilteredIconColor(isDarkTheme)
        } else {
            toIconColor(isDarkTheme)
        }
    }

    fun toIconAlpha(icon: RecordTypeIcon?, isFiltered: Boolean): Float {
        return if (icon is RecordTypeIcon.Text && isFiltered) {
            FILTERED_ICON_TEXT_ALPHA
        } else {
            DEFAULT_ICON_TEXT_ALPHA
        }
    }

    fun toFilteredColor(isDarkTheme: Boolean): Int {
        return resourceRepo.getThemedAttr(R.attr.appFilteredColor, isDarkTheme)
    }

    fun toFilteredColor(
        color: AppColor,
        isDarkTheme: Boolean,
        isFiltered: Boolean,
    ): Int {
        return if (isFiltered) {
            toFilteredColor(isDarkTheme)
        } else {
            mapToColorInt(color, isDarkTheme)
        }
    }

    fun toFilteredUntrackedColor(
        isDarkTheme: Boolean,
        isFiltered: Boolean,
    ): Int {
        return if (isFiltered) {
            toFilteredColor(isDarkTheme)
        } else {
            toUntrackedColor(isDarkTheme)
        }
    }

    fun toFilteredIconColor(isDarkTheme: Boolean): Int {
        return resourceRepo.getThemedAttr(R.attr.appIconFilteredColor, isDarkTheme)
    }

    fun toActiveColor(isDarkTheme: Boolean): Int {
        return resourceRepo.getThemedAttr(R.attr.appActiveColor, isDarkTheme)
    }

    fun toInactiveColor(isDarkTheme: Boolean): Int {
        return resourceRepo.getThemedAttr(R.attr.appInactiveColor, isDarkTheme)
    }

    fun toPositiveColor(isDarkTheme: Boolean): Int {
        return resourceRepo.getThemedAttr(R.attr.appPositiveColor, isDarkTheme)
    }

    fun toNegativeColor(isDarkTheme: Boolean): Int {
        return resourceRepo.getThemedAttr(R.attr.appNegativeColor, isDarkTheme)
    }

    companion object {
        fun getAvailableColors(): List<Int> {
            return availableColors
        }

        private const val DEFAULT_ICON_TEXT_ALPHA = 1.0f
        private const val FILTERED_ICON_TEXT_ALPHA = 0.3f

        // Don't change color positions as they are saved in DB by it.
        private val availableColors: List<Int> = listOf(
            R.color.palette_black, // last by color order.
            R.color.palette_red,
            R.color.palette_pink,
            R.color.palette_purple,
            R.color.palette_deep_purple,
            R.color.palette_indigo,
            R.color.palette_blue,
            R.color.palette_light_blue,
            R.color.palette_cyan,
            R.color.palette_teal,
            R.color.palette_green,
            R.color.palette_light_green,
            R.color.palette_lime,
            R.color.palette_yellow,
            R.color.palette_amber,
            R.color.palette_orange,
            R.color.palette_deep_orange,
            R.color.palette_brown,
            R.color.palette_blue_grey, // after blue by color order.
        )
    }
}