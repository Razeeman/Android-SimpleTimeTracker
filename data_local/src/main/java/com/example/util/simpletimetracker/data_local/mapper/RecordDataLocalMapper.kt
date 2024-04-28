package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.RecordDBO
import com.example.util.simpletimetracker.data_local.model.RecordWithRecordTagsDBO
import com.example.util.simpletimetracker.domain.extension.dropMillis
import com.example.util.simpletimetracker.domain.extension.dropSeconds
import com.example.util.simpletimetracker.domain.model.Record
import javax.inject.Inject

class RecordDataLocalMapper @Inject constructor() {

    fun map(
        dbo: RecordWithRecordTagsDBO,
        showSeconds: Boolean,
    ): Record {
        return Record(
            id = dbo.record.id,
            typeId = dbo.record.typeId,
            timeStarted = formatSeconds(dbo.record.timeStarted, showSeconds),
            timeEnded = formatSeconds(dbo.record.timeEnded, showSeconds),
            comment = dbo.record.comment,
            tagIds = dbo.recordTags.map { it.id },
        )
    }

    fun map(domain: Record): RecordDBO {
        return RecordDBO(
            id = domain.id,
            typeId = domain.typeId,
            timeStarted = formatMillis(domain.timeStarted),
            timeEnded = formatMillis(domain.timeEnded),
            comment = domain.comment,
            tagId = 0,
        )
    }

    private fun formatSeconds(time: Long, showSeconds: Boolean): Long {
        return if (showSeconds) time else time.dropSeconds()
    }

    private fun formatMillis(time: Long): Long {
        return time.dropMillis()
    }
}