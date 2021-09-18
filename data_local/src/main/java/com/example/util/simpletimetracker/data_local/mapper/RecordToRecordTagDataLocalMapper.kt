package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.RecordToRecordTagDBO
import com.example.util.simpletimetracker.domain.model.RecordToRecordTag
import javax.inject.Inject

class RecordToRecordTagDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordToRecordTagDBO): RecordToRecordTag {
        return RecordToRecordTag(
            recordId = dbo.recordId,
            recordTagId = dbo.recordTagId,
        )
    }

    fun map(recordId: Long, recordTagId: Long): RecordToRecordTagDBO {
        return RecordToRecordTagDBO(
            recordId = recordId,
            recordTagId = recordTagId,
        )
    }

    fun map(domain: RecordToRecordTag): RecordToRecordTagDBO {
        return RecordToRecordTagDBO(
            recordId = domain.recordId,
            recordTagId = domain.recordTagId,
        )
    }
}