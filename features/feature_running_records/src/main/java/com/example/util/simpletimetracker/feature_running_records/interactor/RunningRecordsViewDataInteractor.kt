package com.example.util.simpletimetracker.feature_running_records.interactor

import com.example.util.simpletimetracker.core.interactor.ActivityFilterViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_running_records.mapper.RunningRecordViewDataMapper
import javax.inject.Inject

class RunningRecordsViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordInteractor: RecordInteractor,
    private val activityFilterViewDataInteractor: ActivityFilterViewDataInteractor,
    private val mapper: RunningRecordViewDataMapper,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
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

        val runningRecordsViewData = when {
            recordTypes.filterNot { it.hidden }.isEmpty() ->
                listOf(mapper.mapToTypesEmpty())
            runningRecords.isEmpty() ->
                listOf(mapper.mapToEmpty())
            else -> {
                val (todayRangeStart, todayRangeEnd) = getRange(RangeLength.Day)
                val (weekRangeStart, weekRangeEnd) = getRange(RangeLength.Week)

                runningRecords
                    .sortedByDescending {
                        it.timeStarted
                    }
                    .mapNotNull { runningRecord ->
                        val recordType = recordTypesMap[runningRecord.id] ?: return@mapNotNull null
                        val dailyCurrent = if (recordType.dailyGoalTime > 0L) {
                            recordInteractor.getFromRange(todayRangeStart, todayRangeEnd)
                                .filter { it.typeId == runningRecord.id }
                                .map { rangeMapper.clampToRange(it, todayRangeStart, todayRangeEnd) }
                                .let(rangeMapper::mapToDuration)
                        } else {
                            0L
                        }
                        val weeklyCurrent = if (recordType.weeklyGoalTime > 0L) {
                            recordInteractor.getFromRange(weekRangeStart, weekRangeEnd)
                                .filter { it.typeId == runningRecord.id }
                                .map { rangeMapper.clampToRange(it, weekRangeStart, weekRangeEnd) }
                                .let(rangeMapper::mapToDuration)
                        } else {
                            0L
                        }

                        mapper.map(
                            runningRecord = runningRecord,
                            dailyCurrent = dailyCurrent,
                            weeklyCurrent = weeklyCurrent,
                            recordType = recordType,
                            recordTags = recordTags.filter { it.id in runningRecord.tagIds },
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            showSeconds = showSeconds,
                        )
                    }
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
                    numberOfCards,
                    isDarkTheme
                )
            )

        return runningRecordsViewData +
            listOf(DividerViewData(1)) +
            filtersViewData +
            recordTypesViewData
    }

    private suspend fun getRange(rangeLength: RangeLength): Pair<Long, Long> {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()

        return timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = 0,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
    }
}