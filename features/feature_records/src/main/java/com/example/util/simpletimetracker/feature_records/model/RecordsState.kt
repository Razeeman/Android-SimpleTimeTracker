package com.example.util.simpletimetracker.feature_records.model

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_records.customView.RecordsCalendarViewData

sealed class RecordsState {

    data class RecordsData(
        val data: List<ViewHolderType>,
    ) : RecordsState()

    data class CalendarData(
        val data: RecordsCalendarViewData,
    ) : RecordsState()
}