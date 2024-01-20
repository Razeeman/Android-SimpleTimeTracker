package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import javax.inject.Inject

class SettingsCommonInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
) {

    suspend fun getLastSaveString(timestamp: Long): String {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        return timestamp
            .takeUnless { it == 0L }
            ?.let { timeMapper.formatDateTimeYear(it, useMilitaryTime) }
            ?.let { resourceRepo.getString(R.string.settings_automatic_last_save) + " " + it }
            .orEmpty()
    }
}