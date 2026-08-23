package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.mapper.DayOfWeekViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.mapper.ReminderSummaryMapper
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.views.SettingsBottomViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsCheckboxViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsCollapseViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsDurationViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsHintViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsRangeViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsSelectorViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsSelectorWithButtonViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsTextViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsTopViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsWeekdaysViewData
import javax.inject.Inject

class SettingsNotificationsViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val settingsMapper: SettingsMapper,
    private val reminderSummaryMapper: ReminderSummaryMapper,
    private val prefsInteractor: PrefsInteractor,
    private val dayOfWeekViewDataMapper: DayOfWeekViewDataMapper,
) {

    suspend fun execute(
        isCollapsed: Boolean,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val result = mutableListOf<ViewHolderType>()

        result += SettingsTopViewData(
            block = SettingsBlock.NotificationsTop,
        )

        result += SettingsCollapseViewData(
            block = SettingsBlock.NotificationsCollapse,
            title = resourceRepo.getString(R.string.settings_notification_title),
            opened = !isCollapsed,
            iconResId = R.drawable.notifications,
            iconColor = (if (isDarkTheme) R.color.blue_300 else R.color.blue_200)
                .let(resourceRepo::getColor),
            dividerIsVisible = !isCollapsed,
        )

        if (!isCollapsed) {
            val showNotifications = prefsInteractor.getShowNotifications()
            val showNotificationsControls = prefsInteractor.getShowNotificationsControls()
            result += SettingsCheckboxViewData(
                block = SettingsBlock.NotificationsShow,
                title = resourceRepo.getString(R.string.settings_show_notifications),
                subtitle = resourceRepo.getString(R.string.settings_show_notifications_hint) + "\n" +
                    resourceRepo.getString(R.string.settings_show_notifications_controls_hint),
                isChecked = showNotifications,
                bottomSpaceIsVisible = !showNotifications,
                dividerIsVisible = !showNotifications,
                forceBind = true,
            )
            if (showNotifications) {
                result += SettingsCheckboxViewData(
                    block = SettingsBlock.NotificationsShowControls,
                    title = resourceRepo.getString(R.string.settings_show_notifications_controls),
                    subtitle = "",
                    isChecked = showNotificationsControls,
                    bottomSpaceIsVisible = false,
                    dividerIsVisible = false,
                )
                result += SettingsCheckboxViewData(
                    block = SettingsBlock.NotificationsShowEvenWithNoTimers,
                    title = resourceRepo.getString(R.string.settings_show_notification_even_with_no_timers),
                    subtitle = "",
                    isChecked = prefsInteractor.getShowNotificationEvenWithNoTimers(),
                    bottomSpaceIsVisible = true,
                    dividerIsVisible = true,
                )
            }

            val inactivityViewData = loadInactivityReminderViewData()
            result += SettingsSelectorWithButtonViewData(
                data = SettingsSelectorViewData(
                    block = SettingsBlock.NotificationsInactivity,
                    title = resourceRepo.getString(R.string.settings_inactivity_reminder),
                    subtitle = resourceRepo.getString(R.string.settings_inactivity_reminder_hint),
                    selectedValue = inactivityViewData.text,
                    bottomSpaceIsVisible = !inactivityViewData.enabled,
                    dividerIsVisible = !inactivityViewData.enabled,
                ),
                buttonBlock = SettingsBlock.NotificationsInactivityOptions,
                buttonContent = if (inactivityViewData.enabled) {
                    SettingsSelectorWithButtonViewData.Button.Icon(R.drawable.ic_settings)
                } else {
                    null
                },
            )
            if (inactivityViewData.enabled) {
                result += SettingsHintViewData(
                    block = SettingsBlock.NotificationsInactivityOptionsHint,
                    text = reminderSummaryMapper.map(
                        isRecurrent = prefsInteractor.getInactivityReminderRecurrent(),
                        doNotDisturbStart = prefsInteractor.getInactivityReminderDoNotDisturbStart(),
                        doNotDisturbEnd = prefsInteractor.getInactivityReminderDoNotDisturbEnd(),
                        selectedDaysOfWeek = prefsInteractor.getInactivityReminderDaysOfWeek(),
                        firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
                        useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
                    ),
                )
            }

            val activityViewData = loadActivityReminderViewData()
            result += SettingsSelectorWithButtonViewData(
                data = SettingsSelectorViewData(
                    block = SettingsBlock.NotificationsActivity,
                    title = resourceRepo.getString(R.string.settings_activity_reminder),
                    subtitle = resourceRepo.getString(R.string.settings_activity_reminder_hint),
                    selectedValue = activityViewData.text,
                    bottomSpaceIsVisible = !activityViewData.enabled,
                    dividerIsVisible = !activityViewData.enabled,
                ),
                buttonBlock = SettingsBlock.NotificationsActivityOptions,
                buttonContent = if (activityViewData.enabled) {
                    SettingsSelectorWithButtonViewData.Button.Icon(R.drawable.ic_settings)
                } else {
                    null
                },
            )
            if (activityViewData.enabled) {
                result += SettingsHintViewData(
                    block = SettingsBlock.NotificationsActivityOptionsHint,
                    text = reminderSummaryMapper.map(
                        isRecurrent = prefsInteractor.getActivityReminderRecurrent(),
                        doNotDisturbStart = prefsInteractor.getActivityReminderDoNotDisturbStart(),
                        doNotDisturbEnd = prefsInteractor.getActivityReminderDoNotDisturbEnd(),
                        selectedDaysOfWeek = prefsInteractor.getActivityReminderDaysOfWeek(),
                        firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
                        useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
                    ),
                )
            }

            result += SettingsTextViewData(
                block = SettingsBlock.NotificationsSystemSettings,
                title = resourceRepo.getString(R.string.settings_notifications_system_settings),
                subtitle = resourceRepo.getString(R.string.settings_notifications_system_settings_hint),
                dividerIsVisible = false,
            )
        }

        result += SettingsBottomViewData(
            block = SettingsBlock.NotificationsBottom,
        )

        return result
    }

    suspend fun getInactivityReminderOptionsViewData(): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        result += SettingsCheckboxViewData(
            block = SettingsBlock.NotificationsInactivityRecurrent,
            title = resourceRepo.getString(R.string.settings_inactivity_reminder_recurrent),
            subtitle = "",
            isChecked = prefsInteractor.getInactivityReminderRecurrent(),
        )
        result += SettingsRangeViewData(
            blockStart = SettingsBlock.NotificationsInactivityDoNotDisturbStart,
            blockEnd = SettingsBlock.NotificationsInactivityDoNotDisturbEnd,
            title = resourceRepo.getString(R.string.settings_do_not_disturb),
            start = loadInactivityReminderDndStartViewData(),
            end = loadInactivityReminderDndEndViewData(),
        )
        result += SettingsWeekdaysViewData(
            block = SettingsBlock.NotificationsInactivityDaysOfWeek,
            title = resourceRepo.getString(R.string.settings_reminder_active_days),
            subtitle = "",
            items = loadReminderDaysOfWeekViewData(
                selectedDaysOfWeek = prefsInteractor.getInactivityReminderDaysOfWeek(),
            ),
        )

        return result
    }

    suspend fun getActivityReminderOptionsViewData(): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        result += SettingsCheckboxViewData(
            block = SettingsBlock.NotificationsActivityRecurrent,
            title = resourceRepo.getString(R.string.settings_inactivity_reminder_recurrent),
            subtitle = "",
            isChecked = prefsInteractor.getActivityReminderRecurrent(),
        )
        result += SettingsRangeViewData(
            blockStart = SettingsBlock.NotificationsActivityDoNotDisturbStart,
            blockEnd = SettingsBlock.NotificationsActivityDoNotDisturbEnd,
            title = resourceRepo.getString(R.string.settings_do_not_disturb),
            start = loadActivityReminderDndStartViewData(),
            end = loadActivityReminderDndEndViewData(),
        )
        result += SettingsWeekdaysViewData(
            block = SettingsBlock.NotificationsActivityDaysOfWeek,
            title = resourceRepo.getString(R.string.settings_reminder_active_days),
            subtitle = "",
            items = loadReminderDaysOfWeekViewData(
                selectedDaysOfWeek = prefsInteractor.getActivityReminderDaysOfWeek(),
            ),
        )

        return result
    }

    private suspend fun loadInactivityReminderViewData(): SettingsDurationViewData {
        return prefsInteractor.getInactivityReminderDuration()
            .let(settingsMapper::toDurationViewData)
    }

    private suspend fun loadInactivityReminderDndStartViewData(): String {
        val shift = prefsInteractor.getInactivityReminderDoNotDisturbStart()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        return settingsMapper.toStartOfDayText(shift, useMilitaryTime)
    }

    private suspend fun loadInactivityReminderDndEndViewData(): String {
        val shift = prefsInteractor.getInactivityReminderDoNotDisturbEnd()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        return settingsMapper.toStartOfDayText(shift, useMilitaryTime)
    }

    private suspend fun loadActivityReminderViewData(): SettingsDurationViewData {
        return prefsInteractor.getActivityReminderDuration()
            .let(settingsMapper::toDurationViewData)
    }

    private suspend fun loadActivityReminderDndStartViewData(): String {
        val shift = prefsInteractor.getActivityReminderDoNotDisturbStart()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        return settingsMapper.toStartOfDayText(shift, useMilitaryTime)
    }

    private suspend fun loadActivityReminderDndEndViewData(): String {
        val shift = prefsInteractor.getActivityReminderDoNotDisturbEnd()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        return settingsMapper.toStartOfDayText(shift, useMilitaryTime)
    }

    private suspend fun loadReminderDaysOfWeekViewData(
        selectedDaysOfWeek: Set<DayOfWeek>,
    ): List<DayOfWeekViewData> {
        return dayOfWeekViewDataMapper.mapViewData(
            selectedDaysOfWeek = selectedDaysOfWeek,
            isDarkTheme = prefsInteractor.getDarkMode(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
            width = DayOfWeekViewData.Width.MatchParent,
            paddingHorizontalDp = 4,
        )
    }
}