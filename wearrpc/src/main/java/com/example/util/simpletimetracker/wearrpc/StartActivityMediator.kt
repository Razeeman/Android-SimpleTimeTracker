/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

class StartActivityMediator(
    private val api: SimpleTimeTrackerAPI,
    private val onRequestStartActivity: suspend (activity: Activity) -> Unit,
    private val onRequestTagSelection: suspend (activity: Activity) -> Unit,
) {
    suspend fun requestStart(activity: Activity) {
        val settings = api.querySettings()
        if (settings.showRecordTagSelection) {
            requestTagSelectionIfNeeded(activity, settings)
        } else {
            onRequestStartActivity(activity)
        }
    }

    private suspend fun requestTagSelectionIfNeeded(activity: Activity, settings: Settings) {
        val tags = api.queryTagsForActivity(activity.id)
        val generalTags = tags.filter { it.isGeneral }
        val nonGeneralTags = tags.filter { !it.isGeneral }
        val tagSelectionNeeded =
            nonGeneralTags.isNotEmpty() || generalTags.isNotEmpty() && settings.recordTagSelectionEvenForGeneralTags
        if (tagSelectionNeeded) {
            onRequestTagSelection(activity)
        } else {
            onRequestStartActivity(activity)
        }
    }
}