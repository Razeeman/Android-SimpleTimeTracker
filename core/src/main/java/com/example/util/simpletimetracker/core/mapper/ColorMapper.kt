package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import javax.inject.Inject

class ColorMapper @Inject constructor() {

    fun mapToColorResId(colorId: Int): Int {
        return availableColors.getOrNull(colorId) ?: R.color.black
    }

    companion object {
        val availableColors: List<Int> = listOf(
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