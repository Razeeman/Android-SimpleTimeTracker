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
        val daysInOrder = timeMapper.getWeekOrder(firstDayOfWeek).map { dayOfWeek ->
            dayOfWeek to timeMapper.toShortDayOfWeekName(dayOfWeek)
        }
        val recurrentText = resourceRepo.getString(R.string.settings_inactivity_reminder_recurrent)
        val doNotDisturbStart = settingsMapper.toStartOfDayText(
            startOfDayShift = doNotDisturbStart,
            useMilitaryTime = useMilitaryTime,
        )
        val doNotDisturbEnd = settingsMapper.toStartOfDayText(
            startOfDayShift = doNotDisturbEnd,
            useMilitaryTime = useMilitaryTime,
        )
        val days = formatDays(
            daysInOrder = daysInOrder,
            selectedDaysOfWeek = selectedDaysOfWeek,
        ).takeIf(String::isNotEmpty)

        return listOfNotNull(
            recurrentText.takeIf { isRecurrent },
            "$doNotDisturbStart-$doNotDisturbEnd",
            days,
        ).joinToString(separator = " ")
    }

    private fun formatDays(
        daysInOrder: List<Pair<DayOfWeek, String>>,
        selectedDaysOfWeek: Set<DayOfWeek>,
    ): String {
        if (selectedDaysOfWeek.containsAll(DayOfWeek.entries)) return ""

        val runs = mutableListOf<MutableList<Pair<DayOfWeek, String>>>()

        daysInOrder.forEach { day ->
            if (day.first in selectedDaysOfWeek) {
                runs.lastOrNull()?.add(day) ?: runs.add(mutableListOf(day))
            } else if (runs.lastOrNull()?.isNotEmpty() == true) {
                runs.add(mutableListOf())
            }
        }

        return runs
            .filter { it.isNotEmpty() }
            .joinToString(separator = " ") { run ->
                when (run.size) {
                    1 -> run.first().second
                    else -> "${run.first().second}-${run.last().second}"
                }
            }
    }
}
