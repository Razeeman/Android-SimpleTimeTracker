package com.example.util.simpletimetracker.feature_running_records.interactor

import com.example.util.simpletimetracker.core.interactor.ActivityFilterViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_running_records.mapper.RunningRecordsViewDataMapper
import javax.inject.Inject

class RunningRecordsViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordInteractor: RecordInteractor,
    private val activityFilterViewDataInteractor: ActivityFilterViewDataInteractor,
    private val mapper: RunningRecordsViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val getRunningRecordViewDataMediator: GetRunningRecordViewDataMediator,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
    private val filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor,
) {

    // TODO RETRO what to do on first start, when there is no prev record?
    // TODO RETRO show timers also if any
    suspend fun getViewData(
        completeTypeIds: Set<Long>,
    ): List<ViewHolderType> {
        val recordTypes = recordTypeInteractor.getAll()
        val recordTypesMap = recordTypes.associateBy(RecordType::id)
        val recordTags = recordTagInteractor.getAll()
        val runningRecords = runningRecordInteractor.getAll()
        val recordTypesRunning = runningRecords.map(RunningRecord::id)
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showFirstEnterHint = recordTypes.filterNot(RecordType::hidden).isEmpty()
        val showDefaultTypesButton = !prefsInteractor.getDefaultTypesHidden()
        val showPomodoroButton = prefsInteractor.getEnablePomodoroMode()
        val showRepeatButton = prefsInteractor.getEnableRepeatButton()
        val isPomodoroStarted = prefsInteractor.getPomodoroModeStartedTimestampMs() != 0L
        val retroactiveTrackingModeEnabled = prefsInteractor.getRetroactiveTrackingMode()
        val goals = filterGoalsByDayOfWeekInteractor
            .execute(recordTypeGoalInteractor.getAllTypeGoals())
            .groupBy { it.idData.value }
        val allDailyCurrents = if (goals.isNotEmpty()) {
            getCurrentRecordsDurationInteractor.getAllDailyCurrents(
                typeIds = recordTypesMap.keys.toList(),
                runningRecords = runningRecords,
            )
        } else {
            // No goals - no need to calculate durations.
            emptyMap()
        }

        val runningRecordsViewData = when {
            showFirstEnterHint -> {
                listOf(mapper.mapToTypesEmpty())
            }
            retroactiveTrackingModeEnabled -> {
                val prevRecord = recordInteractor.getPrev(
                    timeStarted = System.currentTimeMillis(),
                ).firstOrNull()
                mapper.mapToRetroActiveMode(
                    typesMap = recordTypesMap,
                    recordTags = recordTags,
                    prevRecord = prevRecord,
                    isDarkTheme = isDarkTheme,
                    useProportionalMinutes = useProportionalMinutes,
                    useMilitaryTime = useMilitaryTime,
                    showSeconds = showSeconds,
                )
            }
            runningRecords.isEmpty() -> {
                listOf(mapper.mapToEmpty())
            }
            else -> {
                runningRecords
                    .sortedByDescending(RunningRecord::timeStarted)
                    .mapNotNull { runningRecord ->
                        getRunningRecordViewDataMediator.execute(
                            type = recordTypesMap[runningRecord.id] ?: return@mapNotNull null,
                            tags = recordTags.filter { it.id in runningRecord.tagIds },
                            goals = goals[runningRecord.id].orEmpty(),
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
                        mapper.mapToHasRunningRecords(),
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
                recordTypeViewDataMapper.mapFiltered(
                    recordType = it,
                    isFiltered = it.id in recordTypesRunning,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    isChecked = recordTypeViewDataMapper.mapGoalCheckmark(
                        type = it,
                        goals = goals,
                        allDailyCurrents = allDailyCurrents,
                    ),
                    isComplete = it.id in completeTypeIds,
                )
            }
            .let { data ->
                mutableListOf<ViewHolderType>().apply {
                    data.let(::addAll)
                    if (showRepeatButton) {
                        recordTypeViewDataMapper.mapToRepeatItem(
                            numberOfCards = numberOfCards,
                            isDarkTheme = isDarkTheme,
                        ).let(::add)
                    }
                    if (showPomodoroButton) {
                        recordTypeViewDataMapper.mapToPomodoroItem(
                            numberOfCards = numberOfCards,
                            isDarkTheme = isDarkTheme,
                            isPomodoroStarted = isPomodoroStarted,
                        ).let(::add)
                    }
                    recordTypeViewDataMapper.mapToAddItem(
                        numberOfCards = numberOfCards,
                        isDarkTheme = isDarkTheme,
                    ).let(::add)
                    if (showDefaultTypesButton) {
                        recordTypeViewDataMapper.mapToAddDefaultItem(
                            numberOfCards = numberOfCards,
                            isDarkTheme = isDarkTheme,
                        ).let(::add)
                    }
                }
            }

        return runningRecordsViewData +
            listOf(DividerViewData(1)) +
            filtersViewData +
            recordTypesViewData
    }
}