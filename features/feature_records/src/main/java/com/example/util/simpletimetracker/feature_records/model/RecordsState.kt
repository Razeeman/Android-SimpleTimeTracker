package com.example.util.simpletimetracker.feature_records.model

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_records.customView.RecordsCalendarViewData

sealed interface RecordsState {

    data class RecordsData(
        val data: List<ViewHolderType>,
    ) : RecordsState

    interface CalendarData : RecordsState {

        object Loading : CalendarData

        data class Data(
            val data: RecordsCalendarViewData,
        ) : CalendarData
    }
}