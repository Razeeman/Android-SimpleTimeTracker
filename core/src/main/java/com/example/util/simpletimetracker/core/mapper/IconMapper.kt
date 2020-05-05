package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import javax.inject.Inject

class IconMapper @Inject constructor() {

    fun mapToDrawableId(icon: Int): Int {
        return when (icon) {
            0 -> R.drawable.ic_unknown
            1 -> R.drawable.ic_add
            2 -> R.drawable.ic_bed
            3 -> R.drawable.ic_briefcase
            4 -> R.drawable.ic_food
            5 -> R.drawable.ic_cart
            6 -> R.drawable.ic_music
            else -> R.drawable.ic_unknown
        }
    }
}