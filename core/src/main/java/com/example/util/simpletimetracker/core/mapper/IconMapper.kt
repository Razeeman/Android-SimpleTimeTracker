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
        getIcons(
            listOf(
                R.array.icon_maps,
                R.array.icon_places,
                R.array.icon_social,
                R.array.icon_action,
                R.array.icon_hardware,
                R.array.icon_alert,
                R.array.icon_av,
                R.array.icon_communication,
                R.array.icon_content,
                R.array.icon_device,
                R.array.icon_editor,
                R.array.icon_file,
                R.array.icon_image,
                R.array.icon_navigation,
                R.array.icon_notification,
                R.array.icon_toggle
            )
        )
    }

    fun mapToDrawableResId(iconName: String): Int {
        return context.resources
            .getIdentifier(iconName, "drawable", context.packageName)
            .takeIf { it in availableIconsNames.values }
            ?: R.drawable.unknown
    }

    private fun getIcons(arrayResIds: List<Int>): Map<String, Int> {
        val res = mutableMapOf<String, Int>()

        arrayResIds.forEach { arrayResId ->
            val ta = context.resources.obtainTypedArray(arrayResId)
            (0 until ta.length()).forEach {
                ta.getResourceId(it, R.drawable.unknown).let { resId ->
                    res[context.resources.getResourceEntryName(resId)] = resId
                }
            }
            ta.recycle()
        }

        return res
    }
}