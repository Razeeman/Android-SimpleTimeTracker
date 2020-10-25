package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import javax.inject.Inject

class ColorMapper @Inject constructor(
    private val resourceRepo: ResourceRepo
) {

    fun mapToColorResId(colorId: Int, isDarkTheme: Boolean): Int {
        return getAvailableColors(isDarkTheme).getOrNull(colorId) ?: R.color.black
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

    companion object {
        fun getAvailableColors(isDarkTheme: Boolean = false): List<Int> {
            return if (isDarkTheme) availableColorsDark else availableColors
        }

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

        private val availableColorsDark: List<Int> = listOf(
            R.color.black,
            R.color.red_800,
            R.color.pink_800,
            R.color.purple_800,
            R.color.deep_purple_800,
            R.color.indigo_800,
            R.color.blue_800,
            R.color.light_blue_800,
            R.color.cyan_800,
            R.color.teal_800,
            R.color.green_800,
            R.color.light_green_800,
            R.color.lime_800,
            R.color.yellow_800,
            R.color.amber_800,
            R.color.orange_800,
            R.color.deep_orange_800,
            R.color.brown_800,
            R.color.blue_grey_800
        )

        val colorsNumber = availableColors.size
    }
}