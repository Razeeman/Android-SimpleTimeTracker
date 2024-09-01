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
    private val getStatisticsDetailRangeInteractor: GetStatisticsDetailRangeInteractor,
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
        router.navigate(
            data = StatisticsDetailParams(
                transitionName = transitionName,
                filter = recordsFilterMapper.mapFilter(
                    filterType = filterType,
                    selectedId = itemId,
                ).let(::listOf).map(RecordsFilter::toParams),
                range = getStatisticsDetailRangeInteractor.execute(),
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