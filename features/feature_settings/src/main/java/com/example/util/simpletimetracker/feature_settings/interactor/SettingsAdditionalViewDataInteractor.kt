package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.extension.shiftTimeStamp
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings_views.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.FirstDayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsStartOfDayViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsBottomViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsCheckboxViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsCheckboxWithButtonViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsCollapseViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsHintViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsSelectorViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsSelectorWithButtonViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsSpinnerViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsTextViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsTextWithButtonViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsTopViewData
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

            result += SettingsCheckboxViewData(
                block = SettingsBlock.AdditionalKeepScreenOn,
                title = resourceRepo.getString(R.string.settings_keep_screen_on),
                subtitle = "",
                isChecked = prefsInteractor.getKeepScreenOn(),
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
            )
            result += SettingsTextViewData(
                block = SettingsBlock.AdditionalDataEdit,
                title = resourceRepo.getString(R.string.settings_data_edit),
                subtitle = "",
            )
            result += SettingsTextViewData(
                block = SettingsBlock.AdditionalComplexRules,
                title = resourceRepo.getString(R.string.settings_complex_rules),
                subtitle = "",
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