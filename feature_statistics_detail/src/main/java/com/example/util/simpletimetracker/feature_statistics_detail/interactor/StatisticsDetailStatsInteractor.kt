package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.interactor.TypesFilterInteractor
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import com.example.util.simpletimetracker.navigation.params.TypesFilterParams
import javax.inject.Inject

class StatisticsDetailStatsInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
    private val recordInteractor: RecordInteractor,
    private val typesFilterInteractor: TypesFilterInteractor,
    private val timeMapper: TimeMapper
) {

    suspend fun getStatsViewData(
        filter: TypesFilterParams,
        rangeLength: RangeLength,
        rangePosition: Int
    ): StatisticsDetailStatsViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        val range = timeMapper.getRangeStartAndEnd(rangeLength, rangePosition, firstDayOfWeek)
        val types = typesFilterInteractor.getTypeIds(filter)
        val records = if (range.first == 0L && range.second == 0L) {
            recordInteractor.getAll()
        } else {
            recordInteractor.getFromRange(range.first, range.second)
        }.filter {
            // Skip records that started before this time range.
            it.typeId in types && it.timeStarted > range.first
        }

        return statisticsDetailViewDataMapper.mapStatsData(
            records = records,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime
        )
    }
}