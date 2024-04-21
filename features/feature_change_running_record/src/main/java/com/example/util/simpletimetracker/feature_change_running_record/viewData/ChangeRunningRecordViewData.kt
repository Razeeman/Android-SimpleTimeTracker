package com.example.util.simpletimetracker.feature_change_running_record.viewData

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData

data class ChangeRunningRecordViewData(
    val recordPreview: RunningRecordViewData?,
    val dateTimeStarted: TimeMapper.DateTime,
)