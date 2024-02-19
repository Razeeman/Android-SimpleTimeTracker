/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wear

import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.mapper.AppColorMapper
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.wearrpc.Activity
import com.example.util.simpletimetracker.wearrpc.CurrentActivity
import com.example.util.simpletimetracker.wearrpc.Settings
import com.example.util.simpletimetracker.wearrpc.SimpleTimeTrackerAPI
import com.example.util.simpletimetracker.wearrpc.Tag

class DomainAPI(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val appColorMapper: AppColorMapper,
) : SimpleTimeTrackerAPI {

    override suspend fun queryActivities(): Array<Activity> {
        return recordTypeInteractor.getAll().filter { recordType -> !recordType.hidden }
            .map { recordType ->
                val color = appColorMapper.mapToColorInt(recordType.color)
                val hex = String.format("#%06X", (0xFFFFFF and color))
                Activity(recordType.id, recordType.name, recordType.icon, hex)
            }.toTypedArray()
    }

    override suspend fun queryCurrentActivities(): Array<CurrentActivity> {
        return runningRecordInteractor.getAll().map { record ->
            CurrentActivity(
                record.id,
                record.timeStarted,
                record.tagIds.map { tagId ->
                    val tag = recordTagInteractor.get(tagId)
                    Tag(id = tag?.id ?: -1, name = tag?.name ?: "")
                }.filter { it.id > 0 }.toTypedArray(),
            )
        }.toTypedArray()
    }

    override suspend fun setCurrentActivities(activities: Array<CurrentActivity>) {
        val currents = queryCurrentActivities()
        val unchanged = currents.filter { c -> activities.any {a -> a == c} }
        val stopped = currents.filter { c -> unchanged.none { u -> u == c} }
        val started = activities.filter { a -> currents.none { c -> a == c } }
        stopped.forEach { runningRecordInteractor.remove(it.id) }
        started.forEach { runningRecordInteractor.add(
            RunningRecord(
                id = it.id,
                timeStarted = it.startedAt,
                comment = "",
                tagIds = it.tags.map { t -> t.id },
            )
        ) }
    }

    override suspend fun queryTagsForActivity(activityId: Long): Array<Tag> {
        return recordTagInteractor.getByTypeOrUntyped(activityId).filter { !it.archived }.map {
            Tag(id = it.id, name = it.name)
        }.toTypedArray()
    }

    override suspend fun querySettings(): Settings {
        return Settings(prefsInteractor.getAllowMultitasking())
    }

}