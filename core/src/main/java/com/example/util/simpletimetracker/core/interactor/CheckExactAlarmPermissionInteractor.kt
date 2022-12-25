package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.PermissionRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.action.OpenSystemSettings
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import javax.inject.Inject

class CheckExactAlarmPermissionInteractor @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val permissionRepo: PermissionRepo,
) {

    fun execute() {
        if (!permissionRepo.canScheduleExactAlarms()) {
            val message = resourceRepo.getString(R.string.schedule_exact_alarms)
            SnackBarParams(
                message = message,
                duration = SnackBarParams.Duration.Long,
                actionText = resourceRepo.getString(R.string.schedule_exact_alarms_open_settings),
                actionListener = {
                    router.execute(OpenSystemSettings.ExactAlarms)
                },
            ).let(router::show)
        }
    }
}