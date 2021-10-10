package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.RunningRecordDBO
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.RunningRecord
import javax.inject.Inject

class RunningRecordDataLocalMapper @Inject constructor() {

    fun map(dbo: RunningRecordDBO): RunningRecord {
        return RunningRecord(
            id = dbo.id,
            timeStarted = dbo.timeStarted,
            comment = dbo.comment,
            tagIds = listOf(dbo.tagId)
        )
    }

    fun map(domain: RunningRecord): RunningRecordDBO {
        return RunningRecordDBO(
            id = domain.id,
            timeStarted = domain.timeStarted,
            comment = domain.comment,
            tagId = domain.tagIds.firstOrNull().orZero()
        )
    }
}