package com.example.util.simpletimetracker.feature_records.model

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData

sealed class RecordsState {

    data class RecordsData(
        val data: List<ViewHolderType>,
    ) : RecordsState()

    data class CalendarData(
        val data: List<RecordViewData.Tracked>,
    ) : RecordsState()
}