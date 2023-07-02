package com.example.util.simpletimetracker.core.interactor

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.PermissionRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.navigation.RequestCode
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.action.OpenSystemSettings
import com.example.util.simpletimetracker.navigation.params.action.RequestPermissionParams
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import javax.inject.Inject

class CheckNotificationsPermissionInteractor @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val permissionRepo: PermissionRepo,
) {

    fun execute(
        onEnabled: () -> Unit,
        onDisabled: () -> Unit = {},
        anchor: Any? = null,
    ) {
        when {
            permissionRepo.areNotificationsEnabled() -> onEnabled()

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> requestPermission(
                onEnabled = onEnabled,
                onDisabled = onDisabled,
                anchor = anchor
            )

            else -> doOnDisabled(
                onDisabled = onDisabled,
                anchor = anchor
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission(
        onEnabled: () -> Unit,
        onDisabled: () -> Unit,
        anchor: Any? = null,
    ) {
        router.setResultListener(RequestCode.REQUEST_PERMISSION) {
            val isGranted = it as? Boolean == true
            if (isGranted) {
                onEnabled()
            } else {
                doOnDisabled(onDisabled, anchor)
            }
        }
        router.execute(RequestPermissionParams(Manifest.permission.POST_NOTIFICATIONS))
    }

    private fun doOnDisabled(
        onDisabled: () -> Unit,
        anchor: Any? = null,
    ) {
        showExplanation(anchor)
        onDisabled()
    }

    private fun showExplanation(anchor: Any? = null) {
        SnackBarParams(
            message = resourceRepo.getString(R.string.post_notifications),
            duration = SnackBarParams.Duration.Long,
            actionText = resourceRepo.getString(R.string.schedule_exact_alarms_open_settings),
            actionListener = {
                router.execute(OpenSystemSettings.Notifications)
            },
        ).let {
            router.show(it, anchor)
        }
    }
}