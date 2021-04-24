package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconMapper @Inject constructor(
    private val iconImageMapper: IconImageMapper
) {

    fun mapIcon(icon: String): RecordTypeIcon {
        return if (icon.startsWith("ic_") || icon.isEmpty()) {
            icon.let(iconImageMapper::mapToDrawableResId).let(RecordTypeIcon::Image)
        } else {
            RecordTypeIcon.Emoji(icon)
        }
    }
}