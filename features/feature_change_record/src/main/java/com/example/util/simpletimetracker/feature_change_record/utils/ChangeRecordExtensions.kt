package com.example.util.simpletimetracker.feature_change_record.utils

import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSimpleViewData
import com.example.util.simpletimetracker.feature_views.RecordSimpleView

fun RecordSimpleView.setData(data: ChangeRecordSimpleViewData) {
    itemName = data.name
    itemTimeStarted = data.timeStarted
    itemTimeEnded = data.timeEnded
    itemTimeStartedAccented = data.timeStartedChanged
    itemTimeEndedAccented = data.timeEndedChanged
    itemDuration = data.duration
    itemIcon = data.iconId
    itemColor = data.color
}