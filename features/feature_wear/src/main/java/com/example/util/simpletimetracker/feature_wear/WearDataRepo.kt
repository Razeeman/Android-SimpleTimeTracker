/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.core.interactor.LoadPreselectedTagsInteractor
import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.provider.ApplicationDataProvider
import com.example.util.simpletimetracker.domain.activitySuggestion.interactor.GetCurrentActivitySuggestionsInteractor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.ShouldShowRecordDataSelectionInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordDataSelectionDialogResult
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.NeedTagValueSelectionInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.statistics.interactor.SettingsDataUpdateInteractor
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.widget.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.widget.model.WidgetType
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.wear_api.WearActivityDTO
import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import com.example.util.simpletimetracker.wear_api.WearCurrentActivityDTO
import com.example.util.simpletimetracker.wear_api.WearCurrentStateDTO
import com.example.util.simpletimetracker.wear_api.WearRecordRepeatResponse
import com.example.util.simpletimetracker.wear_api.WearSetSettingsRequest
import com.example.util.simpletimetracker.wear_api.WearSettingsDTO
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagSelectionRequest
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagSelectionResponse
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagValueSelectionRequest
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagValueSelectionResponse
import com.example.util.simpletimetracker.wear_api.WearStartActivityRequest
import com.example.util.simpletimetracker.wear_api.WearStatisticsDTO
import com.example.util.simpletimetracker.wear_api.WearStatisticsRequest
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
    private val recordInteractor: RecordInteractor,
    private val shouldShowRecordDataSelectionInteractor: ShouldShowRecordDataSelectionInteractor,
    private val needTagValueSelectionInteractor: NeedTagValueSelectionInteractor,
    private val removeRunningRecordMediator: Lazy<RemoveRunningRecordMediator>,
    private val addRunningRecordMediator: Lazy<AddRunningRecordMediator>,
    private val recordRepeatInteractor: Lazy<RecordRepeatInteractor>,
    private val updateExternalViewsInteractor: Lazy<UpdateExternalViewsInteractor>,
    private val loadPreselectedTagsInteractor: Lazy<LoadPreselectedTagsInteractor>,
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val widgetInteractor: WidgetInteractor,
    private val settingsDataUpdateInteractor: SettingsDataUpdateInteractor,
    private val wearDataLocalMapper: WearDataLocalMapper,
    private val getCurrentActivitySuggestionsInteractor: GetCurrentActivitySuggestionsInteractor,
    private val statisticsMediator: StatisticsMediator,
    private val applicationDataProvider: ApplicationDataProvider,
) : WearCommunicationAPI {

    override suspend fun queryActivities(): List<WearActivityDTO> {
        return recordTypeInteractor.getAll()
            .filter { recordType -> !recordType.hidden }
            .map(wearDataLocalMapper::map)
    }

    override suspend fun queryCurrentActivities(): WearCurrentStateDTO {
        val tags = recordTagInteractor.getAll()

        fun mapTags(record: RecordBase): List<WearCurrentActivityDTO.TagDTO> {
            val tagDataMap = record.tags.associateBy { it.tagId }
            return tags.mapNotNull { tag ->
                if (tag.id !in tagDataMap.keys) return@mapNotNull null
                wearDataLocalMapper.map(
                    recordTag = tag,
                    recordTagData = tagDataMap[tag.id],
                )
            }
        }

        val recordTypesMapProvider: suspend () -> Map<Long, RecordType> = {
            recordTypeInteractor.getAll().associateBy(RecordType::id)
        }
        val runningRecords = runningRecordInteractor.getAll()
        val runningRecordsData = runningRecords.map { record ->
            wearDataLocalMapper.map(record, mapTags(record))
        }
        val prevRecordsData = recordInteractor
            .getAllPrev(timeStarted = System.currentTimeMillis())
            .map { record ->
                wearDataLocalMapper.map(record, mapTags(record))
            }
        val suggestionsData = getCurrentActivitySuggestionsInteractor.execute(
            recordTypesMapProvider = recordTypesMapProvider,
            runningRecords = runningRecords,
        ).map(RecordType::id)

        return WearCurrentStateDTO(
            currentActivities = runningRecordsData,
            lastRecords = prevRecordsData,
            suggestionIds = suggestionsData,
        )
    }

    override suspend fun queryStatistics(request: WearStatisticsRequest): List<WearStatisticsDTO>? {
        val filterType = wearDataLocalMapper.map(request.filterType ?: return null)
        val shift = request.shift ?: return null
        val rangeLength = RangeLength.Day
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val types = recordTypeInteractor.getAll().associateBy(RecordType::id)

        val filteredIds = when (filterType) {
            ChartFilterType.ACTIVITY -> prefsInteractor.getFilteredTypes()
            ChartFilterType.CATEGORY -> prefsInteractor.getFilteredCategories()
            ChartFilterType.RECORD_TAG -> prefsInteractor.getFilteredTags()
        }

        val dataHolders = statisticsMediator.getDataHolders(
            filterType = filterType,
            types = types,
        )
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = shift,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val statistics = statisticsMediator.getStatistics(
            filterType = filterType,
            filteredIds = filteredIds,
            range = range,
        ).filterNot {
            it.id in filteredIds
        }

        return statistics.map {
            wearDataLocalMapper.map(
                statistics = it,
                dataHolder = dataHolders[it.id],
            )
        }
    }

    override suspend fun startActivity(request: WearStartActivityRequest) {
        val typeId = request.id ?: return
        addRunningRecordMediator.get().startTimer(
            typeId = typeId,
            tags = request.tags.orEmpty().mapNotNull {
                RecordBase.Tag(
                    tagId = it?.tagId ?: return@mapNotNull null,
                    numericValue = it.numericValue,
                )
            },
            comment = "",
        )
        if (recordTypeInteractor.get(typeId)?.defaultDuration.orZero() > 0) {
            updateExternalViewsInteractor.get().onInstantRecordAdd()
        }
    }

    override suspend fun stopActivity(request: WearStopActivityRequest) {
        val typeId = request.id ?: return
        val current = runningRecordInteractor.get(typeId) ?: return
        removeRunningRecordMediator.get().removeWithRecordAdd(current)
    }

    override suspend fun repeatActivity(): WearRecordRepeatResponse {
        return recordRepeatInteractor.get().repeatWithoutMessage()
            .let(wearDataLocalMapper::map)
    }

    override suspend fun queryTagsForActivity(activityId: Long): List<WearTagDTO> {
        val types = recordTypeInteractor.getAll().associateBy { it.id }
        val preselectedTagIds = loadPreselectedTagsInteractor.get().execute(activityId)
        return getSelectableTagsInteractor.execute(activityId)
            .filterNot { it.archived }
            .map {
                wearDataLocalMapper.map(
                    recordTag = it,
                    types = types,
                    preselectedTagIds = preselectedTagIds,
                )
            }
    }

    override suspend fun queryShouldShowTagSelection(
        request: WearShouldShowTagSelectionRequest,
    ): WearShouldShowTagSelectionResponse? {
        val result = shouldShowRecordDataSelectionInteractor.execute(
            typeId = request.id ?: return null,
            commentInputAvailable = false,
        )
        return WearShouldShowTagSelectionResponse(
            shouldShow = RecordDataSelectionDialogResult.Field.Tags in result.fields,
        )
    }

    override suspend fun queryShouldShowTagValueSelection(
        request: WearShouldShowTagValueSelectionRequest,
    ): WearShouldShowTagValueSelectionResponse? {
        val result = needTagValueSelectionInteractor.execute(
            selectedTagIds = request.selectedTagIds ?: return null,
            clickedTagId = request.clickedTagId ?: return null,
        )
        return WearShouldShowTagValueSelectionResponse(
            shouldShow = result,
        )
    }

    override suspend fun querySettings(): WearSettingsDTO {
        return wearDataLocalMapper.map(
            apiVersion = applicationDataProvider.getWearApiVersion(),
            allowMultitasking = prefsInteractor.getAllowMultitasking(),
            recordTagSelectionCloseAfterOne = prefsInteractor.getRecordTagSelectionCloseAfterOne(),
            closeAfterOneTagExcludeActivities = prefsInteractor.getCloseAfterOneTagExcludeActivities(),
            enableRepeatButton = prefsInteractor.getEnableRepeatButton(),
            retroactiveTrackingMode = prefsInteractor.getRetroactiveTrackingMode(),
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
        )
    }

    override suspend fun setSettings(settings: WearSetSettingsRequest) {
        prefsInteractor.setAllowMultitasking(settings.allowMultitasking ?: return)
        widgetInteractor.updateWidgets(WidgetType.QUICK_SETTINGS)
        settingsDataUpdateInteractor.send()
    }

    override suspend fun openPhoneApp() {
        router.startApp()
    }
}