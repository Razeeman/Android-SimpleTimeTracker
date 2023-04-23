package com.example.util.simpletimetracker.feature_records_all.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_records_all.R
import com.example.util.simpletimetracker.feature_records_all.model.RecordsAllSortOrder
import com.example.util.simpletimetracker.feature_records_all.viewData.RecordsAllSortOrderViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import javax.inject.Inject

class RecordsAllViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    private val sortOrderList: List<RecordsAllSortOrder> = listOf(
        RecordsAllSortOrder.TIME_STARTED,
        RecordsAllSortOrder.DURATION
    )

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