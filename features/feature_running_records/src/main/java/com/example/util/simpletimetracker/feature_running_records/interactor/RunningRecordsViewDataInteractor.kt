package com.example.util.simpletimetracker.feature_running_records.interactor

import com.example.util.simpletimetracker.core.interactor.ActivityFilterViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_running_records.mapper.RunningRecordsViewDataMapper
import javax.inject.Inject

class RunningRecordsViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val activityFilterViewDataInteractor: ActivityFilterViewDataInteractor,
    private val mapper: RunningRecordsViewDataMapper,
    private val getRunningRecordViewDataMediator: GetRunningRecordViewDataMediator,
) {

    suspend fun getViewData(): List<ViewHolderType> {
        val recordTypes = recordTypeInteractor.getAll()
        val recordTypesMap = recordTypes.associateBy { it.id }
        val recordTags = recordTagInteractor.getAll()
        val runningRecords = runningRecordInteractor.getAll()
        val recordTypesRunning = runningRecords.map { it.id }
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()

        val runningRecordsViewData = when {
            recordTypes.filterNot { it.hidden }.isEmpty() ->
                listOf(mapper.mapToTypesEmpty())
            runningRecords.isEmpty() ->
                listOf(mapper.mapToEmpty())
            else -> {
                runningRecords
                    .sortedByDescending(RunningRecord::timeStarted)
                    .mapNotNull { runningRecord ->
                        getRunningRecordViewDataMediator.execute(
                            type = recordTypesMap[runningRecord.id] ?: return@mapNotNull null,
                            tags = recordTags.filter { it.id in runningRecord.tagIds },
                            record = runningRecord,
                            nowIconVisible = false,
                            goalsVisible = true,
                            totalDurationVisible = true,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            useProportionalMinutes = useProportionalMinutes,
                            showSeconds = showSeconds,
                        )
                    }
                    .plus(
                        mapper.mapToHasRunningRecords()
                    )
            }
        }

        val filter = activityFilterViewDataInteractor.getFilter()
        val filtersViewData = activityFilterViewDataInteractor.getFilterViewData(
            filter = filter,
            isDarkTheme = isDarkTheme,
            appendAddButton = true,
        ).let {
            if (it.isNotEmpty()) it + DividerViewData(2) else it
        }

        val recordTypesViewData = recordTypes
            .filterNot {
                it.hidden
            }
            .let { list ->
                activityFilterViewDataInteractor.applyFilter(list, filter)
            }
            .map {
                mapper.map(
                    recordType = it,
                    isFiltered = it.id in recordTypesRunning,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme
                )
            }
            .plus(
                mapper.mapToAddItem(
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme
                )
            )
            .let {
                if (recordTypes.isEmpty()) {
                    it + mapper.mapToAddDefaultItem(
                        numberOfCards = numberOfCards,
                        isDarkTheme = isDarkTheme
                    )
                } else {
                    it
                }
            }

        return runningRecordsViewData +
            listOf(DividerViewData(1)) +
            filtersViewData +
            recordTypesViewData
    }
}