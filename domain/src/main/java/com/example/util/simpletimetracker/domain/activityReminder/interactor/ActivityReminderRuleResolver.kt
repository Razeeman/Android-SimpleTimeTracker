package com.example.util.simpletimetracker.domain.activityReminder.interactor

import com.example.util.simpletimetracker.domain.activityReminder.model.ActivityReminderOverride
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import javax.inject.Inject

class ActivityReminderRuleResolver @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun resolveDefault(): ActivityReminderOverride.Rule? {
        val durationSeconds = prefsInteractor.getActivityReminderDuration()
        return if (durationSeconds > 0) {
            ActivityReminderOverride.Rule(
                id = 0,
                durationSeconds = durationSeconds,
                recurrent = prefsInteractor.getActivityReminderRecurrent(),
                applicableDaysOfWeek = prefsInteractor.getActivityReminderDaysOfWeek(),
                doNotDisturbStartMillis = prefsInteractor.getActivityReminderDoNotDisturbStart(),
                doNotDisturbEndMillis = prefsInteractor.getActivityReminderDoNotDisturbEnd(),
            )
        } else {
            null
        }
    }
}
