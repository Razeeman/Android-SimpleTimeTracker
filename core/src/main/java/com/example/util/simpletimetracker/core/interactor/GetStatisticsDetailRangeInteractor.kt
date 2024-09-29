package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.navigation.params.screen.RangeLengthParams
import javax.inject.Inject

class GetStatisticsDetailRangeInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun execute(): RangeLengthParams {
        return getRangeLength().toParams()
    }

    private suspend fun getRangeLength(): RangeLength {
        return if (prefsInteractor.getKeepStatisticsRange()) {
            prefsInteractor.getStatisticsRange()
        } else {
            prefsInteractor.getStatisticsDetailRange()
        }
    }
}