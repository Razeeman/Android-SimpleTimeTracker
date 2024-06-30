package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.model.PomodoroCycleSettings
import com.example.util.simpletimetracker.domain.model.PomodoroCycleType
import com.example.util.simpletimetracker.domain.provider.CurrentTimestampProvider
import javax.inject.Inject

class PomodoroCycleDurationsMapper @Inject constructor(
    private val currentTimestampProvider: CurrentTimestampProvider,
) {

    fun map(
        timeStartedMs: Long,
        settings: PomodoroCycleSettings,
    ): Result {
        val focusTime = settings.focusTimeMs
        val breakTime = settings.breakTimeMs
        val longBreakTime = settings.longBreakTimeMs
        val periodsUntilLongBreak = settings.periodsUntilLongBreak

        val currentTime = currentTimestampProvider.get()
        val currentDuration = currentTime - timeStartedMs
        val periodDuration = if (periodsUntilLongBreak > 0) {
            focusTime * periodsUntilLongBreak +
                breakTime * (periodsUntilLongBreak - 1) +
                longBreakTime
        } else {
            focusTime + breakTime
        }
        if (periodDuration == 0L) {
            // Avoid divide by zero just in case.
            return Result(
                cycleType = PomodoroCycleType.Focus,
                nextCycleType = PomodoroCycleType.Focus,
                cycleDurationMs = 0L,
                currentCycleDurationMs = 0L,
            )
        }
        val currentPeriodDuration = currentDuration % periodDuration
        val currentShortPeriodDuration = currentPeriodDuration % (focusTime + breakTime)

        return if (periodsUntilLongBreak > 0) {
            when {
                currentPeriodDuration >= periodDuration - longBreakTime -> Result(
                    cycleType = PomodoroCycleType.LongBreak,
                    nextCycleType = PomodoroCycleType.Focus,
                    cycleDurationMs = longBreakTime,
                    currentCycleDurationMs = currentPeriodDuration - (periodDuration - longBreakTime),
                )
                currentShortPeriodDuration < focusTime -> Result(
                    cycleType = PomodoroCycleType.Focus,
                    nextCycleType = if (currentPeriodDuration >=
                        periodDuration - longBreakTime - focusTime
                    ) {
                        PomodoroCycleType.LongBreak
                            .takeIf { longBreakTime > 0L }
                            ?: PomodoroCycleType.Focus
                    } else {
                        PomodoroCycleType.Break
                            .takeIf { breakTime > 0L }
                            ?: PomodoroCycleType.Focus
                    },
                    cycleDurationMs = focusTime,
                    currentCycleDurationMs = currentShortPeriodDuration,
                )
                else -> Result(
                    cycleType = PomodoroCycleType.Break,
                    nextCycleType = PomodoroCycleType.Focus,
                    cycleDurationMs = breakTime,
                    currentCycleDurationMs = currentShortPeriodDuration - focusTime,
                )
            }
        } else {
            if (currentPeriodDuration < focusTime) {
                Result(
                    cycleType = PomodoroCycleType.Focus,
                    nextCycleType = PomodoroCycleType.Break
                        .takeIf { breakTime > 0L }
                        ?: PomodoroCycleType.Focus,
                    cycleDurationMs = focusTime,
                    currentCycleDurationMs = currentPeriodDuration,
                )
            } else {
                Result(
                    cycleType = PomodoroCycleType.Break,
                    nextCycleType = PomodoroCycleType.Focus,
                    cycleDurationMs = breakTime,
                    currentCycleDurationMs = currentPeriodDuration - focusTime,
                )
            }
        }
    }

    data class Result(
        val cycleType: PomodoroCycleType,
        val nextCycleType: PomodoroCycleType,
        val cycleDurationMs: Long,
        val currentCycleDurationMs: Long,
    )
}