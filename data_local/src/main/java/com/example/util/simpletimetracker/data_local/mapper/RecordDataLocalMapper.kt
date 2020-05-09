package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.RecordDBO
import com.example.util.simpletimetracker.domain.model.Record
import javax.inject.Inject

class RecordDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordDBO): Record {
        return Record(
            id = dbo.id,
            typeId = dbo.typeId,
            timeStarted = dbo.timeStarted,
            timeEnded = dbo.timeEnded
        )
    }

    fun map(domain: Record): RecordDBO {
        return RecordDBO(
            id = domain.id,
            typeId = domain.typeId,
            timeStarted = domain.timeStarted,
            timeEnded = domain.timeEnded
        )
    }
}