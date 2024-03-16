/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.navigation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearActionResolver @Inject constructor(
    private val resultContainer: WearResultContainer,
) {

    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null

    fun registerResultListeners(activity: ComponentActivity) {
        requestPermissionLauncher = activity.registerForRequestPermission(REQUEST_PERMISSION)
    }

    fun setResultListener(listener: WearResultContainer.ResultListener) {
        resultContainer.setResultListener(REQUEST_PERMISSION, listener)
    }

    fun requestPermission(permissionId: String) {
        requestPermissionLauncher?.launch(permissionId)
    }

    private fun ComponentActivity.registerForRequestPermission(key: String): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            resultContainer.sendResult(key, result)
        }
    }

    companion object {
        private const val REQUEST_PERMISSION = "REQUEST_PERMISSION"
    }
}