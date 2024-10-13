/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.interactor.SettingsDataUpdateInteractor
import com.example.util.simpletimetracker.domain.interactor.ShouldShowTagSelectionInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.wear_api.WearActivityDTO
import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import com.example.util.simpletimetracker.wear_api.WearCurrentActivityDTO
import com.example.util.simpletimetracker.wear_api.WearRecordRepeatResponse
import com.example.util.simpletimetracker.wear_api.WearSettingsDTO
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagSelectionRequest
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagSelectionResponse
import com.example.util.simpletimetracker.wear_api.WearStartActivityRequest
import com.example.util.simpletimetracker.wear_api.WearStopActivityRequest
import com.example.util.simpletimetracker.wear_api.WearTagDTO
import dagger.Lazy
import javax.inject.Inject

class WearDataRepo @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val shouldShowTagSelectionInteractor: ShouldShowTagSelectionInteractor,
    private val removeRunningRecordMediator: Lazy<RemoveRunningRecordMediator>,
    private val addRunningRecordMediator: Lazy<AddRunningRecordMediator>,
    private val recordRepeatInteractor: Lazy<RecordRepeatInteractor>,
    private val router: Router,
    private val widgetInteractor: WidgetInteractor,
    private val settingsDataUpdateInteractor: SettingsDataUpdateInteractor,
    private val wearDataLocalMapper: WearDataLocalMapper,
) : WearCommunicationAPI {

    override suspend fun queryActivities(): List<WearActivityDTO> {
        return recordTypeInteractor.getAll()
            .filter { recordType -> !recordType.hidden }
            .map(wearDataLocalMapper::map)
    }

    override suspend fun queryCurrentActivities(): List<WearCurrentActivityDTO> {
        return runningRecordInteractor.getAll().map { record ->
            val tags = record.tagIds.mapNotNull { tagId ->
                recordTagInteractor.get(tagId)?.let {
                    wearDataLocalMapper.map(
                        recordTag = it,
                        types = emptyMap(), // Color is not needed.
                    )
                }
            }
            wearDataLocalMapper.map(record, tags)
        }
    }

    override suspend fun startActivity(request: WearStartActivityRequest) {
        addRunningRecordMediator.get().startTimer(
            typeId = request.id,
            tagIds = request.tagIds,
            comment = "",
        )
    }

    override suspend fun stopActivity(request: WearStopActivityRequest) {
        val current = runningRecordInteractor.get(request.id) ?: return
        removeRunningRecordMediator.get().removeWithRecordAdd(current)
    }

    override suspend fun repeatActivity(): WearRecordRepeatResponse {
        return recordRepeatInteractor.get().repeatWithoutMessage()
            .let(wearDataLocalMapper::map)
    }

    override suspend fun queryTagsForActivity(activityId: Long): List<WearTagDTO> {
        val types = recordTypeInteractor.getAll().associateBy { it.id }
        return getSelectableTagsInteractor.execute(activityId)
            .filterNot { it.archived }
            .map {
                wearDataLocalMapper.map(
                    recordTag = it,
                    types = types,
                )
            }
    }

    override suspend fun queryShouldShowTagSelection(
        request: WearShouldShowTagSelectionRequest,
    ): WearShouldShowTagSelectionResponse {
        return WearShouldShowTagSelectionResponse(
            shouldShowTagSelectionInteractor.execute(request.id),
        )
    }

    override suspend fun querySettings(): WearSettingsDTO {
        return wearDataLocalMapper.map(
            allowMultitasking = prefsInteractor.getAllowMultitasking(),
            recordTagSelectionCloseAfterOne = prefsInteractor.getRecordTagSelectionCloseAfterOne(),
            enableRepeatButton = prefsInteractor.getEnableRepeatButton(),
        )
    }

    override suspend fun setSettings(settings: WearSettingsDTO) {
        prefsInteractor.setAllowMultitasking(settings.allowMultitasking)
        widgetInteractor.updateWidgets(listOf(WidgetType.QUICK_SETTINGS))
        settingsDataUpdateInteractor.send()
    }

    override suspend fun openPhoneApp() {
        router.startApp()
    }
}