/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import com.example.util.simpletimetracker.wear_api.WearActivity
import com.example.util.simpletimetracker.wear_api.WearCurrentActivity
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearDataRepo @Inject constructor(
    private val wearRPCClient: WearRPCClient,
) {

    val dataUpdated: SharedFlow<Unit> get() = _dataUpdated.asSharedFlow()

    private var _dataUpdated: MutableSharedFlow<Unit> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        wearRPCClient.addListener { _dataUpdated.emit(Unit) }
    }

    suspend fun loadActivities(): Result<List<WearActivity>> {
        return runCatching { wearRPCClient.queryActivities() }
    }

    suspend fun loadCurrentActivities(): Result<List<WearCurrentActivity>> {
        return runCatching { wearRPCClient.queryCurrentActivities() }
    }

    suspend fun setCurrentActivities(starting: List<WearCurrentActivity>): Result<Unit> {
        return runCatching { wearRPCClient.setCurrentActivities(starting) }
    }

    suspend fun loadTagsForActivity(activityId: Long): Result<List<WearTag>> {
        return runCatching { wearRPCClient.queryTagsForActivity(activityId) }
    }

    suspend fun loadSettings(): Result<WearSettings> {
        return runCatching { wearRPCClient.querySettings() }
    }

    suspend fun openAppPhone(): Result<Unit> {
        return runCatching { wearRPCClient.openPhoneApp() }
    }
}