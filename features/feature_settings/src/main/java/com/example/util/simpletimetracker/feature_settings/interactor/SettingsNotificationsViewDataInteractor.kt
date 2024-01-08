package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsBottomViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCheckboxViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsCollapseViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsRangeViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsSelectorViewData
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTopViewData
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsDurationViewData
import javax.inject.Inject

class SettingsNotificationsViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val settingsMapper: SettingsMapper,
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun execute(
        isCollapsed: Boolean,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        result += SettingsTopViewData(
            block = SettingsBlock.NotificationsTop,
        )

        result += SettingsCollapseViewData(
            block = SettingsBlock.NotificationsCollapse,
            title = resourceRepo.getString(R.string.settings_notification_title),
            opened = !isCollapsed,
            dividerIsVisible = !isCollapsed,
        )

        if (!isCollapsed) {
            val showNotifications = prefsInteractor.getShowNotifications()
            result += SettingsCheckboxViewData(
                block = SettingsBlock.NotificationsShow,
                title = resourceRepo.getString(R.string.settings_show_notifications),
                subtitle = resourceRepo.getString(R.string.settings_show_notifications_hint),
                isChecked = showNotifications,
                dividerIsVisible = !showNotifications,
            )
            if (showNotifications) {
                result += SettingsCheckboxViewData(
                    block = SettingsBlock.NotificationsShowControls,
                    title = resourceRepo.getString(R.string.settings_show_notifications_controls),
                    subtitle = "",
                    isChecked = prefsInteractor.getShowNotificationsControls(),
                )
            }

            val inactivityViewData = loadInactivityReminderViewData()
            result += SettingsSelectorViewData(
                block = SettingsBlock.NotificationsInactivity,
                title = resourceRepo.getString(R.string.settings_inactivity_reminder),
                subtitle = resourceRepo.getString(R.string.settings_inactivity_reminder_hint),
                selectedValue = inactivityViewData.text,
                bottomSpaceIsVisible = !inactivityViewData.enabled,
                dividerIsVisible = !inactivityViewData.enabled,
            )
            if (inactivityViewData.enabled) {
                result += SettingsCheckboxViewData(
                    block = SettingsBlock.NotificationsInactivityRecurrent,
                    title = resourceRepo.getString(R.string.settings_inactivity_reminder_recurrent),
                    subtitle = "",
                    isChecked = prefsInteractor.getInactivityReminderRecurrent(),
                    dividerIsVisible = false,
                )
                result += SettingsRangeViewData(
                    block = SettingsBlock.NotificationsInactivityDoNotDisturb,
                    title = resourceRepo.getString(R.string.settings_do_not_disturb),
                    start = loadInactivityReminderDndStartViewData(),
                    end = loadInactivityReminderDndEndViewData(),
                )
            }

            val activityViewData = loadActivityReminderViewData()
            result += SettingsSelectorViewData(
                block = SettingsBlock.NotificationsActivity,
                title = resourceRepo.getString(R.string.settings_activity_reminder),
                subtitle = resourceRepo.getString(R.string.settings_activity_reminder_hint),
                selectedValue = activityViewData.text,
                bottomSpaceIsVisible = !activityViewData.enabled,
                dividerIsVisible = false,
            )
            if (activityViewData.enabled) {
                result += SettingsCheckboxViewData(
                    block = SettingsBlock.NotificationsActivityRecurrent,
                    title = resourceRepo.getString(R.string.settings_inactivity_reminder_recurrent),
                    subtitle = "",
                    isChecked = prefsInteractor.getActivityReminderRecurrent(),
                    dividerIsVisible = false,
                )
                result += SettingsRangeViewData(
                    block = SettingsBlock.NotificationsActivityDoNotDisturb,
                    title = resourceRepo.getString(R.string.settings_do_not_disturb),
                    start = loadActivityReminderDndStartViewData(),
                    end = loadActivityReminderDndEndViewData(),
                    dividerIsVisible = false,
                )
            }
        }

        result += SettingsBottomViewData(
            block = SettingsBlock.NotificationsBottom,
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
}