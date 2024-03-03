/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.domain

import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.wear_api.WearActivity
import com.example.util.simpletimetracker.wear_api.WearSettings
import javax.inject.Inject

class StartActivityMediator @Inject constructor(
    private val wearDataRepo: WearDataRepo,
) {

    suspend fun requestStart(
        activity: WearActivity,
        onRequestStartActivity: suspend (activity: WearActivity) -> Unit,
        onRequestTagSelection: suspend (activity: WearActivity) -> Unit,
    ) {
        val settings = wearDataRepo.loadSettings()
        if (settings.showRecordTagSelection) {
            requestTagSelectionIfNeeded(
                activity = activity,
                settings = settings,
                onRequestStartActivity = onRequestStartActivity,
                onRequestTagSelection = onRequestTagSelection,
            )
        } else {
            onRequestStartActivity(activity)
        }
    }

    private suspend fun requestTagSelectionIfNeeded(
        activity: WearActivity,
        settings: WearSettings,
        onRequestStartActivity: suspend (activity: WearActivity) -> Unit,
        onRequestTagSelection: suspend (activity: WearActivity) -> Unit,
    ) {
        val tags = wearDataRepo.loadTagsForActivity(activity.id)
        val generalTags = tags.filter { it.isGeneral }
        val nonGeneralTags = tags.filter { !it.isGeneral }
        val tagSelectionNeeded = nonGeneralTags.isNotEmpty() ||
            generalTags.isNotEmpty() && settings.recordTagSelectionEvenForGeneralTags
        if (tagSelectionNeeded) {
            onRequestTagSelection(activity)
        } else {
            onRequestStartActivity(activity)
        }
    }
}