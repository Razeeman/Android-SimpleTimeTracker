package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import javax.inject.Inject

class SettingsOpenDateTimeDialogRouter @Inject constructor(
    private val router: Router,
    private val settingsMapper: SettingsMapper,
) {

    fun openDateTimeDialog(
        tag: String,
        timestamp: Long,
        useMilitaryTime: Boolean,
    ) {
        DateTimeDialogParams(
            tag = tag,
            type = DateTimeDialogType.TIME,
            timestamp = timestamp.let(settingsMapper::startOfDayShiftToTimeStamp),
            useMilitaryTime = useMilitaryTime,
        ).let(router::navigate)
    }
}