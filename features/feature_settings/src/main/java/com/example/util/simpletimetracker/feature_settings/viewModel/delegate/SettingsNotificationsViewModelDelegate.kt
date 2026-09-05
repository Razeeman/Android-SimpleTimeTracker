package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.interactor.CheckExactAlarmPermissionInteractor
import com.example.util.simpletimetracker.core.interactor.CheckNotificationsPermissionInteractor
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivityInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationInactivityInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsNotificationsViewDataInteractor
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsOpenDateTimeDialogRouter
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.model.OptionsContent
import com.example.util.simpletimetracker.feature_settings.model.SettingsDialogTags
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.action.OpenSystemSettings
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.RemindersParams
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
    private val settingsOpenDateTimeDialogRouter: SettingsOpenDateTimeDialogRouter,
) : SettingsDelegate, ViewModelDelegate() {

    private var parent: SettingsParent? = null
    private var isCollapsed: Boolean = true

    override fun init(parent: SettingsParent) {
        this.parent = parent
    }

    override suspend fun getViewData(): SettingsDelegate.ViewData {
        return SettingsDelegate.ViewData(
            key = Companion,
            data = settingsNotificationsViewDataInteractor.execute(isCollapsed = isCollapsed),
        )
    }

    override suspend fun getSheetViewData(content: OptionsContent): List<ViewHolderType>? {
        return when (content) {
            OptionsContent.InactivityReminder -> {
                settingsNotificationsViewDataInteractor.getInactivityReminderOptionsViewData()
            }
            OptionsContent.ActivityReminder -> {
                settingsNotificationsViewDataInteractor.getActivityReminderOptionsViewData()
            }
            else -> null
        }
    }

    override fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.NotificationsCollapse -> onCollapseClick()
            SettingsBlock.NotificationsInactivity -> onInactivityReminderClicked()
            SettingsBlock.NotificationsInactivityOptions -> onInactivityReminderOptionsClicked()
            SettingsBlock.NotificationsActivity -> onActivityReminderClicked()
            SettingsBlock.NotificationsActivityOptions -> onActivityReminderOptionsClicked()
            SettingsBlock.NotificationsReminders -> router.navigate(RemindersParams)
            SettingsBlock.NotificationsInactivityDoNotDisturbStart -> onInactivityReminderDoNotDisturbStartClicked()
            SettingsBlock.NotificationsInactivityDoNotDisturbEnd -> onInactivityReminderDoNotDisturbEndClicked()
            SettingsBlock.NotificationsActivityDoNotDisturbStart -> onActivityReminderDoNotDisturbStartClicked()
            SettingsBlock.NotificationsActivityDoNotDisturbEnd -> onActivityReminderDoNotDisturbEndClicked()
            SettingsBlock.NotificationsSystemSettings -> onSystemSettingsClicked()
            SettingsBlock.NotificationsShow -> onShowNotificationsClicked()
            SettingsBlock.NotificationsShowControls -> onShowNotificationsControlsClicked()
            SettingsBlock.NotificationsShowEvenWithNoTimers -> onShowNotificationsEvenWithNoTimersClicked()
            SettingsBlock.NotificationsInactivityRecurrent -> onInactivityReminderRecurrentClicked()
            SettingsBlock.NotificationsActivityRecurrent -> onActivityReminderRecurrentClicked()
            else -> {
                // Do nothing
            }
        }
    }

    override fun onDurationSet(tag: String?, duration: Long) {
        onDurationSetDelegate(tag, duration)
    }

    override fun onDurationDisabled(tag: String?) {
        onDurationDisabledDelegate(tag)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        onDateTimeSetDelegate(timestamp, tag)
    }

    override fun onDayOfWeekClicked(block: SettingsBlock, data: DayOfWeekViewData) {
        when (block) {
            SettingsBlock.NotificationsInactivityDaysOfWeek -> updateReminderDaysOfWeek(
                dayOfWeek = data.dayOfWeek,
                getDaysOfWeek = prefsInteractor::getInactivityReminderDaysOfWeek,
                setDaysOfWeek = prefsInteractor::setInactivityReminderDaysOfWeek,
                reschedule = externalViewsInteractor::onInactivityReminderChange,
            )
            SettingsBlock.NotificationsActivityDaysOfWeek -> updateReminderDaysOfWeek(
                dayOfWeek = data.dayOfWeek,
                getDaysOfWeek = prefsInteractor::getActivityReminderDaysOfWeek,
                setDaysOfWeek = prefsInteractor::setActivityReminderDaysOfWeek,
                reschedule = externalViewsInteractor::onActivityReminderChange,
            )
            else -> Unit
        }
    }

    override fun collapse() {
        isCollapsed = true
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

    private fun onShowNotificationsEvenWithNoTimersClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowNotificationEvenWithNoTimers()
            prefsInteractor.setShowNotificationEvenWithNoTimers(newValue)
            parent?.updateContent()
            externalViewsInteractor.onShowNotificationsEvenWithNoTimersChange()
        }
    }

    private fun onInactivityReminderClicked() = delegateScope.launch {
        val duration = prefsInteractor.getInactivityReminderDuration()

        fun openDialog() {
            DurationDialogParams(
                tag = SettingsDialogTags.INACTIVITY_DURATION_DIALOG_TAG,
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

    private fun onInactivityReminderOptionsClicked() {
        parent?.openOptions(OptionsContent.InactivityReminder)
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
            settingsOpenDateTimeDialogRouter.openDateTimeDialog(
                tag = SettingsDialogTags.INACTIVITY_REMINDER_DND_START_DIALOG_TAG,
                timestamp = prefsInteractor.getInactivityReminderDoNotDisturbStart(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    private fun onInactivityReminderDoNotDisturbEndClicked() {
        delegateScope.launch {
            settingsOpenDateTimeDialogRouter.openDateTimeDialog(
                tag = SettingsDialogTags.INACTIVITY_REMINDER_DND_END_DIALOG_TAG,
                timestamp = prefsInteractor.getInactivityReminderDoNotDisturbEnd(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    private fun onActivityReminderClicked() = delegateScope.launch {
        val duration = prefsInteractor.getActivityReminderDuration()

        fun openDialog() {
            DurationDialogParams(
                tag = SettingsDialogTags.ACTIVITY_DURATION_DIALOG_TAG,
                value = DurationDialogParams.Value.DurationSeconds(duration),
            ).let(router::navigate)
        }

        if (duration > 0) {
            openDialog()
        } else {
            checkNotificationsPermissionInteractor.execute(onEnabled = ::openDialog)
        }
    }

    private fun onActivityReminderOptionsClicked() {
        parent?.openOptions(OptionsContent.ActivityReminder)
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
            settingsOpenDateTimeDialogRouter.openDateTimeDialog(
                tag = SettingsDialogTags.ACTIVITY_REMINDER_DND_START_DIALOG_TAG,
                timestamp = prefsInteractor.getActivityReminderDoNotDisturbStart(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    private fun onActivityReminderDoNotDisturbEndClicked() {
        delegateScope.launch {
            settingsOpenDateTimeDialogRouter.openDateTimeDialog(
                tag = SettingsDialogTags.ACTIVITY_REMINDER_DND_END_DIALOG_TAG,
                timestamp = prefsInteractor.getActivityReminderDoNotDisturbEnd(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    private fun onDurationSetDelegate(tag: String?, duration: Long) {
        when (tag) {
            SettingsDialogTags.INACTIVITY_DURATION_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setInactivityReminderDuration(duration)
                parent?.updateContent()
                externalViewsInteractor.onInactivityReminderChange()
                checkExactAlarmPermissionInteractor.execute()
            }
            SettingsDialogTags.ACTIVITY_DURATION_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setActivityReminderDuration(duration)
                parent?.updateContent()
                externalViewsInteractor.onActivityReminderChange()
                checkExactAlarmPermissionInteractor.execute()
            }
        }
    }

    private fun onDurationDisabledDelegate(tag: String?) {
        when (tag) {
            SettingsDialogTags.INACTIVITY_DURATION_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setInactivityReminderDuration(0)
                parent?.updateContent()
                notificationInactivityInteractor.cancel()
            }

            SettingsDialogTags.ACTIVITY_DURATION_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setActivityReminderDuration(0)
                parent?.updateContent()
                externalViewsInteractor.onActivityReminderChange()
            }
        }
    }

    private fun onDateTimeSetDelegate(timestamp: Long, tag: String?) = delegateScope.launch {
        when (tag) {
            SettingsDialogTags.INACTIVITY_REMINDER_DND_START_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setInactivityReminderDoNotDisturbStart(newValue)
                parent?.updateContent()
                externalViewsInteractor.onInactivityReminderChange()
            }

            SettingsDialogTags.INACTIVITY_REMINDER_DND_END_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setInactivityReminderDoNotDisturbEnd(newValue)
                parent?.updateContent()
                externalViewsInteractor.onInactivityReminderChange()
            }

            SettingsDialogTags.ACTIVITY_REMINDER_DND_START_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setActivityReminderDoNotDisturbStart(newValue)
                parent?.updateContent()
                externalViewsInteractor.onActivityReminderChange()
            }

            SettingsDialogTags.ACTIVITY_REMINDER_DND_END_DIALOG_TAG -> {
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

    private fun updateReminderDaysOfWeek(
        dayOfWeek: DayOfWeek,
        getDaysOfWeek: suspend () -> Set<DayOfWeek>,
        setDaysOfWeek: suspend (Set<DayOfWeek>) -> Unit,
        reschedule: suspend () -> Unit,
    ) = delegateScope.launch {
        val selectedDays = getDaysOfWeek().toMutableSet()
        // Disallow deselecting all days.
        if (dayOfWeek in selectedDays && selectedDays.size == 1) return@launch

        selectedDays.addOrRemove(dayOfWeek)
        setDaysOfWeek(selectedDays)
        parent?.updateContent()
        reschedule()
    }

    companion object : SettingsDelegate.Key
}