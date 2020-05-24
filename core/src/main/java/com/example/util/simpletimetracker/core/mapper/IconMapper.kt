package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import javax.inject.Inject

class IconMapper @Inject constructor() {

    fun mapToDrawableResId(iconId: Int): Int {
        return availableIcons.getOrNull(iconId) ?: R.drawable.ic_unknown
    }

    companion object {
        val availableIcons: List<Int> = listOf(
            R.drawable.ic_unknown,
            R.drawable.ic_bed,
            R.drawable.ic_briefcase,
            R.drawable.ic_food,
            R.drawable.ic_cart,
            R.drawable.ic_music
        )
    }
}