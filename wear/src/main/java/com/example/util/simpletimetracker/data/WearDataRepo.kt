/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import com.example.util.simpletimetracker.complication.WearComplicationManager
import com.example.util.simpletimetracker.notification.WearNotificationManager
import com.example.util.simpletimetracker.wear_api.WearActivity
import com.example.util.simpletimetracker.wear_api.WearCurrentActivity
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
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
) {

    val dataUpdated: SharedFlow<Unit> get() = _dataUpdated.asSharedFlow()

    private var _dataUpdated: MutableSharedFlow<Unit> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var activitiesCache: List<WearActivity>? = null
    private var currentActivitiesCache: List<WearCurrentActivity>? = null
    private val mutex: Mutex = Mutex()

    init {
        wearRPCClient.addListener {
            coroutineScope {
                val deferred = mutableListOf<Deferred<Any>>()
                deferred += async { loadActivities(forceReload = true) }
                deferred += async { loadCurrentActivities(forceReload = true) }
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
            activitiesCache.takeUnless { forceReload }
                ?: wearRPCClient.queryActivities()
                    .also { activitiesCache = it }
        }
    }

    suspend fun loadCurrentActivities(
        forceReload: Boolean,
    ): Result<List<WearCurrentActivity>> = mutex.withLock {
        return runCatching {
            currentActivitiesCache.takeUnless { forceReload }
                ?: wearRPCClient.queryCurrentActivities()
                    .also { currentActivitiesCache = it }
        }
    }

    suspend fun setCurrentActivities(starting: List<WearCurrentActivity>): Result<Unit> = mutex.withLock {
        return runCatching { wearRPCClient.setCurrentActivities(starting) }
    }

    suspend fun loadTagsForActivity(activityId: Long): Result<List<WearTag>> = mutex.withLock {
        return runCatching { wearRPCClient.queryTagsForActivity(activityId) }
    }

    suspend fun loadSettings(): Result<WearSettings> = mutex.withLock {
        return runCatching { wearRPCClient.querySettings() }
    }

    suspend fun openAppPhone(): Result<Unit> = mutex.withLock {
        return runCatching { wearRPCClient.openPhoneApp() }
    }
}