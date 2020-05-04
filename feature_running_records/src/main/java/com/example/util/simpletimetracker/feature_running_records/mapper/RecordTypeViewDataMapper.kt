package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_running_records.adapter.recordType.RecordTypeViewData
import javax.inject.Inject

class RecordTypeViewDataMapper @Inject constructor() {

    fun map(recordType: RecordType): RecordTypeViewData {
        return RecordTypeViewData(
            name = recordType.name,
            color = recordType.color
        )
    }
}