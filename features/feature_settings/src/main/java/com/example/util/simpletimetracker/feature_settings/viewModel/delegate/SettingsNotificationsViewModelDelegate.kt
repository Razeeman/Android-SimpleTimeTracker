package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.interactor.CheckExactAlarmPermissionInteractor
import com.example.util.simpletimetracker.core.interactor.CheckNotificationsPermissionInteractor
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.interactor.NotificationActivityInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationInactivityInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsNotificationsViewDataInteractor
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.action.OpenSystemSettings
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsNotificationsViewModelDelegate @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val settingsMapper: SettingsMapper,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
    private val notificationActivityInteractor: NotificationActivityInteractor,
    private val checkExactAlarmPermissionInteractor: CheckExactAlarmPermissionInteractor,
    private val checkNotificationsPermissionInteractor: CheckNotificationsPermissionInteractor,
    private val settingsNotificationsViewDataInteractor: SettingsNotificationsViewDataInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) : ViewModelDelegate() {

    private var parent: SettingsParent? = null
    private var isCollapsed: Boolean = true

    fun init(parent: SettingsParent) {
        this.parent = parent
    }

    suspend fun getViewData(): List<ViewHolderType> {
        return settingsNotificationsViewDataInteractor.execute(
            isCollapsed = isCollapsed,
        )
    }

    fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.NotificationsCollapse -> onCollapseClick()
            SettingsBlock.NotificationsInactivity -> onInactivityReminderClicked()
            SettingsBlock.NotificationsActivity -> onActivityReminderClicked()
            SettingsBlock.NotificationsInactivityDoNotDisturbStart -> onInactivityReminderDoNotDisturbStartClicked()
            SettingsBlock.NotificationsInactivityDoNotDisturbEnd -> onInactivityReminderDoNotDisturbEndClicked()
            SettingsBlock.NotificationsActivityDoNotDisturbStart -> onActivityReminderDoNotDisturbStartClicked()
            SettingsBlock.NotificationsActivityDoNotDisturbEnd -> onActivityReminderDoNotDisturbEndClicked()
            SettingsBlock.NotificationsSystemSettings -> onSystemSettingsClicked()
            SettingsBlock.NotificationsShow -> onShowNotificationsClicked()
            SettingsBlock.NotificationsShowControls -> onShowNotificationsControlsClicked()
            SettingsBlock.NotificationsWithSwitch -> onShowNotificationWithSwitchClicked()
            SettingsBlock.NotificationsWithSwitchHide -> onShowNotificationWithSwitchHideClicked()
            SettingsBlock.NotificationsInactivityRecurrent -> onInactivityReminderRecurrentClicked()
            SettingsBlock.NotificationsActivityRecurrent -> onActivityReminderRecurrentClicked()
            else -> {
                // Do nothing
            }
        }
    }

    fun onDurationSet(tag: String?, duration: Long) {
        onDurationSetDelegate(tag, duration)
    }

    fun onDurationDisabled(tag: String?) {
        onDurationDisabledDelegate(tag)
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        onDateTimeSetDelegate(timestamp, tag)
    }

    private fun onCollapseClick() = delegateScope.launch {
        isCollapsed = isCollapsed.flip()
        parent?.updateContent()
    }

    private fun onShowNotificationsClicked() {
        fun updateValue(newValue: Boolean) = delegateScope.launch {
            prefsInteractor.setShowNotifications(newValue)
            parent?.updateContent()
            externalViewsInteractor.onShowTimerNotificationsChange()
        }

        delegateScope.launch {
            if (prefsInteractor.getShowNotifications()) {
                updateValue(false)
            } else {
                checkNotificationsPermissionInteractor.execute(
                    onEnabled = { updateValue(true) },
                    onDisabled = { updateValue(false) },
                )
            }
        }
    }

    private fun onShowNotificationsControlsClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowNotificationsControls()
            prefsInteractor.setShowNotificationsControls(newValue)
            parent?.updateContent()
            externalViewsInteractor.onShowTimerNotificationsControlsChange()
        }
    }

    private fun onShowNotificationWithSwitchClicked() {
        fun updateValue(newValue: Boolean) = delegateScope.launch {
            prefsInteractor.setShowNotificationWithSwitch(newValue)
            parent?.updateContent()
            externalViewsInteractor.onShowNotificationWithSwitchChange()
        }

        delegateScope.launch {
            if (prefsInteractor.getShowNotificationWithSwitch()) {
                updateValue(false)
            } else {
                checkNotificationsPermissionInteractor.execute(
                    onEnabled = { updateValue(true) },
                    onDisabled = { updateValue(false) },
                )
            }
        }
    }

    private fun onShowNotificationWithSwitchHideClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowNotificationWithSwitchHide()
            prefsInteractor.setShowNotificationWithSwitchHide(newValue)
            parent?.updateContent()
            externalViewsInteractor.onShowNotificationWithSwitchChange()
        }
    }

    private fun onInactivityReminderClicked() = delegateScope.launch {
        val duration = prefsInteractor.getInactivityReminderDuration()

        fun openDialog() {
            DurationDialogParams(
                tag = SettingsViewModel.INACTIVITY_DURATION_DIALOG_TAG,
                value = DurationDialogParams.Value.DurationSeconds(duration),
            ).let(router::navigate)
        }

        if (duration > 0) {
            openDialog()
        } else {
            checkNotificationsPermissionInteractor.execute(
                onEnabled = ::openDialog,
            )
        }
    }

    private fun onInactivityReminderRecurrentClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getInactivityReminderRecurrent()
            prefsInteractor.setInactivityReminderRecurrent(newValue)
            parent?.updateContent()
            externalViewsInteractor.onInactivityReminderChange()
        }
    }

    private fun onInactivityReminderDoNotDisturbStartClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.INACTIVITY_REMINDER_DND_START_DIALOG_TAG,
                timestamp = prefsInteractor.getInactivityReminderDoNotDisturbStart(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    private fun onInactivityReminderDoNotDisturbEndClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.INACTIVITY_REMINDER_DND_END_DIALOG_TAG,
                timestamp = prefsInteractor.getInactivityReminderDoNotDisturbEnd(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    private fun onActivityReminderClicked() = delegateScope.launch {
        val duration = prefsInteractor.getActivityReminderDuration()

        fun openDialog() {
            DurationDialogParams(
                tag = SettingsViewModel.ACTIVITY_DURATION_DIALOG_TAG,
                value = DurationDialogParams.Value.DurationSeconds(duration),
            ).let(router::navigate)
        }

        if (duration > 0) {
            openDialog()
        } else {
            checkNotificationsPermissionInteractor.execute(onEnabled = ::openDialog)
        }
    }

    private fun onActivityReminderRecurrentClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getActivityReminderRecurrent()
            prefsInteractor.setActivityReminderRecurrent(newValue)
            parent?.updateContent()
            externalViewsInteractor.onActivityReminderChange()
        }
    }

    private fun onActivityReminderDoNotDisturbStartClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.ACTIVITY_REMINDER_DND_START_DIALOG_TAG,
                timestamp = prefsInteractor.getActivityReminderDoNotDisturbStart(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    private fun onActivityReminderDoNotDisturbEndClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.ACTIVITY_REMINDER_DND_END_DIALOG_TAG,
                timestamp = prefsInteractor.getActivityReminderDoNotDisturbEnd(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    private fun onDurationSetDelegate(tag: String?, duration: Long) {
        when (tag) {
            SettingsViewModel.INACTIVITY_DURATION_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setInactivityReminderDuration(duration)
                parent?.updateContent()
                externalViewsInteractor.onInactivityReminderChange()
                checkExactAlarmPermissionInteractor.execute()
            }
            SettingsViewModel.ACTIVITY_DURATION_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setActivityReminderDuration(duration)
                parent?.updateContent()
                externalViewsInteractor.onActivityReminderChange()
                checkExactAlarmPermissionInteractor.execute()
            }
        }
    }

    private fun onDurationDisabledDelegate(tag: String?) {
        when (tag) {
            SettingsViewModel.INACTIVITY_DURATION_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setInactivityReminderDuration(0)
                parent?.updateContent()
                notificationInactivityInteractor.cancel()
            }

            SettingsViewModel.ACTIVITY_DURATION_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setActivityReminderDuration(0)
                parent?.updateContent()
                notificationActivityInteractor.cancel()
            }
        }
    }

    private fun onDateTimeSetDelegate(timestamp: Long, tag: String?) = delegateScope.launch {
        when (tag) {
            SettingsViewModel.INACTIVITY_REMINDER_DND_START_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setInactivityReminderDoNotDisturbStart(newValue)
                parent?.updateContent()
                externalViewsInteractor.onInactivityReminderChange()
            }

            SettingsViewModel.INACTIVITY_REMINDER_DND_END_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setInactivityReminderDoNotDisturbEnd(newValue)
                parent?.updateContent()
                externalViewsInteractor.onInactivityReminderChange()
            }

            SettingsViewModel.ACTIVITY_REMINDER_DND_START_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setActivityReminderDoNotDisturbStart(newValue)
                parent?.updateContent()
                externalViewsInteractor.onActivityReminderChange()
            }

            SettingsViewModel.ACTIVITY_REMINDER_DND_END_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setActivityReminderDoNotDisturbEnd(newValue)
                parent?.updateContent()
                externalViewsInteractor.onActivityReminderChange()
            }
        }
    }

    private fun onSystemSettingsClicked() {
        router.execute(OpenSystemSettings.Notifications)
    }
}