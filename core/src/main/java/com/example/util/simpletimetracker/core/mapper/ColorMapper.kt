package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import javax.inject.Inject

class ColorMapper @Inject constructor() {

    fun mapToColorResId(colorId: Int): Int {
        return availableColors.getOrNull(colorId) ?: R.color.black
    }

    companion object {
        private val availableColors: List<Int> = listOf(
            R.color.black,
            R.color.red_600,
            R.color.pink_600,
            R.color.purple_600,
            R.color.deep_purple_600,
            R.color.indigo_600,
            R.color.blue_600,
            R.color.light_blue_600,
            R.color.cyan_600,
            R.color.teal_600,
            R.color.green_600,
            R.color.light_green_600,
            R.color.lime_600,
            R.color.yellow_600,
            R.color.amber_600,
            R.color.orange_600,
            R.color.deep_orange_600,
            R.color.brown_600,
            R.color.blue_grey_600,
            R.color.grey_900
        )
        val colorsNumber = availableColors.size
    }
}