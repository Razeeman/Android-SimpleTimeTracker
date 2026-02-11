package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.navigation.params.screen.RangeLengthParams
import javax.inject.Inject

class GetStatisticsDetailRangeInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun execute(
        overrideStatisticsRange: RangeLength? = null,
    ): RangeLengthParams {
        return if (prefsInteractor.getKeepStatisticsRange()) {
            overrideStatisticsRange ?: prefsInteractor.getStatisticsRange()
        } else {
            prefsInteractor.getStatisticsDetailRange()
        }.toParams()
    }
}