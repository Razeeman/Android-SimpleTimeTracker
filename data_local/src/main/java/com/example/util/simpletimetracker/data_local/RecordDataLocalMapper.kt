package com.example.util.simpletimetracker.data_local

import com.example.util.simpletimetracker.domain.Record
import javax.inject.Inject

class RecordDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordDBO): Record {
        return Record(
            id = dbo.id,
            name = dbo.name,
            timeStarted = dbo.timeStarted,
            timeEnded = dbo.timeEnded
        )
    }

    fun map(domain: Record): RecordDBO {
        return RecordDBO(
            id = 0,
            name = domain.name,
            timeStarted = domain.timeStarted,
            timeEnded = domain.timeEnded
        )
    }
}