package com.example.util.simpletimetracker.feature_notification.activitySwitch.manager

import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

sealed interface NotificationControlsParams {
    object Disabled : NotificationControlsParams

    data class Enabled(
        val hintIsVisible: Boolean,
        val types: List<Type>,
        val typesShift: Int,
        val tags: List<Tag>,
        val tagsShift: Int,
        val controlIconPrev: RecordTypeIcon,
        val controlIconNext: RecordTypeIcon,
        val controlIconColor: Int,
        val selectedTypeId: Long?,
    ) : NotificationControlsParams

    data class Type(
        val id: Long,
        val icon: RecordTypeIcon,
        val color: Int,
        val isChecked: Boolean?,
        val isComplete: Boolean,
    )

    data class Tag(
        val id: Long,
        val text: String,
        val color: Int,
    )
}