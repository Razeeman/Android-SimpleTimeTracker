package com.example.util.simpletimetracker.feature_notification.recordType.manager

import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class NotificationTypeParams(
    val id: Long,
    val icon: RecordTypeIcon,
    val color: Int,
    val text: String,
    val timeStarted: String,
    val startedTimeStamp: Long,
    val totalDuration: Long?,
    val goalTime: String,
    val stopButton: String,
    val controls: Controls,
    val controlsHint: String,
) {

    data class Type(
        val id: Long,
        val icon: RecordTypeIcon,
        val color: Int,
    )

    data class Tag(
        val id: Long,
        val text: String,
        val color: Int,
    )

    sealed interface Controls {
        object Disabled : Controls
        data class Enabled(
            val types: List<Type>,
            val typesShift: Int,
            val tags: List<Tag>,
            val tagsShift: Int,
            val controlIconPrev: RecordTypeIcon,
            val controlIconNext: RecordTypeIcon,
            val controlIconColor: Int,
            val selectedTypeId: Long?,
        ) : Controls
    }
}