package com.example.util.simpletimetracker.core.mapper

import android.graphics.Color
import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.mapper.AppColorMapper
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class ColorMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val appColorMapper: AppColorMapper,
) {

    @ColorInt fun mapToColorInt(color: AppColor, isDarkTheme: Boolean): Int {
        return appColorMapper.mapToColorInt(color).let {
            if (isDarkTheme) darkenColor(it) else it
        }
    }

    fun toUntrackedColor(isDarkTheme: Boolean): Int {
        return if (isDarkTheme) {
            R.color.colorUntrackedDark
        } else {
            R.color.colorUntracked
        }.let(resourceRepo::getColor)
    }

    fun toIconColor(isDarkTheme: Boolean): Int {
        return if (isDarkTheme) {
            R.color.colorIconDark
        } else {
            R.color.colorIcon
        }.let(resourceRepo::getColor)
    }

    fun toIconAlpha(icon: RecordTypeIcon?, isFiltered: Boolean): Float {
        return if (icon is RecordTypeIcon.Text && isFiltered) {
            FILTERED_ICON_TEXT_ALPHA
        } else {
            DEFAULT_ICON_TEXT_ALPHA
        }
    }

    fun toFilteredColor(isDarkTheme: Boolean): Int {
        return if (isDarkTheme) {
            R.color.colorFilteredDark
        } else {
            R.color.colorFiltered
        }.let(resourceRepo::getColor)
    }

    fun toFilteredIconColor(isDarkTheme: Boolean): Int {
        return if (isDarkTheme) {
            R.color.colorIconFilteredDark
        } else {
            R.color.colorIconFiltered
        }.let(resourceRepo::getColor)
    }

    fun toActiveColor(isDarkTheme: Boolean): Int {
        return if (isDarkTheme) {
            R.color.colorActiveDark
        } else {
            R.color.colorActive
        }.let(resourceRepo::getColor)
    }

    fun toInactiveColor(isDarkTheme: Boolean): Int {
        return if (isDarkTheme) {
            R.color.colorInactiveDark
        } else {
            R.color.colorInactive
        }.let(resourceRepo::getColor)
    }

    fun toPositiveColor(isDarkTheme: Boolean): Int {
        return if (isDarkTheme) {
            R.color.colorPositiveDark
        } else {
            R.color.colorPositive
        }.let(resourceRepo::getColor)
    }

    fun toNegativeColor(isDarkTheme: Boolean): Int {
        return if (isDarkTheme) {
            R.color.colorNegativeDark
        } else {
            R.color.colorNegative
        }.let(resourceRepo::getColor)
    }

    // TODO move to ColorUtils
    /**
     * Darkens color.
     */
    @ColorInt
    fun darkenColor(@ColorInt color: Int): Int {
        return FloatArray(3).apply {
            Color.colorToHSV(color, this)
            // change value
            this[2] *= 0.8f
        }.let(Color::HSVToColor)
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

        val colorsNumber = availableColors.size
    }
}