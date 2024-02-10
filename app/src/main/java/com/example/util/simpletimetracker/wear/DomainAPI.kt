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
            .map { recordType -> Activity(recordType.id, recordType.name) }
            .toTypedArray()
    }

    override suspend fun queryCurrentActivities(): Array<CurrentActivity> {
        TODO("Not yet implemented")
    }

    override suspend fun setCurrentActivities(activities: Array<CurrentActivity>) {
        TODO("Not yet implemented")
    }

    override suspend fun queryTagsForActivity(activityId: Long): Array<Tag> {
        TODO("Not yet implemented")
    }

    override suspend fun querySettings(): Settings {
        TODO("Not yet implemented")
    }

}