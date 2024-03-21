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
import com.example.util.simpletimetracker.domain.interactor.SettingsDataUpdateInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.mapper.AppColorMapper
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.wear_api.WearActivity
import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import com.example.util.simpletimetracker.wear_api.WearCurrentActivity
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
import dagger.Lazy
import javax.inject.Inject

class WearCommunicationInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val removeRunningRecordMediator: Lazy<RemoveRunningRecordMediator>,
    private val addRunningRecordMediator: Lazy<AddRunningRecordMediator>,
    private val appColorMapper: AppColorMapper,
    private val router: Router,
    private val widgetInteractor: WidgetInteractor,
    private val settingsDataUpdateInteractor: SettingsDataUpdateInteractor,
) : WearCommunicationAPI {

    override suspend fun queryActivities(): List<WearActivity> {
        return recordTypeInteractor.getAll()
            .filter { recordType -> !recordType.hidden }
            .map { recordType ->
                WearActivity(
                    id = recordType.id,
                    name = recordType.name,
                    icon = recordType.icon,
                    color = mapColor(recordType.color),
                )
            }
    }

    override suspend fun queryCurrentActivities(): List<WearCurrentActivity> {
        return runningRecordInteractor.getAll().map { record ->
            WearCurrentActivity(
                id = record.id,
                startedAt = record.timeStarted,
                tags = record.tagIds.mapNotNull { tagId ->
                    recordTagInteractor.get(tagId)?.let(::mapTag)
                },
            )
        }
    }

    override suspend fun setCurrentActivities(starting: List<WearCurrentActivity>) {
        val currents = runningRecordInteractor.getAll()
        val currentsIds = runningRecordInteractor.getAll().map(RunningRecord::id)
        val startingIds = starting.map(WearCurrentActivity::id)

        val stopped = currents.filter { it.id !in startingIds }
        val started = starting.filter { it.id !in currentsIds }

        stopped.forEach {
            removeRunningRecordMediator.get().removeWithRecordAdd(it)
        }
        started.forEach { record ->
            addRunningRecordMediator.get().add(
                typeId = record.id,
                tagIds = record.tags.map(WearTag::id),
            )
        }
    }

    override suspend fun queryTagsForActivity(activityId: Long): List<WearTag> {
        val activity = recordTypeInteractor.get(activityId) ?: return emptyList()
        val activityColor = mapColor(activity.color)
        return recordTagInteractor.getByTypeOrUntyped(activityId)
            .filter { !it.archived }
            .map { mapTag(it, activityColor) }
            .sortedBy { it.name }
            .sortedBy { it.isGeneral }
    }

    override suspend fun querySettings(): WearSettings {
        return WearSettings(
            allowMultitasking = prefsInteractor.getAllowMultitasking(),
            showRecordTagSelection = prefsInteractor.getShowRecordTagSelection(),
            recordTagSelectionCloseAfterOne = prefsInteractor.getRecordTagSelectionCloseAfterOne(),
            recordTagSelectionEvenForGeneralTags = prefsInteractor.getRecordTagSelectionEvenForGeneralTags(),
        )
    }

    override suspend fun setSettings(settings: WearSettings) {
        prefsInteractor.setAllowMultitasking(settings.allowMultitasking)
        widgetInteractor.updateWidgets(listOf(WidgetType.QUICK_SETTINGS))
        settingsDataUpdateInteractor.send()
    }

    override suspend fun openPhoneApp() {
        router.startApp()
    }

    private fun mapTag(
        recordTag: RecordTag,
        activityColor: Long? = null,
    ): WearTag {
        val isGeneral = recordTag.typeId == 0L
        val tagColor = if (isGeneral) {
            mapColor(recordTag.color)
        } else {
            activityColor
        }

        return WearTag(
            id = recordTag.id,
            name = recordTag.name,
            isGeneral = isGeneral,
            color = tagColor.orZero(),
        )
    }

    private fun mapColor(appColor: AppColor): Long {
        return appColorMapper.mapToColorInt(appColor).toLong()
    }
}