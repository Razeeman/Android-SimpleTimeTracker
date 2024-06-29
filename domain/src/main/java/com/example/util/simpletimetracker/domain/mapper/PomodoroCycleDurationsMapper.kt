package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.extension.dropMillis
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
        val focusTimeSec = settings.focusTimeMs
        val breakTimeSec = settings.breakTimeMs
        val longBreakTimeSec = settings.longBreakTimeMs
        val periodsUntilLongBreak = settings.periodsUntilLongBreak

        val currentTime = currentTimestampProvider.get().dropMillis()
        val currentDuration = currentTime - timeStartedMs
        val periodDuration = if (periodsUntilLongBreak > 0) {
            focusTimeSec * periodsUntilLongBreak +
                breakTimeSec * (periodsUntilLongBreak - 1) +
                longBreakTimeSec
        } else {
            focusTimeSec + breakTimeSec
        }
        if (periodDuration == 0L) {
            // Avoid divide by zero just in case.
            return Result(
                cycleType = PomodoroCycleType.Focus,
                cycleDuration = 0L,
                currentCycleDuration = 0L,
            )
        }
        val currentPeriodDuration = currentDuration % periodDuration
        val currentShortPeriodDuration = currentPeriodDuration % (focusTimeSec + breakTimeSec)

        return if (periodsUntilLongBreak > 0) {
            when {
                currentPeriodDuration >= periodDuration - longBreakTimeSec -> Result(
                    cycleType = PomodoroCycleType.LongBreak,
                    cycleDuration = longBreakTimeSec,
                    currentCycleDuration = currentPeriodDuration - (periodDuration - longBreakTimeSec),
                )
                currentShortPeriodDuration < focusTimeSec -> Result(
                    cycleType = PomodoroCycleType.Focus,
                    cycleDuration = focusTimeSec,
                    currentCycleDuration = currentShortPeriodDuration,
                )
                else -> Result(
                    cycleType = PomodoroCycleType.Break,
                    cycleDuration = breakTimeSec,
                    currentCycleDuration = currentShortPeriodDuration - focusTimeSec,
                )
            }
        } else {
            if (currentPeriodDuration < focusTimeSec) {
                Result(
                    cycleType = PomodoroCycleType.Focus,
                    cycleDuration = focusTimeSec,
                    currentCycleDuration = currentPeriodDuration,
                )
            } else {
                Result(
                    cycleType = PomodoroCycleType.Break,
                    cycleDuration = breakTimeSec,
                    currentCycleDuration = currentPeriodDuration - focusTimeSec,
                )
            }
        }
    }

    data class Result(
        val cycleType: PomodoroCycleType,
        val cycleDuration: Long,
        val currentCycleDuration: Long,
    )
}