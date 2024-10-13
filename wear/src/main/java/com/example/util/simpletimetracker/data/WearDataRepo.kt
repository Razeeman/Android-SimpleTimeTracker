/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import com.example.util.simpletimetracker.complication.WearComplicationManager
import com.example.util.simpletimetracker.domain.model.WearActivity
import com.example.util.simpletimetracker.domain.model.WearCurrentActivity
import com.example.util.simpletimetracker.domain.model.WearRecordRepeatResult
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.domain.model.WearTag
import com.example.util.simpletimetracker.notification.WearNotificationManager
import com.example.util.simpletimetracker.wear_api.WearActivityDTO
import com.example.util.simpletimetracker.wear_api.WearCurrentActivityDTO
import com.example.util.simpletimetracker.wear_api.WearSettingsDTO
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagSelectionRequest
import com.example.util.simpletimetracker.wear_api.WearStartActivityRequest
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
    private var currentActivitiesCache: List<WearCurrentActivityDTO>? = null
    private var settingsCache: WearSettingsDTO? = null
    private val mutex: Mutex = Mutex()

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
    ): Result<List<WearActivity>> = mutex.withLock {
        return runCatching {
            val data = activitiesCache.takeUnless { forceReload }
                ?: wearRPCClient.queryActivities()
                    .also { activitiesCache = it }
            data.map(wearDataLocalMapper::map)
        }
    }

    suspend fun loadCurrentActivities(
        forceReload: Boolean,
    ): Result<List<WearCurrentActivity>> = mutex.withLock {
        return runCatching {
            val data = currentActivitiesCache.takeUnless { forceReload }
                ?: wearRPCClient.queryCurrentActivities()
                    .also { currentActivitiesCache = it }
            data.map(wearDataLocalMapper::map)
        }
    }

    suspend fun startActivity(id: Long, tagIds: List<Long>): Result<Unit> = mutex.withLock {
        return runCatching {
            val request = WearStartActivityRequest(id, tagIds)
            wearRPCClient.startActivity(request)
        }
    }

    suspend fun stopActivity(id: Long): Result<Unit> = mutex.withLock {
        return runCatching {
            val request = WearStopActivityRequest(id)
            wearRPCClient.stopActivity(request)
        }
    }

    suspend fun repeatActivity(): Result<WearRecordRepeatResult> = mutex.withLock {
        return runCatching {
            val response = wearRPCClient.repeatActivity()
            wearDataLocalMapper.map(response)
        }
    }

    suspend fun loadTagsForActivity(activityId: Long): Result<List<WearTag>> = mutex.withLock {
        return runCatching {
            val data = wearRPCClient.queryTagsForActivity(activityId)
            data.map(wearDataLocalMapper::map)
        }
    }

    suspend fun loadShouldShowTagSelection(activityId: Long): Result<Boolean> = mutex.withLock {
        return runCatching {
            val request = WearShouldShowTagSelectionRequest(activityId)
            wearRPCClient.queryShouldShowTagSelection(request).shouldShow
        }
    }

    suspend fun loadSettings(
        forceReload: Boolean,
    ): Result<WearSettings> = mutex.withLock {
        return runCatching {
            val data = settingsCache.takeUnless { forceReload }
                ?: wearRPCClient.querySettings()
                    .also { settingsCache = it }
            data.let(wearDataLocalMapper::map)
        }
    }

    suspend fun setSettings(settings: WearSettings): Result<Unit> = mutex.withLock {
        return runCatching {
            val data = wearDataLocalMapper.map(settings)
            wearRPCClient.setSettings(data)
        }
    }

    suspend fun openAppPhone(): Result<Unit> = mutex.withLock {
        return runCatching { wearRPCClient.openPhoneApp() }
    }
}