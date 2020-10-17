package com.example.util.simpletimetracker.feature_running_records.interactor

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_running_records.mapper.RunningRecordViewDataMapper
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordDividerViewData
import javax.inject.Inject

class RunningRecordsViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val runningRecordViewDataMapper: RunningRecordViewDataMapper
) {

    suspend fun getViewData(): List<ViewHolderType> {
        val recordTypes = recordTypeInteractor.getAll()
        val recordTypesMap = recordTypes.map { it.id to it }.toMap()
        val runningRecords = runningRecordInteractor.getAll()
        val recordTypesRunning = runningRecords.map { it.id }
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val runningRecordsViewData = when {
            recordTypes.filterNot { it.hidden }.isEmpty() -> emptyList()
            runningRecords.isEmpty() -> listOf(runningRecordViewDataMapper.mapToEmpty())
            else -> runningRecords
                .sortedByDescending {
                    it.timeStarted
                }
                .mapNotNull { runningRecord ->
                    recordTypesMap[runningRecord.id]?.let { type -> runningRecord to type }
                }
                .map { (runningRecord, recordType) ->
                    runningRecordViewDataMapper.map(runningRecord, recordType, isDarkTheme)
                }
        }

        val recordTypesViewData = recordTypes
            .filterNot {
                it.hidden
            }
            .map {
                runningRecordViewDataMapper.map(
                    recordType = it,
                    isFiltered = it.id in recordTypesRunning,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme
                )
            }
            .plus(runningRecordViewDataMapper.mapToAddItem(numberOfCards))

        return runningRecordsViewData +
            listOf(RunningRecordDividerViewData) +
            recordTypesViewData
    }
}