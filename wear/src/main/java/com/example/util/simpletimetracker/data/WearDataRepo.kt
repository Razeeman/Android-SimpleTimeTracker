/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import com.example.util.simpletimetracker.complication.WearComplicationManager
import com.example.util.simpletimetracker.domain.model.WearActivity
import com.example.util.simpletimetracker.domain.model.WearCurrentState
import com.example.util.simpletimetracker.domain.model.WearRecordRepeatResult
import com.example.util.simpletimetracker.domain.model.WearSetSettings
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.domain.model.WearStatistics
import com.example.util.simpletimetracker.domain.model.WearTag
import com.example.util.simpletimetracker.domain.model.WearRecordTag
import com.example.util.simpletimetracker.domain.model.WearShouldShowTagSelectionResult
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.notification.WearNotificationManager
import com.example.util.simpletimetracker.wear_api.WearActivityDTO
import com.example.util.simpletimetracker.wear_api.WearCurrentStateDTO
import com.example.util.simpletimetracker.wear_api.WearSettingsDTO
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagSelectionRequest
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagValueSelectionRequest
import com.example.util.simpletimetracker.wear_api.WearStartActivityRequest
import com.example.util.simpletimetracker.wear_api.WearStatisticsDTO
import com.example.util.simpletimetracker.wear_api.WearStatisticsRequest
import com.example.util.simpletimetracker.wear_api.WearStopActivityRequest
import dagger.Lazy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearDataRepo @Inject constructor(
    private val wearRPCClient: WearRPCClient,
    private val wearComplicationManager: WearComplicationManager,
    private val wearNotificationManager: Lazy<WearNotificationManager>,
    private val wearDataLocalMapper: WearDataLocalMapper,
) {

    val dataUpdated: SharedFlow<Unit> get() = _dataUpdated.asSharedFlow()

    private var _dataUpdated: MutableSharedFlow<Unit> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var activitiesCache: List<WearActivityDTO>? = null
    private var statisticsCache: List<WearStatisticsDTO>? = null
    private var currentActivitiesCache: WearCurrentStateDTO? = null
    private var settingsCache: WearSettingsDTO? = null
    private val activitiesMutex: Mutex = Mutex()
    private val statisticsMutex: Mutex = Mutex()
    private val currentActivitiesMutex: Mutex = Mutex()
    private val settingsMutex: Mutex = Mutex()

    init {
        wearRPCClient.addListener {
            coroutineScope {
                val deferred = mutableListOf<Deferred<Any>>()
                deferred += async { loadActivities(forceReload = true) }
                deferred += async { loadCurrentActivities(forceReload = true) }
                deferred += async { loadSettings(forceReload = true) }
                deferred.awaitAll()
                wearComplicationManager.updateComplications()
                wearNotificationManager.get().updateNotifications()
                _dataUpdated.emit(Unit)
            }
        }
    }

    suspend fun loadActivities(
        forceReload: Boolean,
    ): Result<List<WearActivity>> = activitiesMutex.withLock {
        return runCatching {
            val data = activitiesCache.takeUnless { forceReload }
                ?: wearRPCClient.queryActivities()
                    .also { activitiesCache = it }
            data.map(wearDataLocalMapper::map)
        }
    }

    suspend fun loadCurrentActivities(
        forceReload: Boolean,
    ): Result<WearCurrentState> = currentActivitiesMutex.withLock {
        return runCatching {
            val data = currentActivitiesCache.takeUnless { forceReload }
                ?: wearRPCClient.queryCurrentActivities()
                    .also { currentActivitiesCache = it }
            data.let(wearDataLocalMapper::map)
        }
    }

    suspend fun loadStatistics(
        forceReload: Boolean,
        shift: Int,
        filterType: ChartFilterType,
    ): Result<List<WearStatistics>> = statisticsMutex.withLock {
        return runCatching {
            val request = WearStatisticsRequest(
                shift = shift,
                filterType = wearDataLocalMapper.map(filterType),
            )
            val data = statisticsCache.takeUnless { forceReload }
                ?: wearRPCClient.queryStatistics(request)
                    .also { statisticsCache = it }
            data.map(wearDataLocalMapper::map)
        }
    }

    suspend fun startActivity(
        id: Long,
        tags: List<WearRecordTag>,
        useSelectedTags: Boolean,
    ): Result<Unit> {
        return runCatching {
            val request = WearStartActivityRequest(
                id = id,
                tags = tags.map {
                    WearStartActivityRequest.Tag(
                        tagId = it.tagId,
                        numericValue = it.numericValue,
                    )
                },
                useSelectedTags = useSelectedTags,
            )
            wearRPCClient.startActivity(request)
        }
    }

    suspend fun stopActivity(id: Long): Result<Unit> {
        return runCatching {
            val request = WearStopActivityRequest(id)
            wearRPCClient.stopActivity(request)
        }
    }

    suspend fun repeatActivity(): Result<WearRecordRepeatResult> {
        return runCatching {
            val response = wearRPCClient.repeatActivity()
            wearDataLocalMapper.map(response)
        }
    }

    suspend fun loadTagsForActivity(activityId: Long): Result<List<WearTag>> {
        return runCatching {
            val data = wearRPCClient.queryTagsForActivity(activityId)
            data.map(wearDataLocalMapper::map)
        }
    }

    suspend fun loadShouldShowTagSelection(
        activityId: Long,
    ): Result<WearShouldShowTagSelectionResult> {
        return runCatching {
            val request = WearShouldShowTagSelectionRequest(activityId)
            val data = wearRPCClient.queryShouldShowTagSelection(request)
            wearDataLocalMapper.map(data)
        }
    }

    suspend fun loadShouldShowTagValueSelection(
        selectedTagIds: List<Long>,
        clickedTagId: Long,
    ): Result<Boolean> {
        return runCatching {
            val request = WearShouldShowTagValueSelectionRequest(selectedTagIds, clickedTagId)
            wearRPCClient.queryShouldShowTagValueSelection(request).shouldShow
        }
    }

    suspend fun loadSettings(
        forceReload: Boolean,
    ): Result<WearSettings> = settingsMutex.withLock {
        return runCatching {
            val data = settingsCache.takeUnless { forceReload }
                ?: wearRPCClient.querySettings()
                    .also { settingsCache = it }
            data.let(wearDataLocalMapper::map)
        }
    }

    suspend fun setSettings(settings: WearSetSettings): Result<Unit> {
        return runCatching {
            val data = wearDataLocalMapper.map(settings)
            wearRPCClient.setSettings(data)
        }
    }

    suspend fun openAppPhone(): Result<Unit> {
        return runCatching { wearRPCClient.openPhoneApp() }
    }
}