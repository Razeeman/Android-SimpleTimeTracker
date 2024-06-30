package com.example.util.simpletimetracker.feature_notification.pomodoro.mapper

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.PomodoroCycleSettings
import com.example.util.simpletimetracker.domain.model.PomodoroCycleType
import com.example.util.simpletimetracker.feature_notification.R
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NotificationPomodoroMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
) {

    fun mapCycleType(
        data: PomodoroCycleType,
    ): Long {
        return when (data) {
            is PomodoroCycleType.Focus -> 0L
            is PomodoroCycleType.Break -> 1L
            is PomodoroCycleType.LongBreak -> 2L
        }
    }

    fun mapCycleType(
        data: Long?,
    ): PomodoroCycleType {
        return when (data) {
            0L -> PomodoroCycleType.Focus
            1L -> PomodoroCycleType.Break
            2L -> PomodoroCycleType.LongBreak
            else -> PomodoroCycleType.Focus
        }
    }

    fun mapSubtitle(
        cycleType: PomodoroCycleType,
        settings: PomodoroCycleSettings,
    ): String {
        val cycleName = when (cycleType) {
            is PomodoroCycleType.Focus -> R.string.pomodoro_state_focus
            is PomodoroCycleType.Break -> R.string.pomodoro_state_break
            is PomodoroCycleType.LongBreak -> R.string.pomodoro_state_long_break
        }.let(resourceRepo::getString)
        val cycleDuration = when (cycleType) {
            is PomodoroCycleType.Focus -> settings.focusTimeMs
            is PomodoroCycleType.Break -> settings.breakTimeMs
            is PomodoroCycleType.LongBreak -> settings.longBreakTimeMs
        }
            .let(TimeUnit.MILLISECONDS::toSeconds)
            .let(timeMapper::formatDuration)

        return resourceRepo.getString(
            R.string.notification_pomodoro_subtitle,
            cycleName,
            cycleDuration,
        )
    }
}