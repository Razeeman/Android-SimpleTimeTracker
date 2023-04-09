package com.example.util.simpletimetracker.feature_data_edit.interactor

import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_data_edit.R
import javax.inject.Inject

class DateEditViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordFilterInteractor: RecordFilterInteractor,
) {

    suspend fun getSelectedRecordsCount(
        filters: List<RecordsFilter>,
    ): String {
        val records = if (filters.isEmpty()) {
            emptyList()
        } else {
            recordFilterInteractor.getByFilter(filters)
        }
        val selectedRecordsCount = records.size

        val recordsString = resourceRepo.getQuantityString(
            R.plurals.statistics_detail_times_tracked,
            selectedRecordsCount
        ).lowercase()

        return "$selectedRecordsCount $recordsString"
    }
}