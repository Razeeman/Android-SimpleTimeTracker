package com.example.util.simpletimetracker.feature_pomodoro.timer.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.dropMillis
import com.example.util.simpletimetracker.domain.extension.toDuration
import com.example.util.simpletimetracker.domain.mapper.PomodoroCycleDurationsMapper
import com.example.util.simpletimetracker.domain.model.PomodoroCycleSettings
import com.example.util.simpletimetracker.domain.model.PomodoroCycleType
import com.example.util.simpletimetracker.feature_pomodoro.R
import com.example.util.simpletimetracker.feature_pomodoro.timer.model.PomodoroButtonState
import com.example.util.simpletimetracker.feature_pomodoro.timer.model.PomodoroTimerState
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PomodoroViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val pomodoroCycleDurationsMapper: PomodoroCycleDurationsMapper,
) {

    fun mapButtonState(
        isStarted: Boolean,
    ): PomodoroButtonState {
        val iconResId = if (isStarted) {
            R.drawable.button_stop
        } else {
            R.drawable.button_play
        }

        return PomodoroButtonState(
            iconResId = iconResId,
        )
    }

    fun mapTimerState(
        isStarted: Boolean,
        timeStartedMs: Long,
        timerUpdateMs: Long,
        settings: PomodoroCycleSettings,
    ): PomodoroTimerState {
        // Min increment of one pixel.
        val maxProgress = 1024 * Math.PI

        return if (isStarted) {
            val result = pomodoroCycleDurationsMapper.map(
                timeStartedMs = timeStartedMs.dropMillis(),
                settings = settings,
            )
            val currentCycle = result.cycleType
            val cycleDuration = result.cycleDuration
            val currentCycleDuration = result.currentCycleDuration

            val timeLeft = cycleDuration - currentCycleDuration
            val progression = (currentCycleDuration + timerUpdateMs) *
                maxProgress / cycleDuration
            val times = formatInterval(timeLeft)

            PomodoroTimerState(
                maxProgress = maxProgress.toInt(),
                progress = progression.toInt(),
                timerUpdateMs = timerUpdateMs,
                durationState = mapToDurationState(times),
                currentCycleHint = mapCurrentStateHint(currentCycle),
            )
        } else {
            val times = formatInterval(settings.focusTimeMs)
            val currentCycle = PomodoroCycleType.Focus

            PomodoroTimerState(
                maxProgress = maxProgress.toInt(),
                progress = 0,
                timerUpdateMs = timerUpdateMs,
                durationState = mapToDurationState(times),
                currentCycleHint = mapCurrentStateHint(currentCycle),
            )
        }
    }

    private fun mapToDurationState(
        data: TimeState,
    ): PomodoroTimerState.DurationState {
        return PomodoroTimerState.DurationState(
            textHours = data.hr.toDuration(),
            textMinutes = data.min.toDuration(),
            textSeconds = data.sec.toDuration(),
            hoursIsVisible = data.hr > 0,
        )
    }

    private fun formatInterval(interval: Long): TimeState {
        val hr: Long = TimeUnit.MILLISECONDS.toHours(
            interval,
        )
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(
            interval - TimeUnit.HOURS.toMillis(hr),
        )
        val sec: Long = TimeUnit.MILLISECONDS.toSeconds(
            interval - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min),
        )
        return TimeState(
            hr = hr,
            min = min,
            sec = sec,
        )
    }

    private fun mapCurrentStateHint(
        cycleType: PomodoroCycleType,
    ): String {
        return when (cycleType) {
            is PomodoroCycleType.Focus -> R.string.pomodoro_state_focus
            is PomodoroCycleType.Break -> R.string.pomodoro_state_break
            is PomodoroCycleType.LongBreak -> R.string.pomodoro_state_long_break
        }.let(resourceRepo::getString)
    }

    private data class TimeState(
        val hr: Long,
        val min: Long,
        val sec: Long,
    )
}