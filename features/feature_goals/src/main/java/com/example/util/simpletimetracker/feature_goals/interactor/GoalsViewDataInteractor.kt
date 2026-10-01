package com.example.util.simpletimetracker.feature_goals.interactor

import android.text.SpannableStringBuilder
import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.GoalViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.extension.toRangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_goals.R
import com.example.util.simpletimetracker.feature_goals.model.RangeViewData
import com.example.util.simpletimetracker.feature_goals.model.GoalsSetupData
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.extension.setForegroundSpan
import com.example.util.simpletimetracker.feature_views.extension.toSpannableString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.emptyList

class GoalsViewDataInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val statisticsMediator: StatisticsMediator,
    private val prefsInteractor: PrefsInteractor,
    private val goalViewDataMapper: GoalViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor,
    private val rangeViewDataMapper: RangeViewDataMapper,
) {

    suspend fun getRangeShift(
        dayShift: Int,
        goalRange: RecordTypeGoal.Range,
    ): Int {
        return when (goalRange) {
            is RecordTypeGoal.Range.Session -> 0 // Not possible here.
            is RecordTypeGoal.Range.Overall -> 0 // No shift for overall range.
            is RecordTypeGoal.Range.Daily -> dayShift
            is RecordTypeGoal.Range.Weekly,
            is RecordTypeGoal.Range.Monthly,
            is RecordTypeGoal.Range.Yearly,
            -> {
                val startOfDayShift = prefsInteractor.getStartOfDayShift()
                val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
                timeMapper.toTimestampShift(
                    toTime = timeMapper.toDayDateTimestamp(
                        daysFromToday = dayShift,
                        startOfDayShift = startOfDayShift,
                    ),
                    range = goalRange.toRangeLength() ?: return 0,
                    firstDayOfWeek = firstDayOfWeek,
                ).toInt()
            }
        }
    }

    suspend fun getSetupData(): GoalsSetupData = withContext(Dispatchers.Default) {
        GoalsSetupData(
            isDarkTheme = prefsInteractor.getDarkMode(),
            durationFormat = prefsInteractor.getDurationFormat(),
            showSeconds = prefsInteractor.getShowSeconds(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
            hideFinishedGoals = prefsInteractor.getHideFinishedGoals(),
            types = recordTypeInteractor.getAll().associateBy(RecordType::id),
            goals = recordTypeGoalInteractor.getAll(),
        )
    }

    suspend fun getViewDataForRange(
        dayShift: Int,
        goalRange: RecordTypeGoal.Range,
        setup: GoalsSetupData,
    ): RangeViewData = withContext(Dispatchers.Default) {
        val empty = RangeViewData(
            items = emptyList(),
            hasHiddenFinishedGoals = false,
        )
        val rangeLength = goalRange.toRangeLength() ?: return@withContext empty
        val goalsForRange = setup.goals.filter { it.range == goalRange }
        if (goalsForRange.isEmpty()) return@withContext empty

        val shiftForRange = getRangeShift(
            dayShift = dayShift,
            goalRange = goalRange,
        )
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = shiftForRange,
            firstDayOfWeek = setup.firstDayOfWeek,
            startOfDayShift = setup.startOfDayShift,
        )
        val goals = filterGoalsByDayOfWeekInteractor.execute(
            goals = goalsForRange,
            range = range,
            startOfDayShift = setup.startOfDayShift,
        )
        getViewDataForRange(
            goals = goals,
            types = setup.types,
            rangeLength = rangeLength,
            range = range,
            shift = shiftForRange,
            firstDayOfWeek = setup.firstDayOfWeek,
            startOfDayShift = setup.startOfDayShift,
            isDarkTheme = setup.isDarkTheme,
            durationFormat = setup.durationFormat,
            showSeconds = setup.showSeconds,
            hideFinishedGoals = setup.hideFinishedGoals,
        )
    }

    fun combineRanges(
        items: List<RangeViewData?>,
    ): List<ViewHolderType> {
        val visibleItems = items.filterNotNull().flatMap(RangeViewData::items)
        return when {
            // Have items to show - show them.
            visibleItems.isNotEmpty() -> visibleItems
            // No visible items, null means load in progress - show loader.
            items.any { it == null } -> listOf(LoaderViewData())
            // No visible items and not loading but some are hidden - show message.
            items.filterNotNull().any(RangeViewData::hasHiddenFinishedGoals) -> mapToAllFinished()
            // Empty.
            else -> mapToEmpty()
        }
    }

    private suspend fun getViewDataForRange(
        goals: List<RecordTypeGoal>,
        types: Map<Long, RecordType>,
        rangeLength: RangeLength,
        range: Range,
        shift: Int,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
        isDarkTheme: Boolean,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
        hideFinishedGoals: Boolean,
    ): RangeViewData {
        val result = mutableListOf<ViewHolderType>()

        val allItems = listOf(
            ChartFilterType.ACTIVITY,
            ChartFilterType.CATEGORY,
            ChartFilterType.RECORD_TAG,
        ).flatMap { filterType ->
            val hasGoals = goals.any { goal ->
                when (filterType) {
                    ChartFilterType.ACTIVITY -> goal.idData is RecordTypeGoal.IdData.Type
                    ChartFilterType.CATEGORY -> goal.idData is RecordTypeGoal.IdData.Category
                    ChartFilterType.RECORD_TAG -> goal.idData is RecordTypeGoal.IdData.Tag
                }
            }
            if (!hasGoals) return@flatMap emptyList()
            goalViewDataMapper.mapStatisticsList(
                goals = goals,
                types = types,
                filterType = filterType,
                filteredIds = emptyList(),
                rangeLength = rangeLength,
                statistics = statisticsMediator.getStatistics(
                    filterType = filterType,
                    filteredIds = emptyList(),
                    range = range,
                    forceSeconds = true,
                ),
                data = statisticsMediator.getDataHolders(
                    filterType = filterType,
                    types = types,
                ),
                isDarkTheme = isDarkTheme,
                durationFormat = durationFormat,
                showSeconds = showSeconds,
            )
        }.sortedBy { it.goal.percent }

        val items = if (hideFinishedGoals) {
            allItems.filterNot {
                it.goal.goalState == GoalCheckmarkView.CheckState.GOAL_REACHED
            }
        } else {
            allItems
        }

        if (items.isNotEmpty()) {
            val title = rangeViewDataMapper.mapToShareTitle(
                rangeLength = rangeLength,
                position = shift,
                startOfDayShift = startOfDayShift,
                firstDayOfWeek = firstDayOfWeek,
            )
            HintViewData(title).let(result::add)

            result.addAll(items)
        }

        return RangeViewData(
            items = result,
            hasHiddenFinishedGoals = items.size < allItems.size,
        )
    }

    private fun mapToEmpty(): List<ViewHolderType> {
        val emptyHint = resourceRepo.getString(R.string.no_goals_exist)
        val addHint = resourceRepo.getString(R.string.goal_add_hint)
            .toSpannableString()
            .apply {
                setForegroundSpan(color = resourceRepo.getColor(R.color.textHintCommon))
            }

        return HintBigViewData(
            text = SpannableStringBuilder()
                .append(emptyHint)
                .append("\n")
                .append(addHint),
            infoIconVisible = true,
            closeIconVisible = false,
        ).let(::listOf)
    }

    private fun mapToAllFinished(): List<ViewHolderType> {
        return HintBigViewData(
            text = resourceRepo.getString(R.string.all_goals_finished),
            infoIconVisible = false,
            closeIconVisible = false,
        ).let(::listOf)
    }
}