package com.example.util.simpletimetracker.core.mapper

import android.content.Context
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.domain.di.AppContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconMapper @Inject constructor(
    @AppContext private val context: Context
) {

    val availableIconsNames: Map<String, Int> by lazy {
        val res = mutableMapOf<String, Int>()
        val ta = context.resources.obtainTypedArray(R.array.available_icons)
        (0 until ta.length()).forEach {
            ta.getResourceId(it, R.drawable.unknown).let { resId ->
                res[context.resources.getResourceEntryName(resId)] = resId
            }
        }
        ta.recycle()
        res
    }

    fun mapToDrawableResId(iconName: String): Int {
        return context.resources
            .getIdentifier(iconName, "drawable", context.packageName)
            .takeIf { it in availableIconsNames.values }
            ?: R.drawable.unknown
    }
}