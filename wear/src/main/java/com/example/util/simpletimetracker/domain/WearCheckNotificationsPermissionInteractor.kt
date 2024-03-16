/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.domain

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.util.simpletimetracker.data.WearPermissionRepo
import com.example.util.simpletimetracker.navigation.WearActionResolver
import javax.inject.Inject

class WearCheckNotificationsPermissionInteractor @Inject constructor(
    private val wearActionResolver: WearActionResolver,
    private val wearPermissionRepo: WearPermissionRepo,
) {

    fun execute(
        onEnabled: () -> Unit,
        onDisabled: () -> Unit = {},
    ) {
        when {
            wearPermissionRepo.areNotificationsEnabled() -> onEnabled()

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> requestPermission(
                onEnabled = onEnabled,
                onDisabled = onDisabled,
            )

            else -> onDisabled()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission(
        onEnabled: () -> Unit,
        onDisabled: () -> Unit,
    ) {
        wearActionResolver.setResultListener {
            val isGranted = it as? Boolean == true
            if (isGranted) {
                onEnabled()
            } else {
                onDisabled()
            }
        }
        wearActionResolver.requestPermission(Manifest.permission.POST_NOTIFICATIONS)
    }
}