package com.example.util.simpletimetracker.domain.activityReminder.interactor

import com.example.util.simpletimetracker.domain.activityReminder.model.ActivityReminderOverride
import com.example.util.simpletimetracker.domain.activityReminder.repo.ActivityReminderOverrideRepo
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import javax.inject.Inject

/**
 * Resolves the scheduler-facing reminder rule for an activity.
 *
 * An activity without an override inherits the global reminder settings, a disabled override
 * produces no rule, and a custom override produces its own rule. The returned rule is a complete
 * runtime value; callers do not need to read preferences or persisted overrides separately.
 */
class ActivityReminderRuleResolver @Inject constructor(
    private val activityReminderOverrideRepo: ActivityReminderOverrideRepo,
    private val prefsInteractor: PrefsInteractor,
) {

    @Suppress("MoveVariableDeclarationIntoWhen")
    suspend fun resolve(activityId: Long): ActivityReminderOverride.Rule? {
        val mode = activityReminderOverrideRepo.get(activityId)?.mode

        return when (mode) {
            null -> resolveInherited()
            is ActivityReminderOverride.Mode.Disabled -> null
            is ActivityReminderOverride.Mode.Custom -> mode.rule
        }
    }

    private suspend fun resolveInherited(): ActivityReminderOverride.Rule? {
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
