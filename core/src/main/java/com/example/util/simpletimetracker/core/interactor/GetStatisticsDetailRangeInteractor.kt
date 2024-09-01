package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams.RangeLengthParams
import javax.inject.Inject

class GetStatisticsDetailRangeInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun execute(): RangeLengthParams {
        return when (val rangeLength = getRangeLength()) {
            is RangeLength.Day -> RangeLengthParams.Day
            is RangeLength.Week -> RangeLengthParams.Week
            is RangeLength.Month -> RangeLengthParams.Month
            is RangeLength.Year -> RangeLengthParams.Year
            is RangeLength.All -> RangeLengthParams.All
            is RangeLength.Custom -> RangeLengthParams.Custom(
                start = rangeLength.range.timeStarted,
                end = rangeLength.range.timeEnded,
            )
            is RangeLength.Last -> RangeLengthParams.Last(
                days = rangeLength.days,
            )
        }
    }

    private suspend fun getRangeLength(): RangeLength {
        return if (prefsInteractor.getKeepStatisticsRange()) {
            prefsInteractor.getStatisticsRange()
        } else {
            prefsInteractor.getStatisticsDetailRange()
        }
    }
}