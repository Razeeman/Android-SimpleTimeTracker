package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.CheckExactAlarmPermissionInteractor
import com.example.util.simpletimetracker.core.interactor.CheckNotificationsPermissionInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationActivityInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationInactivityInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsDurationViewData
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsNotificationsViewModelDelegate @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val settingsMapper: SettingsMapper,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
    private val notificationActivityInteractor: NotificationActivityInteractor,
    private val checkExactAlarmPermissionInteractor: CheckExactAlarmPermissionInteractor,
    private val checkNotificationsPermissionInteractor: CheckNotificationsPermissionInteractor,
) : ViewModelDelegate() {

    val showNotificationsCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getShowNotifications() }
    val showNotificationsControlsCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getShowNotificationsControls() }
    val inactivityReminderViewData: LiveData<SettingsDurationViewData>
        by lazySuspend { loadInactivityReminderViewData() }
    val inactivityReminderRecurrentCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getInactivityReminderRecurrent() }
    val inactivityReminderDndStartViewData: LiveData<String>
        by lazySuspend { loadInactivityReminderDndStartViewData() }
    val inactivityReminderDndEndViewData: LiveData<String>
        by lazySuspend { loadInactivityReminderDndEndViewData() }
    val activityReminderViewData: LiveData<SettingsDurationViewData>
        by lazySuspend { loadActivityReminderViewData() }
    val activityReminderRecurrentCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getActivityReminderRecurrent() }
    val activityReminderDndStartViewData: LiveData<String>
        by lazySuspend { loadActivityReminderDndStartViewData() }
    val activityReminderDndEndViewData: LiveData<String>
        by lazySuspend { loadActivityReminderDndEndViewData() }

    private var parent: SettingsParent? = null

    fun init(parent: SettingsParent) {
        this.parent = parent
    }

    suspend fun onUseMilitaryTimeClicked() {
        updateActivityReminderDndStartViewData()
        updateActivityReminderDndEndViewData()
        updateInactivityReminderDndStartViewData()
        updateInactivityReminderDndEndViewData()
    }

    fun onShowNotificationsClicked() {
        fun updateValue(newValue: Boolean) = delegateScope.launch {
            prefsInteractor.setShowNotifications(newValue)
            showNotificationsCheckbox.set(newValue)
            notificationTypeInteractor.updateNotifications()
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

    fun onShowNotificationsControlsClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowNotificationsControls()
            prefsInteractor.setShowNotificationsControls(newValue)
            showNotificationsControlsCheckbox.set(newValue)
            notificationTypeInteractor.updateNotifications()
        }
    }

    fun onInactivityReminderClicked() = delegateScope.launch {
        val duration = prefsInteractor.getInactivityReminderDuration()

        fun openDialog() {
            DurationDialogParams(
                tag = SettingsViewModel.INACTIVITY_DURATION_DIALOG_TAG,
                duration = duration,
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

    fun onInactivityReminderRecurrentClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getInactivityReminderRecurrent()
            prefsInteractor.setInactivityReminderRecurrent(newValue)
            inactivityReminderRecurrentCheckbox.set(newValue)
            notificationInactivityInteractor.cancel()
            notificationInactivityInteractor.checkAndSchedule()
        }
    }

    fun onInactivityReminderDoNotDisturbStartClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.INACTIVITY_REMINDER_DND_START_DIALOG_TAG,
                timestamp = prefsInteractor.getInactivityReminderDoNotDisturbStart(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    fun onInactivityReminderDoNotDisturbEndClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.INACTIVITY_REMINDER_DND_END_DIALOG_TAG,
                timestamp = prefsInteractor.getInactivityReminderDoNotDisturbEnd(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    fun onActivityReminderClicked() = delegateScope.launch {
        val duration = prefsInteractor.getActivityReminderDuration()

        fun openDialog() {
            DurationDialogParams(
                tag = SettingsViewModel.ACTIVITY_DURATION_DIALOG_TAG,
                duration = duration,
            ).let(router::navigate)
        }

        if (duration > 0) {
            openDialog()
        } else {
            checkNotificationsPermissionInteractor.execute(onEnabled = ::openDialog)
        }
    }

    fun onActivityReminderRecurrentClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getActivityReminderRecurrent()
            prefsInteractor.setActivityReminderRecurrent(newValue)
            activityReminderRecurrentCheckbox.set(newValue)
            notificationActivityInteractor.cancel()
            notificationActivityInteractor.checkAndSchedule()
        }
    }

    fun onActivityReminderDoNotDisturbStartClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.ACTIVITY_REMINDER_DND_START_DIALOG_TAG,
                timestamp = prefsInteractor.getActivityReminderDoNotDisturbStart(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    fun onActivityReminderDoNotDisturbEndClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.ACTIVITY_REMINDER_DND_END_DIALOG_TAG,
                timestamp = prefsInteractor.getActivityReminderDoNotDisturbEnd(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    fun onDurationSet(tag: String?, duration: Long) {
        when (tag) {
            SettingsViewModel.INACTIVITY_DURATION_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setInactivityReminderDuration(duration)
                updateInactivityReminderViewData()
                notificationInactivityInteractor.cancel()
                notificationInactivityInteractor.checkAndSchedule()
                checkExactAlarmPermissionInteractor.execute()
            }
            SettingsViewModel.ACTIVITY_DURATION_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setActivityReminderDuration(duration)
                updateActivityReminderViewData()
                notificationActivityInteractor.cancel()
                notificationActivityInteractor.checkAndSchedule()
                checkExactAlarmPermissionInteractor.execute()
            }
        }
    }

    fun onDurationDisabled(tag: String?) {
        when (tag) {
            SettingsViewModel.INACTIVITY_DURATION_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setInactivityReminderDuration(0)
                updateInactivityReminderViewData()
                notificationInactivityInteractor.cancel()
            }

            SettingsViewModel.ACTIVITY_DURATION_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setActivityReminderDuration(0)
                updateActivityReminderViewData()
                notificationActivityInteractor.cancel()
            }
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = delegateScope.launch {
        when (tag) {
            SettingsViewModel.INACTIVITY_REMINDER_DND_START_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setInactivityReminderDoNotDisturbStart(newValue)
                updateInactivityReminderDndStartViewData()
                notificationInactivityInteractor.cancel()
                notificationInactivityInteractor.checkAndSchedule()
            }

            SettingsViewModel.INACTIVITY_REMINDER_DND_END_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setInactivityReminderDoNotDisturbEnd(newValue)
                updateInactivityReminderDndEndViewData()
                notificationInactivityInteractor.cancel()
                notificationInactivityInteractor.checkAndSchedule()
            }

            SettingsViewModel.ACTIVITY_REMINDER_DND_START_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setActivityReminderDoNotDisturbStart(newValue)
                updateActivityReminderDndStartViewData()
                notificationActivityInteractor.cancel()
                notificationActivityInteractor.checkAndSchedule()
            }

            SettingsViewModel.ACTIVITY_REMINDER_DND_END_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setActivityReminderDoNotDisturbEnd(newValue)
                updateActivityReminderDndEndViewData()
                notificationActivityInteractor.cancel()
                notificationActivityInteractor.checkAndSchedule()
            }
        }
    }

    private suspend fun updateInactivityReminderViewData() {
        val data = loadInactivityReminderViewData()
        inactivityReminderViewData.set(data)
    }

    private suspend fun loadInactivityReminderViewData(): SettingsDurationViewData {
        return prefsInteractor.getInactivityReminderDuration()
            .let(settingsMapper::toDurationViewData)
    }

    private suspend fun updateInactivityReminderDndStartViewData() {
        val data = loadInactivityReminderDndStartViewData()
        inactivityReminderDndStartViewData.set(data)
    }

    private suspend fun loadInactivityReminderDndStartViewData(): String {
        val shift = prefsInteractor.getInactivityReminderDoNotDisturbStart()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        return settingsMapper.toStartOfDayText(shift, useMilitaryTime)
    }

    private suspend fun updateInactivityReminderDndEndViewData() {
        val data = loadInactivityReminderDndEndViewData()
        inactivityReminderDndEndViewData.set(data)
    }

    private suspend fun loadInactivityReminderDndEndViewData(): String {
        val shift = prefsInteractor.getInactivityReminderDoNotDisturbEnd()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        return settingsMapper.toStartOfDayText(shift, useMilitaryTime)
    }

    private suspend fun updateActivityReminderViewData() {
        val data = loadActivityReminderViewData()
        activityReminderViewData.set(data)
    }

    private suspend fun loadActivityReminderViewData(): SettingsDurationViewData {
        return prefsInteractor.getActivityReminderDuration()
            .let(settingsMapper::toDurationViewData)
    }

    private suspend fun updateActivityReminderDndStartViewData() {
        val data = loadActivityReminderDndStartViewData()
        activityReminderDndStartViewData.set(data)
    }

    private suspend fun loadActivityReminderDndStartViewData(): String {
        val shift = prefsInteractor.getActivityReminderDoNotDisturbStart()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        return settingsMapper.toStartOfDayText(shift, useMilitaryTime)
    }

    private suspend fun updateActivityReminderDndEndViewData() {
        val data = loadActivityReminderDndEndViewData()
        activityReminderDndEndViewData.set(data)
    }

    private suspend fun loadActivityReminderDndEndViewData(): String {
        val shift = prefsInteractor.getActivityReminderDoNotDisturbEnd()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        return settingsMapper.toStartOfDayText(shift, useMilitaryTime)
    }
}