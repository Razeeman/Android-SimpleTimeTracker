package com.example.util.simpletimetracker.feature_settings.mapper

import com.example.util.simpletimetracker.core.mapper.ChangeReminderViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_views.extension.joinToSpannable
import javax.inject.Inject

class ReminderSummaryMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val changeReminderViewDataMapper: ChangeReminderViewDataMapper,
) {

    fun map(
        isRecurrent: Boolean,
        doNotDisturbStart: Long,
        doNotDisturbEnd: Long,
        selectedDaysOfWeek: Set<DayOfWeek>,
        firstDayOfWeek: DayOfWeek,
        useMilitaryTime: Boolean,
    ): CharSequence {
        val recurrentText = if (isRecurrent) {
            resourceRepo.getString(R.string.settings_inactivity_reminder_recurrent)
        } else {
            resourceRepo.getString(R.string.reminders_schedule_one_time)
        }
        val dnd = changeReminderViewDataMapper.mapDndHint(
            doNotDisturbStartMillis = doNotDisturbStart,
            doNotDisturbEndMillis = doNotDisturbEnd,
            useMilitaryTime = useMilitaryTime,
            iconColor = resourceRepo.getColor(R.color.textSecondary),
        )
        val days = timeMapper.formatDays(
            firstDayOfWeek = firstDayOfWeek,
            selectedDaysOfWeek = selectedDaysOfWeek,
        ).takeIf(String::isNotEmpty)

        return listOfNotNull(
            recurrentText,
            days,
            dnd,
        ).joinToSpannable(separator = " · ")
    }
}
