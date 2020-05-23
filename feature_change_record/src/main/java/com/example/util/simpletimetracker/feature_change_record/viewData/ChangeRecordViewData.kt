package com.example.util.simpletimetracker.feature_change_record.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class ChangeRecordViewData(
    var name: String,
    var timeStarted: String,
    var timeFinished: String,
    var dateTimeStarted: String,
    var dateTimeFinished: String,
    var duration: String,
    @DrawableRes val iconId: Int,
    @ColorInt val color: Int
)