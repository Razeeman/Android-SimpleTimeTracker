package com.example.util.simpletimetracker.feature_notification.pomodoro.interactor

import com.example.util.simpletimetracker.domain.extension.dropMillis
import com.example.util.simpletimetracker.domain.interactor.GetPomodoroSettingsInteractor
import com.example.util.simpletimetracker.domain.interactor.PomodoroCycleNotificationInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.mapper.PomodoroCycleDurationsMapper
import com.example.util.simpletimetracker.feature_notification.pomodoro.manager.NotificationPomodoroManager
import com.example.util.simpletimetracker.feature_notification.pomodoro.scheduler.NotificationPomodoroScheduler
import javax.inject.Inject

class PomodoroCycleNotificationInteractorImpl @Inject constructor(
    private val manager: NotificationPomodoroManager,
    private val scheduler: NotificationPomodoroScheduler,
    private val pomodoroCycleDurationsMapper: PomodoroCycleDurationsMapper,
    private val getPomodoroSettingsInteractor: GetPomodoroSettingsInteractor,
    private val prefsInteractor: PrefsInteractor,
) : PomodoroCycleNotificationInteractor {

    override suspend fun checkAndReschedule() {
        if (!prefsInteractor.getEnablePomodoroMode()) return
        val timeStartedMs = prefsInteractor.getPomodoroModeStartedTimestampMs()
        if (timeStartedMs == 0L) return

        val result = pomodoroCycleDurationsMapper.map(
            timeStartedMs = timeStartedMs.dropMillis(),
            settings = getPomodoroSettingsInteractor.execute(),
        )

        val timeLeft = result.cycleDurationMs - result.currentCycleDurationMs
        scheduler.schedule(
            timestamp = System.currentTimeMillis() + timeLeft,
            cycleType = result.nextCycleType,
        )
    }

    override fun cancel() {
        scheduler.cancelSchedule()
        manager.hide()
    }
}