package com.example.util.simpletimetracker.feature_change_running_record.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon

data class ChangeRunningRecordViewData(
    var name: String,
    var timeStarted: String,
    var dateTimeStarted: String,
    var duration: String,
    var goalTime: String,
    val iconId: RecordTypeIcon,
    @ColorInt val color: Int,
    val comment: String
)