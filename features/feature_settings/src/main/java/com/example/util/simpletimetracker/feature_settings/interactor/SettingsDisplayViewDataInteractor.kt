package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.WidgetTransparencyPercent
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsBottomViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCheckboxViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCheckboxWithButtonViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCheckboxWithRangeViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCheckboxWithRangeViewData.RangeViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCollapseViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsHintViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSelectorViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSpinnerEvenViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSpinnerViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSpinnerWithButtonViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTextViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTopViewData
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.DaysInCalendarViewData
import com.example.util.simpletimetracker.feature_settings.viewData.RepeatButtonViewData
import com.example.util.simpletimetracker.feature_settings.viewData.WidgetTransparencyViewData
import com.example.util.simpletimetracker.navigation.params.screen.CardOrderDialogParams
import javax.inject.Inject

class SettingsDisplayViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val settingsMapper: SettingsMapper,
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun execute(
        isCollapsed: Boolean,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        result += SettingsTopViewData(
            block = SettingsBlock.DisplayTop,
        )

        result += SettingsCollapseViewData(
            block = SettingsBlock.DisplayCollapse,
            title = resourceRepo.getString(R.string.settings_display_title),
            opened = !isCollapsed,
            dividerIsVisible = !isCollapsed,
        )

        if (!isCollapsed) {
            result += SettingsHintViewData(
                block = SettingsBlock.DisplayUntrackedHint,
                text = resourceRepo.getString(R.string.change_record_untracked_time_hint),
                dividerIsVisible = false,
                bottomSpaceIsVisible = false,
            )
            result += SettingsCheckboxViewData(
                block = SettingsBlock.DisplayUntrackedInRecords,
                title = resourceRepo.getString(R.string.settings_show_untracked_time),
                subtitle = "",
                isChecked = prefsInteractor.getShowUntrackedInRecords(),
                bottomSpaceIsVisible = false,
                dividerIsVisible = false,
            )
            result += SettingsCheckboxViewData(
                block = SettingsBlock.DisplayUntrackedInStatistics,
                title = resourceRepo.getString(R.string.settings_show_untracked_time_statistics),
                subtitle = "",
                isChecked = prefsInteractor.getShowUntrackedInStatistics(),
                bottomSpaceIsVisible = false,
                dividerIsVisible = false,
            )
            result += SettingsSelectorViewData(
                block = SettingsBlock.DisplayUntrackedIgnoreShort,
                title = resourceRepo.getString(R.string.settings_ignore_short_untracked),
                subtitle = resourceRepo.getString(R.string.settings_ignore_short_untracked_hint),
                selectedValue = loadIgnoreShortUntrackedViewData(),
                bottomSpaceIsVisible = false,
                dividerIsVisible = false,
            )
            val untrackedRangeViewData = loadUntrackedRangeViewData()
            result += SettingsCheckboxWithRangeViewData(
                blockCheckbox = SettingsBlock.DisplayUntrackedRangeCheckbox,
                blockStart = SettingsBlock.DisplayUntrackedRangeStart,
                blockEnd = SettingsBlock.DisplayUntrackedRangeEnd,
                title = resourceRepo.getString(R.string.settings_untracked_range),
                subtitle = resourceRepo.getString(R.string.settings_untracked_range_hint),
                isChecked = untrackedRangeViewData is RangeViewData.Enabled,
                range = untrackedRangeViewData,
            )
            val showRecordsCalendar = prefsInteractor.getShowRecordsCalendar()
            result += SettingsCheckboxViewData(
                block = SettingsBlock.DisplayCalendarView,
                title = resourceRepo.getString(R.string.settings_show_records_calendar),
                subtitle = "",
                isChecked = showRecordsCalendar,
                bottomSpaceIsVisible = !showRecordsCalendar,
                dividerIsVisible = !showRecordsCalendar,
            )
            if (showRecordsCalendar) {
                result += SettingsCheckboxViewData(
                    block = SettingsBlock.DisplayReverseOrder,
                    title = resourceRepo.getString(R.string.settings_reverse_order_in_calendar),
                    subtitle = "",
                    isChecked = prefsInteractor.getReverseOrderInCalendar(),
                    bottomSpaceIsVisible = false,
                    dividerIsVisible = false,
                )
                val daysInCalendarViewData = loadDaysInCalendarViewData()
                result += SettingsSpinnerViewData(
                    block = SettingsBlock.DisplayDaysInCalendar,
                    title = resourceRepo.getString(R.string.settings_days_in_calendar),
                    value = daysInCalendarViewData.items
                        .getOrNull(daysInCalendarViewData.selectedPosition)?.text.orEmpty(),
                    items = daysInCalendarViewData.items,
                    selectedPosition = daysInCalendarViewData.selectedPosition,
                    processSameItemSelected = false,
                )
            }
            val showActivityFilters = prefsInteractor.getShowActivityFilters()
            result += SettingsCheckboxViewData(
                block = SettingsBlock.DisplayShowActivityFilters,
                title = resourceRepo.getString(R.string.settings_show_activity_filters),
                subtitle = "",
                isChecked = showActivityFilters,
                bottomSpaceIsVisible = !showActivityFilters,
                dividerIsVisible = !showActivityFilters,
            )
            if (showActivityFilters) {
                result += SettingsCheckboxViewData(
                    block = SettingsBlock.DisplayAllowMultipleActivityFilters,
                    title = resourceRepo.getString(R.string.settings_allow_multiple_activity_filters),
                    subtitle = resourceRepo.getString(R.string.settings_allow_multiple_activity_filters_hint),
                    isChecked = prefsInteractor.getAllowMultipleActivityFilters(),
                    bottomSpaceIsVisible = true,
                    dividerIsVisible = true,
                )
            }
            val enableRepeatButton = prefsInteractor.getEnableRepeatButton()
            result += SettingsCheckboxViewData(
                block = SettingsBlock.DisplayEnableRepeatButton,
                title = resourceRepo.getString(R.string.settings_show_repeat_button),
                subtitle = "",
                isChecked = enableRepeatButton,
                bottomSpaceIsVisible = !enableRepeatButton,
                dividerIsVisible = !enableRepeatButton,
            )
            if (enableRepeatButton) {
                val repeatButtonViewData = loadRepeatButtonViewData()
                result += SettingsSpinnerViewData(
                    block = SettingsBlock.DisplayRepeatButtonMode,
                    title = resourceRepo.getString(R.string.settings_repeat_button_type),
                    value = repeatButtonViewData.items
                        .getOrNull(repeatButtonViewData.selectedPosition)?.text.orEmpty(),
                    items = repeatButtonViewData.items,
                    selectedPosition = repeatButtonViewData.selectedPosition,
                    processSameItemSelected = false,
                ).let(::SettingsSpinnerEvenViewData)
            }
            val enablePomodoroMode = prefsInteractor.getEnablePomodoroMode()
            result += SettingsCheckboxWithButtonViewData(
                data = SettingsCheckboxViewData(
                    block = SettingsBlock.DisplayEnablePomodoroMode,
                    title = resourceRepo.getString(R.string.settings_enable_pomodoro_mode),
                    subtitle = "",
                    isChecked = enablePomodoroMode,
                    bottomSpaceIsVisible = true,
                    dividerIsVisible = true,
                ),
                buttonBlock = SettingsBlock.DisplayPomodoroModeActivities,
                isButtonVisible = enablePomodoroMode,
            )
            result += SettingsCheckboxViewData(
                block = SettingsBlock.DisplayGoalsOnSeparateTabs,
                title = resourceRepo.getString(R.string.settings_show_goals_separately),
                subtitle = "",
                isChecked = prefsInteractor.getShowGoalsSeparately(),
                bottomSpaceIsVisible = true,
                dividerIsVisible = true,
            )
            result += SettingsCheckboxViewData(
                block = SettingsBlock.DisplayNavBarAtTheBottom,
                title = resourceRepo.getString(R.string.settings_show_nav_bar_at_the_bottom),
                subtitle = "",
                isChecked = prefsInteractor.getIsNavBarAtTheBottom(),
                bottomSpaceIsVisible = true,
                dividerIsVisible = true,
            )
            val widgetTransparencyViewData = loadWidgetTransparencyViewData()
            result += SettingsSpinnerViewData(
                block = SettingsBlock.DisplayWidgetBackground,
                title = resourceRepo.getString(R.string.settings_widget_transparency),
                value = widgetTransparencyViewData.selectedValue,
                items = widgetTransparencyViewData.items,
                selectedPosition = widgetTransparencyViewData.selectedPosition,
                processSameItemSelected = false,
            )
            result += SettingsCheckboxViewData(
                block = SettingsBlock.DisplayMilitaryFormat,
                title = resourceRepo.getString(R.string.settings_use_military_time),
                subtitle = loadUseMilitaryTimeViewData(),
                isChecked = prefsInteractor.getUseMilitaryTimeFormat(),
            )
            result += SettingsCheckboxViewData(
                block = SettingsBlock.DisplayMonthDayFormat,
                title = resourceRepo.getString(R.string.settings_use_monthday_time),
                subtitle = loadUseMonthDayTimeViewData(),
                isChecked = prefsInteractor.getUseMonthDayTimeFormat(),
            )
            result += SettingsCheckboxViewData(
                block = SettingsBlock.DisplayProportionalFormat,
                title = resourceRepo.getString(R.string.settings_use_proportional_minutes),
                subtitle = loadUseProportionalMinutesViewData(),
                isChecked = prefsInteractor.getUseProportionalMinutes(),
            )
            result += SettingsCheckboxViewData(
                block = SettingsBlock.DisplayShowSeconds,
                title = resourceRepo.getString(R.string.settings_show_seconds),
                subtitle = "",
                isChecked = prefsInteractor.getShowSeconds(),
            )
            result += mapOrderData(
                CardOrderDialogParams.Type.RecordType(prefsInteractor.getCardOrder()),
            )
            result += mapOrderData(
                CardOrderDialogParams.Type.Category(prefsInteractor.getCategoryOrder()),
            )
            result += mapOrderData(
                CardOrderDialogParams.Type.Tag(prefsInteractor.getTagOrder()),
            )
            result += SettingsTextViewData(
                block = SettingsBlock.DisplayCardSize,
                title = resourceRepo.getString(R.string.settings_change_card_size),
                subtitle = "",
                dividerIsVisible = false,
            )
        }

        result += SettingsBottomViewData(
            block = SettingsBlock.DisplayBottom,
        )

        return result
    }

    private fun mapOrderData(
        type: CardOrderDialogParams.Type,
    ): SettingsSpinnerWithButtonViewData {
        val cardOrderViewData = when (type) {
            is CardOrderDialogParams.Type.RecordType -> {
                type.order.let(settingsMapper::toCardOrderViewData)
            }
            is CardOrderDialogParams.Type.Category -> {
                type.order.let(settingsMapper::toCardOrderViewData)
            }
            is CardOrderDialogParams.Type.Tag -> {
                type.order.let(settingsMapper::toCardTagOrderViewData)
            }
        }

        val block = when (type) {
            is CardOrderDialogParams.Type.RecordType -> SettingsBlock.DisplaySortActivities
            is CardOrderDialogParams.Type.Category -> SettingsBlock.DisplaySortCategories
            is CardOrderDialogParams.Type.Tag -> SettingsBlock.DisplaySortTags
        }

        val title = when (type) {
            is CardOrderDialogParams.Type.RecordType -> R.string.settings_sort_order
            is CardOrderDialogParams.Type.Category -> R.string.settings_sort_order_category
            is CardOrderDialogParams.Type.Tag -> R.string.settings_sort_order_tag
        }.let(resourceRepo::getString)

        return SettingsSpinnerWithButtonViewData(
            data = SettingsSpinnerViewData(
                block = block,
                title = title,
                value = cardOrderViewData.items
                    .getOrNull(cardOrderViewData.selectedPosition)?.text.orEmpty(),
                items = cardOrderViewData.items,
                selectedPosition = cardOrderViewData.selectedPosition,
                processSameItemSelected = false,
            ),
            isButtonVisible = cardOrderViewData.isManualConfigButtonVisible,
        )
    }

    private suspend fun loadIgnoreShortUntrackedViewData(): String {
        return prefsInteractor.getIgnoreShortUntrackedDuration()
            .let(settingsMapper::toDurationViewData)
            .text
    }

    private suspend fun loadUntrackedRangeViewData(): RangeViewData {
        val enabled = prefsInteractor.getUntrackedRangeEnabled()

        return if (enabled) {
            val start = prefsInteractor.getUntrackedRangeStart()
            val end = prefsInteractor.getUntrackedRangeEnd()
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            RangeViewData.Enabled(
                settingsMapper.toStartOfDayText(start, useMilitaryTime),
                settingsMapper.toStartOfDayText(end, useMilitaryTime),
            )
        } else {
            RangeViewData.Disabled
        }
    }

    private suspend fun loadDaysInCalendarViewData(): DaysInCalendarViewData {
        return prefsInteractor.getDaysInCalendar()
            .let(settingsMapper::toDaysInCalendarViewData)
    }

    private suspend fun loadWidgetTransparencyViewData(): WidgetTransparencyViewData {
        return prefsInteractor.getWidgetBackgroundTransparencyPercent()
            .let(::WidgetTransparencyPercent)
            .let(settingsMapper::toWidgetTransparencyViewData)
    }

    private suspend fun loadUseMilitaryTimeViewData(): String {
        return prefsInteractor.getUseMilitaryTimeFormat()
            .let(settingsMapper::toUseMilitaryTimeHint)
    }

    private suspend fun loadUseMonthDayTimeViewData(): String {
        return prefsInteractor.getUseMonthDayTimeFormat()
            .let(settingsMapper::toUseMonthDayTimeHint)
    }

    private suspend fun loadUseProportionalMinutesViewData(): String {
        return prefsInteractor.getUseProportionalMinutes()
            .let(settingsMapper::toUseProportionalMinutesHint)
    }

    private suspend fun loadRepeatButtonViewData(): RepeatButtonViewData {
        return prefsInteractor.getRepeatButtonType()
            .let(settingsMapper::toRepeatButtonViewData)
    }
}