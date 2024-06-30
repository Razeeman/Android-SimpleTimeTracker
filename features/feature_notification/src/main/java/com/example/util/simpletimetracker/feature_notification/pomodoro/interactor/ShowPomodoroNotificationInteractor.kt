package com.example.util.simpletimetracker.feature_notification.pomodoro.interactor

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.GetPomodoroSettingsInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.pomodoro.manager.NotificationPomodoroManager
import com.example.util.simpletimetracker.feature_notification.pomodoro.manager.NotificationPomodoroParams
import com.example.util.simpletimetracker.feature_notification.pomodoro.mapper.NotificationPomodoroMapper
import javax.inject.Inject

class ShowPomodoroNotificationInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val manager: NotificationPomodoroManager,
    private val prefsInteractor: PrefsInteractor,
    private val mapper: NotificationPomodoroMapper,
    private val getPomodoroSettingsInteractor: GetPomodoroSettingsInteractor,
) {

    suspend fun show(
        cycleTypeExtra: Long?,
    ) {
        manager.hide()
        val cycleType = mapper.mapCycleType(cycleTypeExtra)
        NotificationPomodoroParams(
            title = resourceRepo.getString(R.string.running_records_pomodoro),
            subtitle = mapper.mapSubtitle(
                cycleType = cycleType,
                settings = getPomodoroSettingsInteractor.execute(),
            ),
            isDarkTheme = prefsInteractor.getDarkMode(),
        ).let(manager::show)
    }
}