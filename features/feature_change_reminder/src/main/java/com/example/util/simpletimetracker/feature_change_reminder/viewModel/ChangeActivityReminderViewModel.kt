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
import com.example.util.simpletimetracker.domain.activityReminder.interactor.ActivityReminderOverrideInteractor
import com.example.util.simpletimetracker.domain.activityReminder.interactor.ActivityRemindersDataUpdateInteractor
import com.example.util.simpletimetracker.domain.activityReminder.model.ActivityReminderOverride
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.toLocalDateTime
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivityInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.utils.LocalDateMapper
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_change_reminder.R
import com.example.util.simpletimetracker.feature_change_reminder.interactor.ChangeActivityReminderViewDataInteractor
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeActivityReminderEditor
import com.example.util.simpletimetracker.feature_change_reminder.viewData.ChangeActivityReminderViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.ChangeActivityReminderParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class ChangeActivityReminderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val router: Router,
    private val activityReminderOverrideInteractor: ActivityReminderOverrideInteractor,
    private val activityRemindersDataUpdateInteractor: ActivityRemindersDataUpdateInteractor,
    private val notificationActivityInteractor: NotificationActivityInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val localDateMapper: LocalDateMapper,
    private val checkNotificationsPermissionInteractor: CheckNotificationsPermissionInteractor,
    private val checkExactAlarmPermissionInteractor: CheckExactAlarmPermissionInteractor,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val resourceRepo: ResourceRepo,
    private val viewDataInteractor: ChangeActivityReminderViewDataInteractor,
) : BaseViewModel() {

    private val params: ChangeActivityReminderParams = savedStateHandle[ARGS_PARAMS]
        ?: ChangeActivityReminderParams.New

    val viewData: LiveData<ChangeActivityReminderViewData> = MutableLiveData()

    private var activity: RecordType? = null
    private var editor: ChangeActivityReminderEditor? = null
    private var updateJob: Job? = null
    private var controlsEnabled: Boolean = true

    init {
        viewModelScope.launch {
            initialize()
            updateViewData()
        }
    }

    fun onModeSelected(position: Int) {
        val mode = viewDataInteractor.mapMode(position) ?: return
        val editor = editor ?: return
        if (editor.mode == mode) return
        editor.selectMode(mode)
        updateViewData()
    }

    fun onActivityClick() {
        // Block activity change after rule is created, because it serves as id in database,
        // need to remove data on activity change etc.
        if (params !is ChangeActivityReminderParams.New || !controlsEnabled) return
        viewModelScope.launch {
            val currentOverrides = activityReminderOverrideInteractor.getAll()
                .map(ActivityReminderOverride::activityId)

            TypesSelectionDialogParams(
                tag = ACTIVITY_TAG,
                title = resourceRepo.getString(R.string.change_record_message_choose_type),
                subtitle = "",
                type = TypesSelectionDialogParams.Type.Activity,
                selectedTypeIds = listOfNotNull(activity?.id),
                selectedTagValues = emptyList(),
                selectedTagValueOnStart = emptyList(),
                isMultiSelectAvailable = false,
                idsShouldBeVisible = listOfNotNull(activity?.id),
                excludedTypeIds = currentOverrides,
                showHints = false,
                allowTagValueSelection = false,
            ).let(router::navigate)
        }
    }

    fun onActivitySelected(tag: String, ids: List<Long>) = viewModelScope.launch {
        if (tag != ACTIVITY_TAG || params !is ChangeActivityReminderParams.New) return@launch
        activity = ids.firstOrNull()?.let { recordTypeInteractor.get(it) } ?: return@launch
        updateViewData()
    }

    fun onDurationClick() {
        val editor = editor ?: return
        DurationDialogParams(
            tag = DURATION_TAG,
            value = DurationDialogParams.Value.DurationSeconds(editor.durationSeconds),
            hideDisableButton = true,
            showSeconds = true,
        ).let(router::navigate)
    }

    fun onDurationSet(durationSeconds: Long, tag: String?) {
        if (tag != DURATION_TAG) return
        editor?.durationSeconds = durationSeconds
        updateViewData()
    }

    fun onRecurrentChanged() {
        val editor = editor ?: return
        editor.recurrent = !editor.recurrent
        updateViewData()
    }

    fun onDayClick(data: DayOfWeekViewData) {
        val editor = editor ?: return
        if (data.dayOfWeek in editor.daysOfWeek && editor.daysOfWeek.size == 1) return
        editor.toggleDay(data.dayOfWeek)
        updateViewData()
    }

    fun onDoNotDisturbStartClick() {
        openTimeDialog(DND_START_TAG, editor?.doNotDisturbStartMillis ?: return)
    }

    fun onDoNotDisturbEndClick() {
        openTimeDialog(DND_END_TAG, editor?.doNotDisturbEndMillis ?: return)
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        val millis = timestamp.toLocalDateTime(TimeZone.getDefault()).toLocalTime().toMillisOfDay()
        when (tag) {
            DND_START_TAG -> editor?.doNotDisturbStartMillis = millis
            DND_END_TAG -> editor?.doNotDisturbEndMillis = millis
            else -> return
        }
        updateViewData()
    }

    fun onSaveClick() {
        val editor = editor ?: return
        if (!controlsEnabled) return
        if (activity == null) {
            snackBarMessageNavigationInteractor.showMessage(
                R.string.change_record_message_choose_type,
            )
            return
        }
        if (editor.mode == ChangeActivityReminderEditor.Mode.CUSTOM && editor.durationSeconds <= 0L) {
            snackBarMessageNavigationInteractor.showMessage(
                R.string.activity_reminder_positive_duration_required,
            )
            return
        }

        controlsEnabled = false
        updateViewData()
        if (editor.mode == ChangeActivityReminderEditor.Mode.CUSTOM) {
            checkNotificationsPermissionInteractor.execute(
                onEnabled = { persist(requestExactAlarmPermission = true) },
                onDisabled = {
                    controlsEnabled = true
                    updateViewData()
                },
            )
        } else {
            persist(requestExactAlarmPermission = false)
        }
    }

    fun onDeleteClick() {
        val activityId = activity?.id ?: return
        if (params !is ChangeActivityReminderParams.Change || !controlsEnabled) return
        controlsEnabled = false
        updateViewData()
        viewModelScope.launch {
            val wasInherited = activityReminderOverrideInteractor.get(activityId) == null
            activityReminderOverrideInteractor.remove(activityId)
            activityRemindersDataUpdateInteractor.send()
            notificationActivityInteractor.onReminderOverrideChanged(
                activityId = activityId,
                wasInherited = wasInherited,
            )
            snackBarMessageNavigationInteractor.showMessage(R.string.change_reminder_removed)
            router.back()
        }
    }

    private fun persist(requestExactAlarmPermission: Boolean) = viewModelScope.launch {
        val editor = editor ?: return@launch
        val activityId = activity?.id ?: return@launch
        val data = editor.toOverride(activityId)

        val wasInherited = activityReminderOverrideInteractor.get(activityId) == null
        activityReminderOverrideInteractor.save(data)
        activityRemindersDataUpdateInteractor.send()
        notificationActivityInteractor.onReminderOverrideChanged(
            activityId = activityId,
            wasInherited = wasInherited,
        )
        if (requestExactAlarmPermission) checkExactAlarmPermissionInteractor.execute()
        router.back()
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

    private suspend fun initialize() {
        // Use global values.
        val defaultRule = ActivityReminderOverride.Rule(
            id = 0L,
            durationSeconds = prefsInteractor.getActivityReminderDuration(),
            recurrent = prefsInteractor.getActivityReminderRecurrent(),
            applicableDaysOfWeek = prefsInteractor.getActivityReminderDaysOfWeek()
                .ifEmpty { DayOfWeek.entries.toSet() },
            doNotDisturbStartMillis = prefsInteractor.getActivityReminderDoNotDisturbStart(),
            doNotDisturbEndMillis = prefsInteractor.getActivityReminderDoNotDisturbEnd(),
        )
        when (val params = params) {
            is ChangeActivityReminderParams.New -> {
                editor = ChangeActivityReminderEditor.new(defaultRule)
            }
            is ChangeActivityReminderParams.Change -> {
                val override = activityReminderOverrideInteractor.get(params.activityId)
                activity = recordTypeInteractor.get(params.activityId)
                if (activity == null || override == null) {
                    router.back()
                    return
                }
                editor = ChangeActivityReminderEditor.create(
                    override = override,
                    defaultRule = defaultRule,
                )
            }
        }
    }

    private fun updateViewData() {
        val editor = editor ?: return
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            viewData.set(loadViewData(editor))
        }
    }

    private suspend fun loadViewData(
        editor: ChangeActivityReminderEditor,
    ): ChangeActivityReminderViewData {
        return viewDataInteractor.getViewData(
            activity = activity,
            editor = editor,
            controlsEnabled = controlsEnabled,
            activitySelectionEnabled = params is ChangeActivityReminderParams.New,
            deleteVisible = params is ChangeActivityReminderParams.Change,
        )
    }

    private fun LocalTime.toMillisOfDay(): Long {
        return TimeUnit.HOURS.toMillis(hour.toLong()) +
            TimeUnit.MINUTES.toMillis(minute.toLong()) +
            TimeUnit.SECONDS.toMillis(second.toLong())
    }

    private companion object {
        const val DURATION_TAG = "change_activity_reminder_duration"
        const val DND_START_TAG = "change_activity_reminder_dnd_start"
        const val DND_END_TAG = "change_activity_reminder_dnd_end"
        const val ACTIVITY_TAG = "change_activity_reminder_activity"
    }
}
