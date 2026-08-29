package com.example.util.simpletimetracker.feature_change_reminder.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.CheckExactAlarmPermissionInteractor
import com.example.util.simpletimetracker.core.interactor.CheckNotificationsPermissionInteractor
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderOccurrenceCalculator
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledRemindersDataUpdateInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_change_reminder.ChangeReminderViewDataInteractor
import com.example.util.simpletimetracker.feature_change_reminder.R
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor.ConditionType
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor.ValidationError
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor.ValidationResult
import com.example.util.simpletimetracker.feature_change_reminder.viewData.ChangeReminderViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.ChangeReminderParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ChangeReminderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val router: Router,
    private val scheduledReminderInteractor: ScheduledReminderInteractor,
    private val scheduledRemindersDataUpdateInteractor: ScheduledRemindersDataUpdateInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val occurrenceCalculator: ScheduledReminderOccurrenceCalculator,
    private val resourceRepo: ResourceRepo,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val checkNotificationsPermissionInteractor: CheckNotificationsPermissionInteractor,
    private val checkExactAlarmPermissionInteractor: CheckExactAlarmPermissionInteractor,
    private val changeReminderViewDataInteractor: ChangeReminderViewDataInteractor,
) : BaseViewModel() {

    private val extra: ChangeReminderParams = savedStateHandle[ARGS_PARAMS]
        ?: ChangeReminderParams.New

    val viewData: LiveData<ChangeReminderViewData> = MutableLiveData()

    private var editor: ChangeReminderEditor = ChangeReminderEditor.new(
        nowTimestamp = currentTimestampProvider.get(),
    )
    private var updateJob: Job? = null
    private var selectedActivity: RecordType? = null
    private var controlsEnabled = true

    init {
        viewModelScope.launch {
            initializeData()
            updateViewData()
        }
    }

    fun onMessageChanged(value: String) {
        if (value == editor.message) return
        editor.onMessageChanged(value)
        updateViewData()
    }

    fun onScheduleSelected(position: Int) {
        val type = changeReminderViewDataInteractor.mapSchedule(position) ?: return
        if (type == editor.scheduleType) return
        editor.selectSchedule(type)
        selectedActivity = null
        updateViewData()
    }

    fun onConditionSelected(position: Int) {
        val type = changeReminderViewDataInteractor.mapCondition(position) ?: return
        if (type == editor.conditionType) return
        when (type) {
            ConditionType.ALWAYS -> {
                editor.selectCondition(type)
                selectedActivity = null
            }
            ConditionType.NOT_TRACKED -> {
                openActivitySelection()
            }
        }
        updateViewData()
    }

    fun onDayClick(data: DayOfWeekViewData) {
        if (data.dayOfWeek in editor.daysOfWeek && editor.daysOfWeek.size == 1) return
        editor.toggleDay(data.dayOfWeek)
        updateViewData()
    }

    fun onDayOfMonthSelected(position: Int) {
        val value = changeReminderViewDataInteractor.mapDayOfMonth(position) ?: return
        if (value == editor.dayOfMonth) return
        editor.dayOfMonth = value
        updateViewData()
    }

    fun onDateClick() = viewModelScope.launch {
        val timestamp = occurrenceCalculator.resolveLocalDateTime(
            dateEpochDay = editor.oneTimeDate,
            timeOfDayMillis = editor.timeOfDayMillis,
            timeZone = TimeZone.getDefault(),
        ).takeUnless { it == 0L } ?: currentTimestampProvider.get()
        router.navigate(
            DateTimeDialogParams(
                tag = DATE_TAG,
                timestamp = timestamp,
                type = DateTimeDialogType.DATE,
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
                firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
            ),
        )
    }

    fun onTimeClick() = viewModelScope.launch {
        val timeZone = TimeZone.getDefault()
        val today = Instant.ofEpochMilli(currentTimestampProvider.get())
            .atZone(timeZone.toZoneId()).toLocalDate()
        val timestamp = occurrenceCalculator.resolveLocalDateTime(
            dateEpochDay = today.toEpochDay(),
            timeOfDayMillis = editor.timeOfDayMillis,
            timeZone = timeZone,
        ).takeUnless { it == 0L } ?: currentTimestampProvider.get()
        router.navigate(
            DateTimeDialogParams(
                tag = TIME_TAG,
                timestamp = timestamp,
                type = DateTimeDialogType.TIME,
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            ),
        )
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        val dateTime = Instant.ofEpochMilli(timestamp).atZone(TimeZone.getDefault().toZoneId())
        when (tag) {
            DATE_TAG -> editor.oneTimeDate = dateTime.toLocalDate().toEpochDay()
            TIME_TAG -> editor.timeOfDayMillis = dateTime.toLocalTime().toMillisOfDay()
        }
        updateViewData()
    }

    fun onActivityClick() {
        openActivitySelection()
    }

    private fun openActivitySelection() {
        TypesSelectionDialogParams(
            tag = ACTIVITY_TAG,
            title = resourceRepo.getString(R.string.change_record_message_choose_type),
            subtitle = "",
            type = TypesSelectionDialogParams.Type.Activity,
            selectedTypeIds = listOfNotNull(editor.activityId),
            selectedTagValues = emptyList(),
            selectedTagValueOnStart = emptyList(),
            isMultiSelectAvailable = false,
            idsShouldBeVisible = listOfNotNull(editor.activityId),
            showHints = false,
            allowTagValueSelection = false,
        ).let(router::navigate)
    }

    fun onActivitySelected(tag: String, ids: List<Long>) = viewModelScope.launch {
        if (tag != ACTIVITY_TAG) return@launch
        val activity = ids.firstOrNull()?.let { recordTypeInteractor.get(it) } ?: return@launch
        selectedActivity = activity
        editor.selectCondition(ConditionType.NOT_TRACKED)
        editor.selectActivity(
            type = activity,
            prefill = { resourceRepo.getString(R.string.change_reminder_message_prefill, it) },
        )
        updateViewData()
    }

    fun onSaveClick() = viewModelScope.launch {
        if (!controlsEnabled) return@launch
        val result = editor.validate(
            nowTimestamp = currentTimestampProvider.get(),
            occurrenceCalculator = occurrenceCalculator,
        )
        when (result) {
            is ValidationResult.Error -> showValidationError(result.error)
            is ValidationResult.Valid -> saveWithPermission(result.reminder)
        }
    }

    fun onDeleteClick() {
        if (editor.id == 0L || !controlsEnabled) return
        controlsEnabled = false
        updateViewData()
        viewModelScope.launch {
            scheduledReminderInteractor.remove(editor.id)
            scheduledRemindersDataUpdateInteractor.send()
            snackBarMessageNavigationInteractor.showMessage(R.string.change_reminder_removed)
            router.back()
        }
    }

    private fun saveWithPermission(reminder: ScheduledReminder) {
        controlsEnabled = false
        updateViewData()
        if (!reminder.enabled) {
            persist(reminder)
            return
        }
        checkNotificationsPermissionInteractor.execute(
            onEnabled = { persist(reminder) },
            onDisabled = { persist(reminder.copy(enabled = false)) },
        )
    }

    private fun persist(reminder: ScheduledReminder) = viewModelScope.launch {
        scheduledReminderInteractor.save(reminder)
        scheduledRemindersDataUpdateInteractor.send()
        if (reminder.enabled) checkExactAlarmPermissionInteractor.execute()
        router.back()
    }

    private fun showValidationError(error: ValidationError) {
        val stringRes = when (error) {
            ValidationError.MESSAGE_REQUIRED -> R.string.change_reminder_message_required
            ValidationError.FUTURE_REQUIRED -> R.string.change_reminder_future_required
        }
        snackBarMessageNavigationInteractor.showMessage(stringRes)
    }

    private fun LocalTime.toMillisOfDay(): Long {
        return TimeUnit.HOURS.toMillis(hour.toLong()) +
            TimeUnit.MINUTES.toMillis(minute.toLong())
    }

    private suspend fun initializeData() {
        val reminder = (extra as? ChangeReminderParams.Change)
            ?.let { scheduledReminderInteractor.get(it.id) }
        if (extra is ChangeReminderParams.Change && reminder == null) {
            router.back()
            return
        }
        editor = if (reminder != null) {
            ChangeReminderEditor.from(currentTimestampProvider.get(), reminder)
        } else {
            ChangeReminderEditor.new(currentTimestampProvider.get())
        }
        selectedActivity = editor.activityId?.let { recordTypeInteractor.get(it) }
        if (editor.conditionType == ConditionType.NOT_TRACKED && selectedActivity == null) {
            editor.selectCondition(ConditionType.ALWAYS)
        }
    }

    private fun updateViewData() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            viewData.set(loadViewData())
        }
    }

    private suspend fun loadViewData(): ChangeReminderViewData {
        return changeReminderViewDataInteractor.getViewData(
            editor = editor,
            selectedActivity = selectedActivity,
            controlsEnabled = controlsEnabled,
        )
    }

    private companion object {
        const val DATE_TAG = "change_reminder_date"
        const val TIME_TAG = "change_reminder_time"
        const val ACTIVITY_TAG = "change_reminder_activity"
    }
}
