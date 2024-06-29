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
        val currentPeriodDuration = currentDuration % periodDuration
        val currentShortPeriodDuration = currentPeriodDuration % (focusTimeSec + breakTimeSec)

        return if (periodsUntilLongBreak > 0) {
            when {
                currentPeriodDuration >= periodDuration - longBreakTimeSec -> Result(
                    cycleDuration = longBreakTimeSec,
                    currentCycleDuration = currentPeriodDuration - (periodDuration - longBreakTimeSec),
                    cycleType = PomodoroCycleType.LongBreak,
                )
                currentShortPeriodDuration < focusTimeSec -> Result(
                    cycleDuration = focusTimeSec,
                    currentCycleDuration = currentShortPeriodDuration,
                    cycleType = PomodoroCycleType.Focus,
                )
                else -> Result(
                    cycleDuration = breakTimeSec,
                    currentCycleDuration = currentShortPeriodDuration - focusTimeSec,
                    cycleType = PomodoroCycleType.Break,
                )
            }
        } else {
            if (currentPeriodDuration < focusTimeSec) {
                Result(
                    cycleDuration = focusTimeSec,
                    currentCycleDuration = currentPeriodDuration,
                    cycleType = PomodoroCycleType.Focus,
                )
            } else {
                Result(
                    cycleDuration = breakTimeSec,
                    currentCycleDuration = currentPeriodDuration - focusTimeSec,
                    cycleType = PomodoroCycleType.Break,
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