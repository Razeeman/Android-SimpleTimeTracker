package com.example.util.simpletimetracker.feature_change_record.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class ChangeRecordSimpleViewData(
    val name: String,
    val timeStarted: String,
    val timeEnded: String,
    val timeStartedChanged: Boolean,
    val timeEndedChanged: Boolean,
    val duration: String,
    val iconId: RecordTypeIcon,
    @ColorInt val color: Int,
)