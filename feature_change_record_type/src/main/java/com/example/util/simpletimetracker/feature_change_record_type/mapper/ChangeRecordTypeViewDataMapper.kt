package com.example.util.simpletimetracker.feature_change_record_type.mapper

import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeViewData
import javax.inject.Inject

class ChangeRecordTypeViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper
) {

    fun map(recordType: RecordType): ChangeRecordTypeViewData {
        return ChangeRecordTypeViewData(
            name = recordType.name
        )
    }
}