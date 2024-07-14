package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordsFilterMapper
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import javax.inject.Inject

class StatisticsDetailNavigationInteractor @Inject constructor(
    private val router: Router,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val prefsInteractor: PrefsInteractor,
    private val recordsFilterMapper: RecordsFilterMapper,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
) {

    suspend fun navigate(
        transitionName: String,
        filterType: ChartFilterType,
        shift: Int,
        sharedElements: Map<Any, String>,
        itemId: Long,
        itemName: String,
        itemIcon: RecordTypeIcon?,
        itemColor: Int,
    ) {
        val rangeLength = if (prefsInteractor.getKeepStatisticsRange()) {
            prefsInteractor.getStatisticsRange()
        } else {
            prefsInteractor.getStatisticsDetailRange()
        }

        router.navigate(
            data = StatisticsDetailParams(
                transitionName = transitionName,
                filter = recordsFilterMapper.mapFilter(
                    filterType = filterType,
                    selectedId = itemId,
                ).let(::listOf).map(RecordsFilter::toParams),
                range = when (rangeLength) {
                    is RangeLength.Day -> StatisticsDetailParams.RangeLengthParams.Day
                    is RangeLength.Week -> StatisticsDetailParams.RangeLengthParams.Week
                    is RangeLength.Month -> StatisticsDetailParams.RangeLengthParams.Month
                    is RangeLength.Year -> StatisticsDetailParams.RangeLengthParams.Year
                    is RangeLength.All -> StatisticsDetailParams.RangeLengthParams.All
                    is RangeLength.Custom -> StatisticsDetailParams.RangeLengthParams.Custom(
                        start = rangeLength.range.timeStarted,
                        end = rangeLength.range.timeEnded,
                    )
                    is RangeLength.Last -> StatisticsDetailParams.RangeLengthParams.Last(
                        days = rangeLength.days,
                    )
                },
                shift = shift,
                preview = StatisticsDetailParams.Preview(
                    name = itemName,
                    iconId = itemIcon?.toParams(),
                    color = itemColor,
                ),
            ),
            sharedElements = sharedElements,
        )
    }

    suspend fun navigateByGoal(
        goalId: Long,
        shift: Int,
    ) {
        val goal = recordTypeGoalInteractor.get(goalId) ?: return
        val isDarkTheme = prefsInteractor.getDarkMode()

        when (goal.idData) {
            is RecordTypeGoal.IdData.Type -> {
                val type = recordTypeInteractor.get(goal.idData.value) ?: return
                navigate(
                    transitionName = "",
                    filterType = ChartFilterType.ACTIVITY,
                    shift = shift,
                    sharedElements = emptyMap(),
                    itemId = type.id,
                    itemName = type.name,
                    itemIcon = iconMapper.mapIcon(type.icon),
                    itemColor = colorMapper.mapToColorInt(
                        color = type.color,
                        isDarkTheme = isDarkTheme,
                    ),
                )
            }
            is RecordTypeGoal.IdData.Category -> {
                val category = categoryInteractor.get(goal.idData.value) ?: return
                navigate(
                    transitionName = "",
                    filterType = ChartFilterType.CATEGORY,
                    shift = shift,
                    sharedElements = emptyMap(),
                    itemId = category.id,
                    itemName = category.name,
                    itemIcon = null,
                    itemColor = colorMapper.mapToColorInt(
                        color = category.color,
                        isDarkTheme = isDarkTheme,
                    ),
                )
            }
        }
    }
}