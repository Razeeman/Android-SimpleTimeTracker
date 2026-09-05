package com.example.util.simpletimetracker.feature_reminders.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.CheckExactAlarmPermissionInteractor
import com.example.util.simpletimetracker.core.interactor.CheckNotificationsPermissionInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.activityReminder.interactor.ActivityRemindersDataUpdateInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderOccurrenceCalculator
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledRemindersDataUpdateInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_reminders.interactor.RemindersViewDataInteractor
import com.example.util.simpletimetracker.feature_reminders.viewData.ReminderViewData
import com.example.util.simpletimetracker.feature_reminders.viewData.ActivityReminderViewData
import com.example.util.simpletimetracker.feature_reminders.viewData.RemindersButtonViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeReminderParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeActivityReminderParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.TimeZone
import javax.inject.Inject
import com.example.util.simpletimetracker.core.R as coreR

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val scheduledReminderInteractor: ScheduledReminderInteractor,
    private val remindersViewDataInteractor: RemindersViewDataInteractor,
    private val checkNotificationsPermissionInteractor: CheckNotificationsPermissionInteractor,
    private val checkExactAlarmPermissionInteractor: CheckExactAlarmPermissionInteractor,
    private val scheduledRemindersDataUpdateInteractor: ScheduledRemindersDataUpdateInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val occurrenceCalculator: ScheduledReminderOccurrenceCalculator,
    private val activityRemindersDataUpdateInteractor: ActivityRemindersDataUpdateInteractor,
) : BaseViewModel() {

    val viewData: LiveData<List<ViewHolderType>> by lazySuspend {
        listOf(LoaderViewData()).also { updateViewData() }
    }

    private var loadJob: Job? = null

    init {
        subscribeToUpdates()
    }

    fun onVisible() {
        updateViewData()
    }

    fun onAddClick(item: ButtonViewData) {
        if (item.id !is RemindersButtonViewData) return
        router.navigate(ChangeReminderParams.New)
    }

    fun onReminderClick(item: ReminderViewData) {
        router.navigate(ChangeReminderParams.Change(item.id))
    }

    fun onActivityReminderClick(item: ActivityReminderViewData) {
        router.navigate(ChangeActivityReminderParams(item.activityId))
    }

    fun onEnabledClick(item: ReminderViewData) {
        if (item.enabled) {
            setEnabled(id = item.id, enabled = false)
        } else {
            viewModelScope.launch {
                if (!canEnable(item.id)) {
                    showMessage(coreR.string.change_reminder_future_required)
                    return@launch
                }
                checkNotificationsPermissionInteractor.execute(
                    onEnabled = {
                        setEnabled(id = item.id, enabled = true)
                        checkExactAlarmPermissionInteractor.execute()
                    },
                    onDisabled = ::updateViewData,
                )
            }
        }
    }

    private fun setEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            scheduledReminderInteractor.setEnabled(id = id, enabled = enabled)
            updateViewData()
        }
    }

    private suspend fun canEnable(id: Long): Boolean {
        val schedule = scheduledReminderInteractor.get(id)?.schedule
        if (schedule !is ScheduledReminder.Schedule.OneTime) return true

        return occurrenceCalculator.calculateNext(
            schedule = schedule,
            nowTimestamp = currentTimestampProvider.get(),
            timeZone = TimeZone.getDefault(),
            catchUpOverdueOneTime = false,
        ) != null
    }

    private fun showMessage(stringResId: Int) {
        val params = SnackBarParams(
            message = resourceRepo.getString(stringResId),
            duration = SnackBarParams.Duration.Short,
        )
        router.show(params)
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            scheduledRemindersDataUpdateInteractor.dataUpdated.collect {
                updateViewData()
            }
        }
        viewModelScope.launch {
            activityRemindersDataUpdateInteractor.dataUpdated.collect {
                updateViewData()
            }
        }
    }

    private fun updateViewData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val data = loadViewData()
            delayLoad()
            viewData.set(data)
        }
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        return remindersViewDataInteractor.getViewData()
    }
}
