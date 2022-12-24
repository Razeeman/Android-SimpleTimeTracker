package com.example.util.simpletimetracker.feature_change_running_record.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.viewData.GoalTimeViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class ChangeRunningRecordViewData(
    var name: String,
    val tagName: String,
    var timeStarted: String,
    var dateTimeStarted: String,
    var duration: String,
    var goalTime: GoalTimeViewData,
    var goalTime2: GoalTimeViewData,
    var goalTime3: GoalTimeViewData,
    val iconId: RecordTypeIcon,
    @ColorInt val color: Int,
    val comment: String
)