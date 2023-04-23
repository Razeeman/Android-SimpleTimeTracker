package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordsDateDivider.RecordsDateDividerViewData
import java.util.Calendar
import javax.inject.Inject

class DateDividerViewDataMapper @Inject constructor(
    private val timeMapper: TimeMapper,
) {

    fun addDateViewData(
        viewData: List<Pair<Long, ViewHolderType>>,
    ): List<ViewHolderType> {
        val calendar = Calendar.getInstance()
        val newViewData = mutableListOf<ViewHolderType>()
        var previousTimeStarted = 0L

        viewData.forEach { (timeStarted, recordViewData) ->
            synchronized(timeMapper) {
                if (!timeMapper.sameDay(timeStarted, previousTimeStarted, calendar)) {
                    timeMapper.formatDateYear(timeStarted)
                        .let(::RecordsDateDividerViewData)
                        .let(newViewData::add)
                }
            }
            previousTimeStarted = timeStarted
            newViewData.add(recordViewData)
        }

        return newViewData
    }
}