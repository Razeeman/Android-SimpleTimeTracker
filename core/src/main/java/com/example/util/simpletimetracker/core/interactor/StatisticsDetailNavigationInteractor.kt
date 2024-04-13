package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.mapper.RecordsFilterMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import javax.inject.Inject

class StatisticsDetailNavigationInteractor @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val recordsFilterMapper: RecordsFilterMapper,
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
                    is RangeLength.Last -> StatisticsDetailParams.RangeLengthParams.Last
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
}