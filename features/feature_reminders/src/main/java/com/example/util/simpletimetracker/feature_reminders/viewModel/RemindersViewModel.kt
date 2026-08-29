package com.example.util.simpletimetracker.feature_reminders.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.CheckExactAlarmPermissionInteractor
import com.example.util.simpletimetracker.core.interactor.CheckNotificationsPermissionInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledRemindersDataUpdateInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_reminders.interactor.RemindersViewDataInteractor
import com.example.util.simpletimetracker.feature_reminders.viewData.ReminderViewData
import com.example.util.simpletimetracker.feature_reminders.viewData.RemindersButtonViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeReminderParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val router: Router,
    private val scheduledReminderInteractor: ScheduledReminderInteractor,
    private val remindersViewDataInteractor: RemindersViewDataInteractor,
    private val checkNotificationsPermissionInteractor: CheckNotificationsPermissionInteractor,
    private val checkExactAlarmPermissionInteractor: CheckExactAlarmPermissionInteractor,
    private val scheduledRemindersDataUpdateInteractor: ScheduledRemindersDataUpdateInteractor,
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

    fun onEnabledClick(item: ReminderViewData) {
        if (item.enabled) {
            setEnabled(id = item.id, enabled = false)
        } else {
            checkNotificationsPermissionInteractor.execute(
                onEnabled = {
                    setEnabled(id = item.id, enabled = true)
                    checkExactAlarmPermissionInteractor.execute()
                },
                onDisabled = ::updateViewData,
            )
        }
    }

    private fun setEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            scheduledReminderInteractor.setEnabled(id = id, enabled = enabled)
            updateViewData()
        }
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            scheduledRemindersDataUpdateInteractor.dataUpdated.collect {
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
