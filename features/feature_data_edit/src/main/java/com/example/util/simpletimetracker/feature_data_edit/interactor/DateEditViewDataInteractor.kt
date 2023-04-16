package com.example.util.simpletimetracker.feature_data_edit.interactor

import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_data_edit.R
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeActivityState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeButtonState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeCommentState
import javax.inject.Inject

class DateEditViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordFilterInteractor: RecordFilterInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
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

    suspend fun getChangeActivityState(
        newTypeId: Long,
    ): DataEditChangeActivityState {
        val type = newTypeId.let { recordTypeInteractor.get(it) }

        return if (type == null) {
            DataEditChangeActivityState.Disabled
        } else {
            DataEditChangeActivityState.Enabled(
                recordTypeViewDataMapper.map(
                    recordType = type,
                    isDarkTheme = prefsInteractor.getDarkMode(),
                )
            )
        }
    }

    fun getChangeCommentState(
        newComment: String,
    ): DataEditChangeCommentState {
        return DataEditChangeCommentState.Enabled(newComment)
    }

    suspend fun getChangeButtonState(
        enabled: Boolean,
    ): DataEditChangeButtonState {
        val theme = if (prefsInteractor.getDarkMode()) {
            R.style.AppThemeDark
        } else {
            R.style.AppTheme
        }

        return DataEditChangeButtonState(
            enabled = enabled,
            backgroundTint = (if (enabled) R.attr.appActiveColor else R.attr.appInactiveColor)
                .let { resourceRepo.getThemedAttr(it, theme) },
        )
    }
}