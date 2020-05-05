package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.RunningRecordDBO
import com.example.util.simpletimetracker.domain.model.RunningRecord
import javax.inject.Inject

class RunningRecordDataLocalMapper @Inject constructor() {

    fun map(dbo: RunningRecordDBO): RunningRecord {
        return RunningRecord(
            name = dbo.name,
            timeStarted = dbo.timeStarted
        )
    }

    fun map(domain: RunningRecord): RunningRecordDBO {
        return RunningRecordDBO(
            name = domain.name,
            timeStarted = domain.timeStarted
        )
    }
}