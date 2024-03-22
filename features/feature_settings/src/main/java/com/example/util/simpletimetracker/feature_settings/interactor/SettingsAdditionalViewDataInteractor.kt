package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.extension.shiftTimeStamp
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsBottomViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCheckboxViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCheckboxWithButtonViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCollapseViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsHintViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSelectorViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSelectorWithButtonViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSpinnerEvenViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSpinnerViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTextViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTextWithButtonViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTopViewData
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.FirstDayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.viewData.RepeatButtonViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsStartOfDayViewData
import java.util.Calendar
import javax.inject.Inject

class SettingsAdditionalViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val settingsMapper: SettingsMapper,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
) {

    suspend fun execute(
        isCollapsed: Boolean,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        result += SettingsTopViewData(
            block = SettingsBlock.AdditionalTop,
        )

        result += SettingsCollapseViewData(
            block = SettingsBlock.AdditionalCollapse,
            title = resourceRepo.getString(R.string.settings_additional_title),
            opened = !isCollapsed,
            dividerIsVisible = !isCollapsed,
        )

        if (!isCollapsed) {
            result += SettingsSelectorViewData(
                block = SettingsBlock.AdditionalIgnoreShort,
                title = resourceRepo.getString(R.string.settings_ignore_short_records),
                subtitle = resourceRepo.getString(R.string.settings_ignore_short_records_hint),
                selectedValue = loadIgnoreShortRecordsViewData(),
                bottomSpaceIsVisible = true,
                dividerIsVisible = true,
            )

            val showRecordTagSelection = prefsInteractor.getShowRecordTagSelection()
            result += SettingsCheckboxWithButtonViewData(
                data = SettingsCheckboxViewData(
                    block = SettingsBlock.AdditionalShowTagSelection,
                    title = resourceRepo.getString(R.string.settings_show_record_tag_selection),
                    subtitle = resourceRepo.getString(R.string.settings_show_record_tag_selection_hint),
                    isChecked = showRecordTagSelection,
                    bottomSpaceIsVisible = !showRecordTagSelection,
                    dividerIsVisible = !showRecordTagSelection,
                ),
                buttonBlock = SettingsBlock.AdditionalTagSelectionExcludeActivities,
                isButtonVisible = showRecordTagSelection,
            )
            if (showRecordTagSelection) {
                result += SettingsCheckboxViewData(
                    block = SettingsBlock.AdditionalCloseAfterOneTag,
                    title = resourceRepo.getString(R.string.settings_show_record_tag_close_hint),
                    subtitle = "",
                    isChecked = prefsInteractor.getRecordTagSelectionCloseAfterOne(),
                    bottomSpaceIsVisible = true,
                    dividerIsVisible = true,
                )
            }

            result += SettingsCheckboxViewData(
                block = SettingsBlock.AdditionalKeepStatisticsRange,
                title = resourceRepo.getString(R.string.settings_keep_statistics_range),
                subtitle = resourceRepo.getString(R.string.settings_keep_statistics_range_hint),
                isChecked = prefsInteractor.getKeepStatisticsRange(),
                bottomSpaceIsVisible = true,
                dividerIsVisible = true,
            )

            val firstDayOfWeekViewData = loadFirstDayOfWeekViewData()
            result += SettingsSpinnerViewData(
                block = SettingsBlock.AdditionalFirstDayOfWeek,
                title = resourceRepo.getString(R.string.settings_first_day_of_week),
                value = firstDayOfWeekViewData.items
                    .getOrNull(firstDayOfWeekViewData.selectedPosition)?.text.orEmpty(),
                items = firstDayOfWeekViewData.items,
                selectedPosition = firstDayOfWeekViewData.selectedPosition,
                processSameItemSelected = false,
            )

            val repeatButtonViewData = loadRepeatButtonViewData()
            result += SettingsSpinnerViewData(
                block = SettingsBlock.AdditionalRepeatButton,
                title = resourceRepo.getString(R.string.settings_repeat_button_type),
                value = repeatButtonViewData.items
                    .getOrNull(repeatButtonViewData.selectedPosition)?.text.orEmpty(),
                items = repeatButtonViewData.items,
                selectedPosition = repeatButtonViewData.selectedPosition,
                processSameItemSelected = false,
            ).let(::SettingsSpinnerEvenViewData)

            val startOfDayViewData = loadStartOfDayViewData()
            result += SettingsSelectorWithButtonViewData(
                data = SettingsSelectorViewData(
                    block = SettingsBlock.AdditionalShiftStartOfDay,
                    title = resourceRepo.getString(R.string.settings_start_of_day),
                    subtitle = startOfDayViewData.hint,
                    selectedValue = startOfDayViewData.startOfDayValue,
                    bottomSpaceIsVisible = false,
                    dividerIsVisible = false,
                ),
                buttonBlock = SettingsBlock.AdditionalShiftStartOfDayButton,
                isButtonVisible = startOfDayViewData.startOfDaySign.isNotEmpty(),
                buttonText = startOfDayViewData.startOfDaySign,
            )
            result += SettingsHintViewData(
                block = SettingsBlock.AdditionalShiftStartOfDayHint,
                text = resourceRepo.getString(R.string.settings_start_of_day_hint),
                topSpaceIsVisible = false,
            )
            result += SettingsTextWithButtonViewData(
                buttonBlock = SettingsBlock.AdditionalAutomatedTracking,
                data = SettingsTextViewData(
                    block = SettingsBlock.AdditionalAutomatedTracking,
                    title = resourceRepo.getString(R.string.settings_automated_tracking),
                    subtitle = "",
                    dividerIsVisible = false,
                    layoutIsClickable = false,
                ),
            )
            result += SettingsCheckboxViewData(
                block = SettingsBlock.AdditionalSendEvents,
                title = resourceRepo.getString(R.string.settings_automated_tracking_send_events),
                subtitle = "",
                isChecked = prefsInteractor.getAutomatedTrackingSendEvents(),
                topSpaceIsVisible = false,
                dividerIsVisible = false,
            )
        }

        result += SettingsBottomViewData(
            block = SettingsBlock.AdditionalBottom,
        )

        return result
    }

    private suspend fun loadIgnoreShortRecordsViewData(): String {
        return prefsInteractor.getIgnoreShortRecordsDuration()
            .let(settingsMapper::toDurationViewData)
            .text
    }

    private suspend fun loadFirstDayOfWeekViewData(): FirstDayOfWeekViewData {
        return prefsInteractor.getFirstDayOfWeek()
            .let(settingsMapper::toFirstDayOfWeekViewData)
    }

    private suspend fun loadRepeatButtonViewData(): RepeatButtonViewData {
        return prefsInteractor.getRepeatButtonType()
            .let(settingsMapper::toRepeatButtonViewData)
    }

    private suspend fun loadStartOfDayViewData(): SettingsStartOfDayViewData {
        val shift = prefsInteractor.getStartOfDayShift()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val calendar = Calendar.getInstance()

        val hint = resourceRepo.getString(
            R.string.settings_start_of_day_hint_value,
            timeMapper.formatDateTime(
                time = calendar.shiftTimeStamp(timeMapper.getStartOfDayTimeStamp(), shift),
                useMilitaryTime = useMilitaryTime,
                showSeconds = false,
            ),
        )

        return SettingsStartOfDayViewData(
            startOfDayValue = settingsMapper.toStartOfDayText(shift, useMilitaryTime = true),
            startOfDaySign = settingsMapper.toStartOfDaySign(shift),
            hint = hint,
        )
    }
}