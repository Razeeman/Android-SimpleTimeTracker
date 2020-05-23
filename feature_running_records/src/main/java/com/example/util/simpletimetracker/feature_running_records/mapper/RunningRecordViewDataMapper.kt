package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordViewData
import javax.inject.Inject

class RunningRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper
) {

    fun map(
        runningRecord: RunningRecord,
        recordType: RecordType
    ): RunningRecordViewData {
        return RunningRecordViewData(
            id = runningRecord.id,
            name = recordType.name,
            timeStarted = runningRecord.timeStarted
                .let(timeMapper::formatTime),
            timer = (System.currentTimeMillis() - runningRecord.timeStarted)
                .let(timeMapper::formatIntervalWithSeconds),
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let(colorMapper::mapToColorResId)
                .let(resourceRepo::getColor)
        )
    }
}