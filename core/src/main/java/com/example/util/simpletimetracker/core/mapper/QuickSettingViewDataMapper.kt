package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.widget.model.QuickSettingsWidgetType
import javax.inject.Inject

class QuickSettingViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun mapTitle(type: QuickSettingsWidgetType): String {
        return when (type) {
            QuickSettingsWidgetType.AllowMultitasking -> R.string.settings_allow_multitasking
            QuickSettingsWidgetType.ShowRecordTagSelection -> R.string.settings_show_record_tag_selection
            QuickSettingsWidgetType.ShowTimerNotifications -> R.string.settings_show_notifications
            QuickSettingsWidgetType.RetroactiveTracking -> R.string.settings_retroactive_tracking_mode
            QuickSettingsWidgetType.ShowCommentInput -> R.string.settings_show_comment_input
        }.let(resourceRepo::getString)
    }
}
