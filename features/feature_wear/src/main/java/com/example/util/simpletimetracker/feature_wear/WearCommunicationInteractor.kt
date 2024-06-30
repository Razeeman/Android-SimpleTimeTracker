/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.GetSelectableTagsInteractor
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
import com.example.util.simpletimetracker.domain.model.RecordType
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
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val removeRunningRecordMediator: Lazy<RemoveRunningRecordMediator>,
    private val addRunningRecordMediator: Lazy<AddRunningRecordMediator>,
    private val appColorMapper: AppColorMapper,
    private val router: Router,
    private val widgetInteractor: WidgetInteractor,
    private val settingsDataUpdateInteractor: SettingsDataUpdateInteractor,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
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
            val tags = record.tagIds.mapNotNull { tagId ->
                recordTagInteractor.get(tagId)?.let {
                    mapTag(
                        recordTag = it,
                        types = emptyMap(), // Color is not needed.
                    )
                }
            }
            WearCurrentActivity(
                id = record.id,
                startedAt = record.timeStarted,
                tags = tags,
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
            addRunningRecordMediator.get().startTimer(
                typeId = record.id,
                tagIds = record.tags.map(WearTag::id),
                comment = "",
                checkMultitasking = false,
            )
        }
    }

    override suspend fun queryTagsForActivity(activityId: Long): List<WearTag> {
        val types = recordTypeInteractor.getAll().associateBy { it.id }
        return getSelectableTagsInteractor.execute(activityId)
            .filterNot { it.archived }
            .map {
                mapTag(
                    recordTag = it,
                    types = types,
                )
            }
    }

    override suspend fun querySettings(): WearSettings {
        return WearSettings(
            allowMultitasking = prefsInteractor.getAllowMultitasking(),
            showRecordTagSelection = prefsInteractor.getShowRecordTagSelection(),
            recordTagSelectionCloseAfterOne = prefsInteractor.getRecordTagSelectionCloseAfterOne(),
            recordTagSelectionExcludedActivities = prefsInteractor.getRecordTagSelectionExcludeActivities(),
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
        types: Map<Long, RecordType>,
    ): WearTag {
        return WearTag(
            id = recordTag.id,
            name = recordTag.name,
            color = recordTagViewDataMapper.mapColor(
                tag = recordTag,
                types = types,
            ).let(::mapColor),
        )
    }

    private fun mapColor(appColor: AppColor): Long {
        return appColorMapper.mapToColorInt(appColor).toLong()
    }
}