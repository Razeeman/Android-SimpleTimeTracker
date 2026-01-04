package com.example.util.simpletimetracker.feature_running_records.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.interactor.ActivityFilterViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.ActivitySuggestionViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.core.interactor.RecordsShortcutsViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.addBetweenEach
import com.example.util.simpletimetracker.domain.extension.plus
import com.example.util.simpletimetracker.domain.extension.search
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.commentField.CommentFieldViewData
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData
import com.example.util.simpletimetracker.feature_running_records.mapper.RunningRecordsViewDataMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RunningRecordsViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
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
    private val activitySuggestionViewDataInteractor: ActivitySuggestionViewDataInteractor,
    private val recordsShortcutsViewDataInteractor: RecordsShortcutsViewDataInteractor,
) {

    suspend fun getViewData(
        completeTypeIds: Set<Long>,
        navBarHeightDp: Int,
        searchText: String,
        fromSearchChange: Boolean,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val recordTypes = recordTypeInteractor.getAll()
        val recordTypesMap = recordTypes.associateBy(RecordType::id)
        val recordTags = recordTagInteractor.getAll()
        val runningRecords = runningRecordInteractor.getAll()
        val recordTypesRunning = runningRecords.map(RunningRecord::id)
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val durationFormat = prefsInteractor.getDurationFormat()
        val showFirstEnterHint = recordTypes.filterNot(RecordType::hidden).isEmpty()
        val showDefaultTypesButton = !prefsInteractor.getDefaultTypesHidden()
        val showPomodoroButton = prefsInteractor.getEnablePomodoroMode()
        val showRepeatButton = prefsInteractor.getEnableRepeatButton()
        val isPomodoroStarted = prefsInteractor.getPomodoroModeStartedTimestampMs() != 0L
        val retroactiveTrackingModeEnabled = prefsInteractor.getRetroactiveTrackingMode()
        val isFiltersCollapsed = prefsInteractor.getIsActivityFiltersCollapsed()
        val isNavBarAtTheBottom = prefsInteractor.getIsNavBarAtTheBottom()
        val enableSearchOnMain = prefsInteractor.getEnableSearchOnMain()
        val startTimersByLongClick = prefsInteractor.getStartTimerByLongClick()
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
        val actualSearchText = if (enableSearchOnMain) searchText else ""

        val runningRecordsViewData = when {
            showFirstEnterHint -> {
                listOf(mapper.mapToTypesEmpty())
            }
            retroactiveTrackingModeEnabled -> {
                val prevRecord = recordInteractor.getAllPrev(
                    timeStarted = System.currentTimeMillis(),
                )
                mapper.mapToRetroActiveMode(
                    typesMap = recordTypesMap,
                    recordTags = recordTags,
                    prevRecords = prevRecord,
                    isDarkTheme = isDarkTheme,
                    durationFormat = durationFormat,
                    useMilitaryTime = useMilitaryTime,
                    showSeconds = showSeconds,
                    startTimersByLongClick = startTimersByLongClick,
                )
            }
            runningRecords.isEmpty() -> {
                mapper.mapToEmpty(
                    startTimersByLongClick = startTimersByLongClick,
                ).let(::listOf)
            }
            else -> {
                runningRecords
                    .sortedByDescending(RunningRecord::timeStarted)
                    .mapNotNull { runningRecord ->
                        getRunningRecordViewDataMediator.execute(
                            type = recordTypesMap[runningRecord.id] ?: return@mapNotNull null,
                            tags = recordTags,
                            goals = goals[runningRecord.id].orEmpty(),
                            record = runningRecord,
                            nowIconVisible = false,
                            goalsVisible = true,
                            totalDurationVisible = true,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            durationFormat = durationFormat,
                            showSeconds = showSeconds,
                        )
                    }
                    .plus(
                        mapper.mapToHasRunningRecords(
                            startTimersByLongLick = startTimersByLongClick,
                        ),
                    )
            }
        }

        val searchViewData = if (enableSearchOnMain) {
            CommentFieldViewData(
                id = "running_records_search".hashCode().toLong(),
                text = if (fromSearchChange) null else actualSearchText,
                marginTopDp = -3,
                marginHorizontal = 8,
                hint = resourceRepo.getString(R.string.search_hint),
                valueType = CommentFieldViewData.ValueType.TextSingleLine,
            ).let(::listOf)
        } else {
            emptyList()
        }

        val filter = activityFilterViewDataInteractor.getFilter()
        val filtersViewData = activityFilterViewDataInteractor.getFilterViewData(
            filter = filter,
            searchText = actualSearchText,
            isDarkTheme = isDarkTheme,
            isFiltersCollapsed = isFiltersCollapsed,
            appendAddButton = true,
        )

        val suggestionsViewData = activitySuggestionViewDataInteractor.getSuggestionsViewData(
            recordTypesMap = recordTypesMap,
            goals = goals,
            runningRecords = runningRecords,
            allDailyCurrents = allDailyCurrents,
            completeTypeIds = completeTypeIds,
            searchText = actualSearchText,
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
        )

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
                    checkState = recordTypeViewDataMapper.mapGoalCheckmark(
                        type = it,
                        goals = goals,
                        allDailyCurrents = allDailyCurrents,
                    ),
                    isComplete = it.id in completeTypeIds,
                )
            }.search(
                text = actualSearchText,
                searchableContent = { name },
            )
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

        val shortcutsViewData = recordsShortcutsViewDataInteractor.getShortcutsViewData(
            filter = filter,
            recordTypesMap = recordTypesMap,
            recordTags = recordTags,
            runningRecords = runningRecords,
            searchText = actualSearchText,
            isDarkTheme = isDarkTheme,
        )

        // Flexbox layout doesn't fully support clipToPadding = false.
        // Because of that bottom padding with nav bar insets are not applied to main recycler.
        // Instead an empty space added.
        val bottomSpaceForNavBar = if (isNavBarAtTheBottom) {
            emptyList()
        } else {
            EmptySpaceViewData(
                id = "running_records_nav_bar_space".hashCode().toLong(),
                height = EmptySpaceViewData.ViewDimension.ExactSizeDp(navBarHeightDp),
                wrapBefore = true,
            ).let(::listOf)
        }

        return@withContext listOf(
            runningRecordsViewData,
            searchViewData,
            suggestionsViewData,
            filtersViewData,
            recordTypesViewData,
            shortcutsViewData,
        ).filter {
            it.isNotEmpty()
        }.addBetweenEach { index ->
            listOf(DividerViewData(index.toLong()))
        }.flatten().plus(
            bottomSpaceForNavBar,
        )
    }
}