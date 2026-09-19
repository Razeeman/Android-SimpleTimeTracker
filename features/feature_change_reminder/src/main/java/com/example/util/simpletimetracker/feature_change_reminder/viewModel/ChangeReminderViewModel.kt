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
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.extension.toLocalDateTime
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledRemindersDataUpdateInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.domain.utils.LocalDateMapper
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_change_reminder.R
import com.example.util.simpletimetracker.feature_change_reminder.interactor.ChangeReminderViewDataInteractor
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
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import com.example.util.simpletimetracker.navigation.params.screen.ReminderConditionTargetType
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val localDateMapper: LocalDateMapper,
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
    private var selectedTargetName: String? = null
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
        selectedTargetName = null
        updateViewData()
    }

    fun onConditionSelected(position: Int) {
        val type = changeReminderViewDataInteractor.mapCondition(position) ?: return
        when (type) {
            ConditionType.ALWAYS -> {
                if (type == editor.conditionType) return
                editor.selectCondition(type)
                selectedTargetName = null
            }
            ConditionType.NOT_TRACKED -> {
                openTargetTypeSelection()
            }
        }
        updateViewData()
    }

    fun onConditionTargetClick() {
        if (editor.conditionType != ConditionType.NOT_TRACKED) return
        openTargetTypeSelection()
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
        val timestamp = localDateMapper.resolveDateTime(
            dateEpochDay = editor.date,
            timeOfDayMillis = editor.timeOfDayMillis,
            timeZone = TimeZone.getDefault(),
        ) ?: currentTimestampProvider.get()
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
        val today = currentTimestampProvider.get().toLocalDateTime(timeZone).toLocalDate()
        val timestamp = localDateMapper.resolveDateTime(
            date = today,
            timeOfDayMillis = editor.timeOfDayMillis,
            timeZone = timeZone,
        ) ?: currentTimestampProvider.get()
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
        val dateTime = timestamp.toLocalDateTime(TimeZone.getDefault())
        when (tag) {
            DATE_TAG -> editor.date = dateTime.toLocalDate().toEpochDay()
            TIME_TAG -> editor.timeOfDayMillis = dateTime.toLocalTime().toMillisOfDay()
            DND_START_TAG -> editor.doNotDisturbStartMillis = dateTime.toLocalTime().toMillisOfDay()
            DND_END_TAG -> editor.doNotDisturbEndMillis = dateTime.toLocalTime().toMillisOfDay()
            else -> return
        }
        updateViewData()
    }

    fun onIntervalClick() {
        DurationDialogParams(
            tag = INTERVAL_TAG,
            value = DurationDialogParams.Value.DurationSeconds(editor.intervalSeconds),
            hideDisableButton = true,
            showSeconds = false,
        ).let(router::navigate)
    }

    fun onDurationSet(durationSeconds: Long, tag: String?) {
        if (tag != INTERVAL_TAG) return
        editor.intervalSeconds = durationSeconds
        updateViewData()
    }

    fun onDoNotDisturbStartClick() {
        openTimeDialog(DND_START_TAG, editor.doNotDisturbStartMillis)
    }

    fun onDoNotDisturbEndClick() {
        openTimeDialog(DND_END_TAG, editor.doNotDisturbEndMillis)
    }

    fun onTargetTypeSelected(type: ReminderConditionTargetType) {
        val currentTarget = editor.conditionTarget
        val (tag, title, selectionType) = when (type) {
            ReminderConditionTargetType.Activity -> Triple(
                ACTIVITY_TAG,
                R.string.activity_hint,
                TypesSelectionDialogParams.Type.Activity,
            )
            ReminderConditionTargetType.Category -> Triple(
                CATEGORY_TAG,
                R.string.category_hint,
                TypesSelectionDialogParams.Type.Category,
            )
            ReminderConditionTargetType.Tag -> Triple(
                TAG_TAG,
                R.string.record_tag_hint,
                TypesSelectionDialogParams.Type.Tag.All,
            )
        }
        val selectedId = when {
            type is ReminderConditionTargetType.Activity &&
                currentTarget is ScheduledReminder.Condition.Target.Activity -> currentTarget.id
            type is ReminderConditionTargetType.Category &&
                currentTarget is ScheduledReminder.Condition.Target.Category -> currentTarget.id
            type is ReminderConditionTargetType.Tag &&
                currentTarget is ScheduledReminder.Condition.Target.Tag -> currentTarget.id
            else -> null
        }
        TypesSelectionDialogParams(
            tag = tag,
            title = resourceRepo.getString(title),
            subtitle = "",
            type = selectionType,
            selectedTypeIds = listOfNotNull(selectedId),
            selectedTagValues = emptyList(),
            selectedTagValueOnStart = emptyList(),
            isMultiSelectAvailable = false,
            idsShouldBeVisible = listOfNotNull(selectedId),
            showHints = false,
            allowTagValueSelection = false,
        ).let(router::navigate)
    }

    fun onTargetSelected(tag: String, ids: List<Long>) = viewModelScope.launch {
        val id = ids.firstOrNull() ?: return@launch
        val targetAndName = when (tag) {
            ACTIVITY_TAG -> ScheduledReminder.Condition.Target.Activity(id)
            CATEGORY_TAG -> ScheduledReminder.Condition.Target.Category(id)
            TAG_TAG -> ScheduledReminder.Condition.Target.Tag(id)
            else -> null
        }?.let {
            val name = loadTargetName(it) ?: return@let null
            it to name
        } ?: return@launch
        selectedTargetName = targetAndName.second
        editor.selectTarget(
            target = targetAndName.first,
            prefill = {
                resourceRepo.getString(
                    R.string.change_reminder_message_prefill,
                    targetAndName.second,
                )
            },
        )
        updateViewData()
    }

    fun onSaveClick() = viewModelScope.launch {
        if (!controlsEnabled) return@launch
        val result = editor.validate(
            nowTimestamp = currentTimestampProvider.get(),
            localDateMapper = localDateMapper,
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
            ValidationError.INTERVAL_REQUIRED -> R.string.change_reminder_interval_required
        }
        snackBarMessageNavigationInteractor.showMessage(stringRes)
    }

    private fun LocalTime.toMillisOfDay(): Long {
        return TimeUnit.HOURS.toMillis(hour.toLong()) +
            TimeUnit.MINUTES.toMillis(minute.toLong())
    }

    private fun openTimeDialog(tag: String, timeOfDayMillis: Long) = viewModelScope.launch {
        val timeZone = TimeZone.getDefault()
        val date = currentTimestampProvider.get().toLocalDateTime(timeZone).toLocalDate()
        val timestamp = localDateMapper.resolveDateTime(
            date = date,
            timeOfDayMillis = timeOfDayMillis,
            timeZone = timeZone,
        ) ?: currentTimestampProvider.get()
        router.navigate(
            DateTimeDialogParams(
                tag = tag,
                timestamp = timestamp,
                type = DateTimeDialogType.TIME,
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            ),
        )
    }

    private fun openTargetTypeSelection() {
        val items = listOf(
            ReminderConditionTargetType.Activity to R.string.activity_hint,
            ReminderConditionTargetType.Category to R.string.category_hint,
            ReminderConditionTargetType.Tag to R.string.record_tag_hint,
        ).map { (id, textRes) ->
            OptionsListParams.Item(
                id = id,
                text = resourceRepo.getString(textRes),
                icon = null,
            )
        }
        router.navigate(OptionsListParams(items))
    }

    private suspend fun loadTargetName(
        target: ScheduledReminder.Condition.Target?,
    ): String? {
        return when (target) {
            is ScheduledReminder.Condition.Target.Activity ->
                recordTypeInteractor.get(target.id)?.name
            is ScheduledReminder.Condition.Target.Category ->
                categoryInteractor.get(target.id)?.name
            is ScheduledReminder.Condition.Target.Tag ->
                recordTagInteractor.get(target.id)?.name
            null -> null
        }
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
        selectedTargetName = loadTargetName(editor.conditionTarget)
        if (editor.conditionType == ConditionType.NOT_TRACKED && selectedTargetName == null) {
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
            selectedTargetName = selectedTargetName,
            controlsEnabled = controlsEnabled,
        )
    }

    private companion object {
        const val DATE_TAG = "change_reminder_date"
        const val TIME_TAG = "change_reminder_time"
        const val INTERVAL_TAG = "change_reminder_interval"
        const val DND_START_TAG = "change_reminder_dnd_start"
        const val DND_END_TAG = "change_reminder_dnd_end"
        const val ACTIVITY_TAG = "change_reminder_activity"
        const val CATEGORY_TAG = "change_reminder_category"
        const val TAG_TAG = "change_reminder_tag"
    }
}
