package com.example.util.simpletimetracker.feature_change_running_record.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_change_running_record.R
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import javax.inject.Inject

class ChangeRunningRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo
) {

    fun map(runningRecord: RunningRecord, recordType: RecordType?, isDarkTheme: Boolean): ChangeRunningRecordViewData {
        return ChangeRunningRecordViewData(
            name = recordType?.name.orEmpty(),
            timeStarted = runningRecord.timeStarted
                .let(timeMapper::formatTime),
            dateTimeStarted = runningRecord.timeStarted
                .let(timeMapper::formatDateTime),
            duration = runningRecord
                .let { System.currentTimeMillis() - it.timeStarted }
                .let(timeMapper::formatIntervalWithSeconds),
            iconId = recordType?.icon
                ?.let(iconMapper::mapToDrawableResId)
                ?: R.drawable.unknown,
            color = (recordType?.color
                ?.let { colorMapper.mapToColorResId(it, isDarkTheme) }
                ?: ColorMapper.getAvailableColors(isDarkTheme).random())
                .let(resourceRepo::getColor)
        )
    }
}