/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
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
import com.example.util.simpletimetracker.wearrpc.WearCommunicationAPI
import com.example.util.simpletimetracker.wearrpc.Tag
import javax.inject.Inject

class WearCommunicationInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val appColorMapper: AppColorMapper,
) : WearCommunicationAPI {

    override suspend fun queryActivities(): List<Activity> {
        return recordTypeInteractor.getAll()
            .filter { recordType -> !recordType.hidden }
            .map { recordType ->
                Activity(
                    id = recordType.id,
                    name = recordType.name,
                    icon = recordType.icon,
                    color = mapColor(recordType.color),
                )
            }
    }

    override suspend fun queryCurrentActivities(): List<CurrentActivity> {
        return runningRecordInteractor.getAll().map { record ->
            CurrentActivity(
                id = record.id,
                startedAt = record.timeStarted,
                tags = record.tagIds.mapNotNull { tagId ->
                    recordTagInteractor.get(tagId)?.let(::mapTag)
                },
            )
        }
    }

    override suspend fun setCurrentActivities(starting: List<CurrentActivity>) {
        val currents = runningRecordInteractor.getAll()
        val currentsIds = runningRecordInteractor.getAll().map(RunningRecord::id)
        val startingIds = starting.map(CurrentActivity::id)

        val stopped = currents.filter { it.id !in startingIds }
        val started = starting.filter { it.id !in currentsIds }

        stopped.forEach {
            removeRunningRecordMediator.removeWithRecordAdd(it)
        }
        started.forEach { record ->
            addRunningRecordMediator.add(
                typeId = record.id,
                timeStarted = record.startedAt,
                tagIds = record.tags.map(Tag::id),
            )
        }
    }

    override suspend fun queryTagsForActivity(activityId: Long): List<Tag> {
        val activity = recordTypeInteractor.get(activityId) ?: return emptyList()
        val activityColor = mapColor(activity.color)
        return recordTagInteractor.getByTypeOrUntyped(activityId)
            .filter { !it.archived }
            .map { mapTag(it, activityColor) }
            .sortedBy { it.name }
            .sortedBy { it.isGeneral }
    }

    private fun mapTag(
        recordTag: RecordTag,
        activityColor: Long? = null,
    ): Tag {
        val isGeneral = recordTag.typeId == 0L
        val tagColor = if (isGeneral) {
            mapColor(recordTag.color)
        } else {
            activityColor
        }

        return Tag(
            id = recordTag.id,
            name = recordTag.name,
            isGeneral = isGeneral,
            color = tagColor.orZero(),
        )
    }

    private fun mapColor(appColor: AppColor): Long {
        return appColorMapper.mapToColorInt(appColor).toLong()
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