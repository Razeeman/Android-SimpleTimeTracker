package com.example.util.simpletimetracker.feature_pomodoro.settings.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.PermissionRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_pomodoro.R
import com.example.util.simpletimetracker.feature_pomodoro.settings.model.PomodoroHintButtonType
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.views.SettingsDurationViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsSelectorViewData
import javax.inject.Inject

class PomodoroSettingsViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val permissionRepo: PermissionRepo,
    private val timeMapper: TimeMapper,
) {

    suspend fun execute(): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        if (!permissionRepo.areNotificationsEnabled()) {
            result += HintBigViewData(
                text = resourceRepo.getString(R.string.post_notifications),
                infoIconVisible = false,
                closeIconVisible = false,
                button = HintBigViewData.Button.Present(
                    text = resourceRepo.getString(R.string.schedule_exact_alarms_open_settings),
                    type = PomodoroHintButtonType.PostPermissions,
                ),
            )
        } else if (!permissionRepo.canScheduleExactAlarms()) {
            result += HintBigViewData(
                text = resourceRepo.getString(R.string.schedule_exact_alarms),
                infoIconVisible = false,
                closeIconVisible = false,
                button = HintBigViewData.Button.Present(
                    text = resourceRepo.getString(R.string.schedule_exact_alarms_open_settings),
                    type = PomodoroHintButtonType.ExactAlarms,
                ),
            )
        }

        val focusViewData = prefsInteractor.getPomodoroFocusTime()
            .let(::mapDuration)
        result += SettingsSelectorViewData(
            block = SettingsBlock.PomodoroFocusTime,
            title = resourceRepo.getString(R.string.pomodoro_state_focus),
            subtitle = "",
            selectedValue = focusViewData.text,
            bottomSpaceIsVisible = true,
            dividerIsVisible = false,
            backgroundIsVisible = false,
        )

        val breakViewData = prefsInteractor.getPomodoroBreakTime()
            .let(::mapDuration)
        result += SettingsSelectorViewData(
            block = SettingsBlock.PomodoroBreakTime,
            title = resourceRepo.getString(R.string.pomodoro_state_break),
            subtitle = "",
            selectedValue = breakViewData.text,
            bottomSpaceIsVisible = true,
            dividerIsVisible = false,
            backgroundIsVisible = false,
        )

        val periodsUntilLongBreak = prefsInteractor.getPomodoroPeriodsUntilLongBreak()
            .let(::mapCount)

        if (periodsUntilLongBreak.enabled) {
            val longBreakViewData = prefsInteractor.getPomodoroLongBreakTime()
                .let(::mapDuration)
            result += SettingsSelectorViewData(
                block = SettingsBlock.PomodoroLongBreakTime,
                title = resourceRepo.getString(R.string.pomodoro_state_long_break),
                subtitle = "",
                selectedValue = longBreakViewData.text,
                bottomSpaceIsVisible = true,
                dividerIsVisible = false,
                backgroundIsVisible = false,
            )
        }

        result += SettingsSelectorViewData(
            block = SettingsBlock.PomodoroPeriodsUntilLongBreak,
            title = resourceRepo.getString(R.string.pomodoro_settings_periods_until_long_break),
            subtitle = "",
            selectedValue = periodsUntilLongBreak.text,
            bottomSpaceIsVisible = true,
            dividerIsVisible = false,
            backgroundIsVisible = false,
        )

        return result
    }

    private fun mapDuration(value: Long): SettingsDurationViewData {
        return SettingsDurationViewData(
            text = timeMapper.formatDuration(value),
            enabled = value > 0,
        )
    }

    private fun mapCount(value: Long): SettingsDurationViewData {
        return if (value > 0) {
            SettingsDurationViewData(
                text = value.toString(),
                enabled = true,
            )
        } else {
            SettingsDurationViewData(
                text = resourceRepo.getString(R.string.settings_inactivity_reminder_disabled),
                enabled = false,
            )
        }
    }
}