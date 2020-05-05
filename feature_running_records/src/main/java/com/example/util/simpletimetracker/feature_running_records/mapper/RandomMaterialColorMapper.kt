package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.feature_running_records.R
import javax.inject.Inject

class RandomMaterialColorMapper @Inject constructor() {

    // TODO remove after switching to ADD fragment
    fun mapToColorResId(random: Int): Int {
        return when (random) {
            0 -> R.color.red_600
            1 -> R.color.pink_600
            2 -> R.color.purple_600
            3 -> R.color.deep_purple_600
            4 -> R.color.indigo_600
            5 -> R.color.blue_600
            6 -> R.color.light_blue_600
            7 -> R.color.cyan_600
            8 -> R.color.teal_600
            9 -> R.color.green_600
            10 -> R.color.light_green_600
            11 -> R.color.lime_600
            12 -> R.color.yellow_600
            13 -> R.color.amber_600
            14 -> R.color.orange_600
            15 -> R.color.deep_orange_600
            16 -> R.color.brown_600
            17 -> R.color.blue_grey_600
            18 -> R.color.grey_900
            else -> R.color.black
        }
    }

    companion object {
        const val NUMBER_OF_COLORS = 19
    }
}