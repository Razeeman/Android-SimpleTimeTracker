package com.example.util.simpletimetracker.feature_change_running_record.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.GoalTimeViewData
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
    var goalTime4: GoalTimeViewData,
    val iconId: RecordTypeIcon,
    @ColorInt val color: Int,
    val comment: String,
    val nowIconVisible: Boolean,
)