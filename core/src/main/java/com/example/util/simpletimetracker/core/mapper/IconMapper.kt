package com.example.util.simpletimetracker.core.mapper

import android.content.Context
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.resources.CommonActivityIcon
import com.example.util.simpletimetracker.resources.IconMapperUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconMapper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun mapIcon(icon: String): RecordTypeIcon {
        return IconMapperUtils.mapIcon(context, icon).let(::map)
    }

    private fun map(icon: CommonActivityIcon): RecordTypeIcon {
        return when (icon) {
            is CommonActivityIcon.Image -> RecordTypeIcon.Image(icon.iconId)
            is CommonActivityIcon.Text -> RecordTypeIcon.Text(icon.text)
        }
    }
}