/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
 package com.example.util.simpletimetracker.wear

import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.mapper.AppColorMapper
import com.example.util.simpletimetracker.wearrpc.Activity
import com.example.util.simpletimetracker.wearrpc.CurrentActivity
import com.example.util.simpletimetracker.wearrpc.Settings
import com.example.util.simpletimetracker.wearrpc.SimpleTimeTrackerAPI
import com.example.util.simpletimetracker.wearrpc.Tag

class DomainAPI (
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val appColorMapper: AppColorMapper,
): SimpleTimeTrackerAPI {

    override suspend fun queryActivities(): Array<Activity> {
        return recordTypeInteractor.getAll()
            .filter { recordType -> !recordType.hidden }
            .map { recordType ->
                val color = appColorMapper.mapToColorInt(recordType.color)
                val hex = String.format("#%06X", (0xFFFFFF and color))
                Activity(recordType.id, recordType.name, recordType.icon, hex)
            }
            .toTypedArray()
    }

    override suspend fun queryCurrentActivities(): Array<CurrentActivity> {
        return runningRecordInteractor.getAll().map {
            CurrentActivity(
                it.id,
                it.timeStarted,
                arrayOf()  // TODO - Pull actual list of active tags
            )
        }.toTypedArray()
    }

    override suspend fun setCurrentActivities(activities: Array<CurrentActivity>) {
        TODO("Not yet implemented")
        // This is a little tricky... The given `activities` should be considered a declarative
        // statement of the activities expected to be running upon successful completion of this
        // method.

        // Currently running activities not in the given Array should be stopped

        // Activities in the given Array that are not running should be started

        // For activities in the given Array which are running...
            // If the start dates + tags are unchanged, then leave the activity running.
            // If the start dates and/or tags are different, stop the current running activity
            // instance and restart it as of the requested start date.

    }

    override suspend fun queryTagsForActivity(activityId: Long): Array<Tag> {
        TODO("Not yet implemented")
        // Look up the tags which can be associated with this activity and return them
    }

    override suspend fun querySettings(): Settings {
        return Settings(prefsInteractor.getAllowMultitasking())
    }

}