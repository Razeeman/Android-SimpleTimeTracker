package com.example.util.simpletimetracker.feature_change_running_record.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class ChangeRunningRecordViewData(
    var name: String,
    var timeStarted: String,
    var dateTimeStarted: String,
    var duration: String,
    var goalTime: String,
    @DrawableRes val iconId: Int,
    @ColorInt val color: Int
)