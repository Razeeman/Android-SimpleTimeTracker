package com.example.util.simpletimetracker.feature_pomodoro.settings.interactor

import com.example.util.simpletimetracker.core.interactor.LanguageInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsBottomViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCheckboxViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSelectorViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSpinnerNotCheckableViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSpinnerViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTextViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTopViewData
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.DarkModeViewData
import com.example.util.simpletimetracker.feature_settings.viewData.LanguageViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsDurationViewData
import javax.inject.Inject

class PomodoroSettingsViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val settingsMapper: SettingsMapper,
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun execute(): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        val focusViewData = prefsInteractor.getPomodoroFocusTime()
            .let(::mapDuration)
        result += SettingsSelectorViewData(
            block = SettingsBlock.PomodoroFocusTime,
            title = resourceRepo.getString(R.string.pomodoro_settings_focus_time),
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
            title = resourceRepo.getString(R.string.pomodoro_settings_break_time),
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
                title = resourceRepo.getString(R.string.pomodoro_settings_long_break_time),
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
        return value.let(settingsMapper::toDurationViewData)
    }

    private fun mapCount(value: Long): SettingsDurationViewData {
        return value.let(settingsMapper::toCountViewData)
    }
}