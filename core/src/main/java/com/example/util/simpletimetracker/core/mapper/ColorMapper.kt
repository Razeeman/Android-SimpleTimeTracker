package com.example.util.simpletimetracker.core.mapper

import android.graphics.Color
import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.mapper.AppColorMapper
import com.example.util.simpletimetracker.domain.model.AppColor
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

        // Don't change color positions as they are saved in DB by it.
        private val availableColors: List<Int> = listOf(
            R.color.black,
            R.color.red_500,
            R.color.pink_500,
            R.color.purple_500,
            R.color.deep_purple_500,
            R.color.indigo_500,
            R.color.blue_500,
            R.color.light_blue_500,
            R.color.cyan_500,
            R.color.teal_500,
            R.color.green_500,
            R.color.light_green_500,
            R.color.lime_500,
            R.color.yellow_500,
            R.color.amber_500,
            R.color.orange_500,
            R.color.deep_orange_500,
            R.color.brown_500,
            R.color.blue_grey_500
        )

        val colorsNumber = availableColors.size
    }
}