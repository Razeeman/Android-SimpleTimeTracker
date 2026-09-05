package com.example.util.simpletimetracker.feature_settings.mapper

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.feature_settings.R
import javax.inject.Inject

class ReminderSummaryMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val settingsMapper: SettingsMapper,
    private val timeMapper: TimeMapper,
) {

    fun map(
        isRecurrent: Boolean,
        doNotDisturbStart: Long,
        doNotDisturbEnd: Long,
        selectedDaysOfWeek: Set<DayOfWeek>,
        firstDayOfWeek: DayOfWeek,
        useMilitaryTime: Boolean,
    ): String {
        val recurrentText = if (isRecurrent) {
            resourceRepo.getString(R.string.settings_inactivity_reminder_recurrent)
        } else {
            resourceRepo.getString(R.string.reminders_schedule_one_time)
        }
        val doNotDisturbStart = settingsMapper.toStartOfDayText(
            startOfDayShift = doNotDisturbStart,
            useMilitaryTime = useMilitaryTime,
        )
        val doNotDisturbEnd = settingsMapper.toStartOfDayText(
            startOfDayShift = doNotDisturbEnd,
            useMilitaryTime = useMilitaryTime,
        )
        val days = timeMapper.formatDays(
            firstDayOfWeek = firstDayOfWeek,
            selectedDaysOfWeek = selectedDaysOfWeek,
        ).takeIf(String::isNotEmpty)

        // TODO if dnd is disabled (start equals end) - do not show it.
        return listOfNotNull(
            recurrentText,
            days,
            "$doNotDisturbStart-$doNotDisturbEnd",
        ).joinToString(separator = " · ")
    }
}
