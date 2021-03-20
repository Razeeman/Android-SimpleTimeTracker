package com.example.util.simpletimetracker.feature_records_all.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.spinner.CustomSpinner
import com.example.util.simpletimetracker.core.viewData.RecordViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_records_all.R
import com.example.util.simpletimetracker.feature_records_all.model.RecordsAllSortOrder
import com.example.util.simpletimetracker.feature_records_all.viewData.RecordsAllSortOrderViewData
import javax.inject.Inject

class RecordsAllViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper
) {

    private val sortOrderList: List<RecordsAllSortOrder> = listOf(
        RecordsAllSortOrder.TIME_STARTED,
        RecordsAllSortOrder.DURATION
    )

    fun map(
        record: Record,
        recordType: RecordType,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean
    ): ViewHolderType {
        val (timeStarted, timeEnded) = record.timeStarted to record.timeEnded

        return RecordViewData.Tracked(
            id = record.id,
            name = recordType.name,
            timeStarted = timeStarted
                .let { timeMapper.formatTime(it, useMilitaryTime) },
            timeFinished = timeEnded
                .let { timeMapper.formatTime(it, useMilitaryTime) },
            duration = (timeEnded - timeStarted)
                .let(timeMapper::formatInterval),
            iconId = recordType.icon
                .let(iconMapper::mapToDrawableResId),
            color = recordType.color
                .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                .let(resourceRepo::getColor),
            comment = record.comment
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.records_empty.let(resourceRepo::getString)
        )
    }

    fun toSortOrderViewData(currentOrder: RecordsAllSortOrder): RecordsAllSortOrderViewData {
        return RecordsAllSortOrderViewData(
            items = sortOrderList.map(::toSortOrderName).map(CustomSpinner::CustomSpinnerTextItem),
            selectedPosition = toPosition(currentOrder)
        )
    }

    fun toSortOrder(position: Int): RecordsAllSortOrder {
        return sortOrderList.getOrElse(position) { sortOrderList.first() }
    }

    private fun toPosition(sortOrder: RecordsAllSortOrder): Int {
        return sortOrderList.indexOf(sortOrder).takeUnless { it == -1 }.orZero()
    }

    private fun toSortOrderName(sortOrder: RecordsAllSortOrder): String {
        return when (sortOrder) {
            RecordsAllSortOrder.TIME_STARTED -> R.string.records_all_sort_time_started
            RecordsAllSortOrder.DURATION -> R.string.records_all_sort_duration
        }.let(resourceRepo::getString)
    }
}