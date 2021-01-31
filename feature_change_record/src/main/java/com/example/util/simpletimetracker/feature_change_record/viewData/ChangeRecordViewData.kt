package com.example.util.simpletimetracker.feature_change_record.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class ChangeRecordViewData(
    val name: String,
    val timeStarted: String,
    val timeFinished: String,
    val dateTimeStarted: String,
    val dateTimeFinished: String,
    val duration: String,
    @DrawableRes val iconId: Int,
    @ColorInt val color: Int,
    val comment: String
)