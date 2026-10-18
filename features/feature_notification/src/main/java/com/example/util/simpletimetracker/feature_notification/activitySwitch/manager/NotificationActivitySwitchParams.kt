package com.example.util.simpletimetracker.feature_notification.activitySwitch.manager

import com.example.util.simpletimetracker.feature_views.viewData.ColorStyle
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class NotificationActivitySwitchParams(
    val icon: RecordTypeIcon,
    val color: ColorStyle,
    val title: CharSequence,
    val subtitle: String,
    val untrackedStartedTimeStamp: Long?,
    val prevRecordDuration: Long?,
    val controls: NotificationControlsParams,
)
