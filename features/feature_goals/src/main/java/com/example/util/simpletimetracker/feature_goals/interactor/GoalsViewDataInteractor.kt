package com.example.util.simpletimetracker.feature_goals.interactor

import android.text.SpannableStringBuilder
import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.GoalViewDataMapper
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
) {

    suspend fun getViewData(): List<ViewHolderType> = withContext(Dispatchers.Default) {
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
            .mapNotNull {
                when (it) {
                    // No point in statistics for session goals.
                    is RecordTypeGoal.Range.Session -> return@mapNotNull null
                    is RecordTypeGoal.Range.Daily -> RangeLength.Day
                    is RecordTypeGoal.Range.Weekly -> RangeLength.Week
                    is RecordTypeGoal.Range.Monthly -> RangeLength.Month
                }
            }
            .map { rangeLength ->
                val range = timeMapper.getRangeStartAndEnd(
                    rangeLength = rangeLength,
                    shift = 0,
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
            val title = when (rangeLength) {
                is RangeLength.Day -> R.string.title_today
                is RangeLength.Week -> R.string.title_this_week
                is RangeLength.Month -> R.string.title_this_month
                else -> return emptyList()
            }.let(resourceRepo::getString)
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