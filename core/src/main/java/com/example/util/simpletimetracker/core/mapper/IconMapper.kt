package com.example.util.simpletimetracker.core.mapper

import android.content.Context
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.domain.di.AppContext
import javax.inject.Inject

class IconMapper @Inject constructor(
    @AppContext private val context: Context
) {

    val availableIcons: List<Int> by lazy {
        val res = mutableListOf<Int>()
        val ta = context.resources.obtainTypedArray(R.array.available_icons)
        (0 until ta.length()).forEach {
            res.add(ta.getResourceId(it, R.drawable.unknown))
        }
        ta.recycle()
        res
    }

    // TODO save drawable name to prevent position shift after array changes
    fun mapToDrawableResId(iconId: Int): Int {
        var res: Int
        context.resources
            .obtainTypedArray(R.array.available_icons)
            .also { res = it.getResourceId(iconId, R.drawable.unknown) }
            .recycle()
        return res
    }
}