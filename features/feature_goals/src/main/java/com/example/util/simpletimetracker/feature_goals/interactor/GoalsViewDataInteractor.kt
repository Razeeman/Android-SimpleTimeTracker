package com.example.util.simpletimetracker.feature_goals.interactor

import android.text.SpannableStringBuilder
import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.GoalViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
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
import com.example.util.simpletimetracker.feature_goals.R
import com.example.util.simpletimetracker.feature_views.extension.setForegroundSpan
import com.example.util.simpletimetracker.feature_views.extension.toSpannableString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
            is RecordTypeGoal.Range.Session -> return 0 // Not possible here.
            is RecordTypeGoal.Range.Daily -> dayShift
            is RecordTypeGoal.Range.Weekly,
            is RecordTypeGoal.Range.Monthly,
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

    suspend fun getViewData(
        dayShift: Int,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val durationFormat = prefsInteractor.getDurationFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val types = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val goals = recordTypeGoalInteractor.getAll()

        val typeDataHolders = statisticsMediator.getDataHolders(
            filterType = ChartFilterType.ACTIVITY,
            types = types,
        )
        val categoryDataHolders = statisticsMediator.getDataHolders(
            filterType = ChartFilterType.CATEGORY,
            types = types,
        )
        val tagDataHolders = statisticsMediator.getDataHolders(
            filterType = ChartFilterType.RECORD_TAG,
            types = types,
        )

        val items = goals
            .asSequence()
            .map(RecordTypeGoal::range)
            .toSet()
            .sortedBy {
                when (it) {
                    is RecordTypeGoal.Range.Session -> 0
                    is RecordTypeGoal.Range.Daily -> 1
                    is RecordTypeGoal.Range.Weekly -> 2
                    is RecordTypeGoal.Range.Monthly -> 3
                }
            }
            .filter {
                // No point in statistics for session goals.
                it !is RecordTypeGoal.Range.Session
            }
            .mapNotNull { goalRange ->
                val rangeLength = goalRange.toRangeLength() ?: return@mapNotNull null
                val shiftForRange = getRangeShift(dayShift, goalRange)
                val range = timeMapper.getRangeStartAndEnd(
                    rangeLength = rangeLength,
                    shift = shiftForRange,
                    firstDayOfWeek = firstDayOfWeek,
                    startOfDayShift = startOfDayShift,
                )
                getViewDataForRange(
                    goals = filterGoalsByDayOfWeekInteractor.execute(
                        goals = goals,
                        range = range,
                        startOfDayShift = startOfDayShift,
                    ),
                    types = types,
                    rangeLength = rangeLength,
                    range = range,
                    shift = shiftForRange,
                    firstDayOfWeek = firstDayOfWeek,
                    startOfDayShift = startOfDayShift,
                    typeDataHolders = typeDataHolders,
                    categoryDataHolders = categoryDataHolders,
                    tagDataHolders = tagDataHolders,
                    isDarkTheme = isDarkTheme,
                    durationFormat = durationFormat,
                    showSeconds = showSeconds,
                )
            }
            .toList()

        return@withContext items
            .flatten()
            .takeUnless { it.isEmpty() }
            ?: mapToEmpty()
    }

    private suspend fun getViewDataForRange(
        goals: List<RecordTypeGoal>,
        types: Map<Long, RecordType>,
        rangeLength: RangeLength,
        range: Range,
        shift: Int,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
        typeDataHolders: Map<Long, StatisticsDataHolder>,
        categoryDataHolders: Map<Long, StatisticsDataHolder>,
        tagDataHolders: Map<Long, StatisticsDataHolder>,
        isDarkTheme: Boolean,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        val items = listOf(
            ChartFilterType.ACTIVITY,
            ChartFilterType.CATEGORY,
            ChartFilterType.RECORD_TAG,
        ).flatMap { filterType ->
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
                ),
                data = when (filterType) {
                    ChartFilterType.ACTIVITY -> typeDataHolders
                    ChartFilterType.CATEGORY -> categoryDataHolders
                    ChartFilterType.RECORD_TAG -> tagDataHolders
                },
                isDarkTheme = isDarkTheme,
                durationFormat = durationFormat,
                showSeconds = showSeconds,
            )
        }.sortedBy { it.goal.percent }

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

        return result
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
}