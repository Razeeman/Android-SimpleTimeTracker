/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wear

import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.mapper.AppColorMapper
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.RecordTag
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
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val appColorMapper: AppColorMapper,
) : SimpleTimeTrackerAPI {

    override suspend fun queryActivities(): Array<Activity> {
        return recordTypeInteractor.getAll().filter { recordType -> !recordType.hidden }
            .map { recordType ->
                Activity(
                    id = recordType.id,
                    name = recordType.name,
                    icon = recordType.icon,
                    color = asColor(recordType.color),
                )
            }.toTypedArray()
    }

    override suspend fun queryCurrentActivities(): Array<CurrentActivity> {
        return runningRecordInteractor.getAll().map { record ->
            CurrentActivity(
                record.id,
                record.timeStarted,
                record.tagIds.map { tagId ->
                    asTag(recordTagInteractor.get(tagId))
                }.filter { it.id > 0 }.toTypedArray(),
            )
        }.toTypedArray()
    }

    override suspend fun setCurrentActivities(activities: Array<CurrentActivity>) {
        val currents = queryCurrentActivities()
        val unchanged = currents.filter { c -> activities.any { a -> a == c } }
        val stopped = currents.filter { c -> unchanged.none { u -> u == c } }
        val started = activities.filter { a -> currents.none { c -> a == c } }
        stopped.forEach { removeRunningRecordMediator.removeWithRecordAdd(asRunningRecord(it)) }
        started.forEach { runningRecordInteractor.add(asRunningRecord(it)) }
    }

    private fun asRunningRecord(currentActivity: CurrentActivity): RunningRecord {
        return RunningRecord(
            id = currentActivity.id,
            timeStarted = currentActivity.startedAt,
            comment = "",
            tagIds = currentActivity.tags.map { t -> t.id },
        )
    }

    override suspend fun queryTagsForActivity(activityId: Long): Array<Tag> {
        val activityColor = recordTypeInteractor.get(activityId)?.color
        return recordTagInteractor.getByTypeOrUntyped(activityId).filter { !it.archived }
            .map { asTag(it, asColor(activityColor)) }.sortedBy { it.name }
            .sortedBy { it.isGeneral }.toTypedArray()
    }

    private fun asTag(recordTag: RecordTag?, activityColor: Long = 0x00000000): Tag {
        return if (recordTag != null) {
            val isGeneral = recordTag.typeId == 0L
            val tagColor = if (isGeneral) {
                asColor(recordTag.color)
            } else {
                activityColor
            }
            Tag(
                id = recordTag.id,
                name = recordTag.name,
                isGeneral = isGeneral,
                color = tagColor,
            )
        } else {
            Tag(id = -1, name = "", isGeneral = true, color = 0xFF555555)
        }
    }

    private fun asColor(appColor: AppColor?): Long {
        return if (appColor == null) {
            0x00000000
        } else {
            appColorMapper.mapToColorInt(appColor).toLong()
        }
    }

    override suspend fun querySettings(): Settings {
        return Settings(
            allowMultitasking = prefsInteractor.getAllowMultitasking(),
            showRecordTagSelection = prefsInteractor.getShowRecordTagSelection(),
            recordTagSelectionCloseAfterOne = prefsInteractor.getRecordTagSelectionCloseAfterOne(),
            recordTagSelectionEvenForGeneralTags = prefsInteractor.getRecordTagSelectionEvenForGeneralTags(),
        )
    }

}