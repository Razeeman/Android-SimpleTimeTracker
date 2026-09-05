package com.example.util.simpletimetracker.feature_change_reminder.interactor

import com.example.util.simpletimetracker.core.mapper.DayOfWeekViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.extension.toLocalDateTime
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.utils.LocalDateMapper
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_change_reminder.R
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeActivityReminderEditor
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeActivityReminderEditor.Mode
import com.example.util.simpletimetracker.feature_change_reminder.viewData.ChangeActivityReminderViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import java.util.TimeZone
import javax.inject.Inject

class ChangeActivityReminderViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val dayOfWeekViewDataMapper: DayOfWeekViewDataMapper,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val localDateMapper: LocalDateMapper,
) {

    val modes = listOf(
        Mode.DISABLED,
        Mode.CUSTOM,
    )

    suspend fun getViewData(
        activity: RecordType?,
        editor: ChangeActivityReminderEditor,
        controlsEnabled: Boolean,
        activitySelectionEnabled: Boolean,
        deleteVisible: Boolean,
    ): ChangeActivityReminderViewData {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        return ChangeActivityReminderViewData(
            activityName = activity?.name
                ?: resourceRepo.getString(R.string.change_record_message_choose_type),
            activitySelectionEnabled = activitySelectionEnabled && controlsEnabled,
            modeItems = mapModeItems(),
            modeSelectedPosition = modes.indexOf(editor.mode),
            customFieldsVisible = editor.mode == Mode.CUSTOM,
            durationText = editor.durationSeconds
                .let(timeMapper::formatDuration),
            recurrent = editor.recurrent,
            daysOfWeek = dayOfWeekViewDataMapper.mapViewData(
                selectedDaysOfWeek = editor.daysOfWeek,
                isDarkTheme = prefsInteractor.getDarkMode(),
                firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
                width = DayOfWeekViewData.Width.MatchParent,
                paddingHorizontalDp = 4,
            ),
            doNotDisturbStartText = formatTimeOfDay(
                millis = editor.doNotDisturbStartMillis,
                useMilitaryTime = useMilitaryTime,
            ),
            doNotDisturbEndText = formatTimeOfDay(
                millis = editor.doNotDisturbEndMillis,
                useMilitaryTime = useMilitaryTime,
            ),
            controlsEnabled = controlsEnabled,
            deleteVisible = deleteVisible,
        )
    }

    fun mapMode(position: Int): Mode? {
        return modes.getOrNull(position)
    }

    private fun formatTimeOfDay(millis: Long, useMilitaryTime: Boolean): String {
        val timeZone = TimeZone.getDefault()
        val date = currentTimestampProvider.get().toLocalDateTime(timeZone).toLocalDate()
        val timestamp = localDateMapper.resolveDateTime(
            date = date,
            timeOfDayMillis = millis,
            timeZone = timeZone,
        ) ?: return resourceRepo.getString(R.string.no_data)
        return timeMapper.formatTime(
            time = timestamp,
            useMilitaryTime = useMilitaryTime,
            showSeconds = false,
        )
    }

    private fun mapModeItems(): List<CustomSpinner.CustomSpinnerTextItem> {
        return modes.map {
            val textRes = when (it) {
                Mode.DISABLED -> R.string.activity_reminder_mode_disabled
                Mode.CUSTOM -> R.string.activity_reminder_mode_custom
            }
            CustomSpinner.CustomSpinnerTextItem(resourceRepo.getString(textRes))
        }
    }
}
