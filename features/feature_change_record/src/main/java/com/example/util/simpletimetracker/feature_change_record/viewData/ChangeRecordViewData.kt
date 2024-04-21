package com.example.util.simpletimetracker.feature_change_record.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class ChangeRecordViewData(
    val name: String,
    val tagName: String,
    val timeStarted: String,
    val timeFinished: String,
    val dateTimeStarted: TimeMapper.DateTime,
    val dateTimeFinished: TimeMapper.DateTime,
    val duration: String,
    val iconId: RecordTypeIcon,
    @ColorInt val color: Int,
    val comment: String,
)