/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import com.example.util.simpletimetracker.wear_api.WearActivityDTO
import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import com.example.util.simpletimetracker.wear_api.WearCurrentStateDTO
import com.example.util.simpletimetracker.wear_api.WearRecordRepeatResponse
import com.example.util.simpletimetracker.wear_api.WearRequests
import com.example.util.simpletimetracker.wear_api.WearSetSettingsRequest
import com.example.util.simpletimetracker.wear_api.WearSettingsDTO
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagSelectionRequest
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagSelectionResponse
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagValueSelectionRequest
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagValueSelectionResponse
import com.example.util.simpletimetracker.wear_api.WearStartActivityRequest
import com.example.util.simpletimetracker.wear_api.WearStatisticsDTO
import com.example.util.simpletimetracker.wear_api.WearStatisticsRequest
import com.example.util.simpletimetracker.wear_api.WearRecordsRequest
import com.example.util.simpletimetracker.wear_api.WearRecordDTO
import com.example.util.simpletimetracker.wear_api.WearStopActivityRequest
import com.example.util.simpletimetracker.wear_api.WearTagDTO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

// Watch side.
@Singleton
class WearRPCClient @Inject constructor(
    private val messenger: WearMessenger,
) : WearCommunicationAPI {

    private val gson = Gson()

    override suspend fun queryActivities(): List<WearActivityDTO> {
        val response: List<WearActivityDTO>? = messenger
            .send(WearRequests.QUERY_ACTIVITIES)
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException
    }

    override suspend fun queryCurrentActivities(): WearCurrentStateDTO {
        val response: WearCurrentStateDTO? = messenger
            .send(WearRequests.QUERY_CURRENT_ACTIVITIES)
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException
    }

    override suspend fun queryStatistics(request: WearStatisticsRequest): List<WearStatisticsDTO> {
        val response: List<WearStatisticsDTO>? = messenger
            .send(WearRequests.QUERY_STATISTICS, mapToBytes(request))
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException
    }

    override suspend fun queryRecords(request: WearRecordsRequest): List<WearRecordDTO> {
        val response: List<WearRecordDTO>? = messenger
            .send(WearRequests.QUERY_RECORDS, mapToBytes(request))
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException
    }

    override suspend fun startActivity(request: WearStartActivityRequest) {
        messenger.send(WearRequests.START_ACTIVITY, mapToBytes(request))
    }

    override suspend fun stopActivity(request: WearStopActivityRequest) {
        messenger.send(WearRequests.STOP_ACTIVITY, mapToBytes(request))
    }

    override suspend fun repeatActivity(): WearRecordRepeatResponse {
        val response: WearRecordRepeatResponse? = messenger
            .send(WearRequests.REPEAT_ACTIVITY)
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException
    }

    override suspend fun queryTagsForActivity(activityId: Long): List<WearTagDTO> {
        val response: List<WearTagDTO>? = messenger
            .send(WearRequests.QUERY_TAGS_FOR_ACTIVITY, mapToBytes(activityId))
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException
    }

    override suspend fun queryShouldShowTagSelection(
        request: WearShouldShowTagSelectionRequest,
    ): WearShouldShowTagSelectionResponse {
        val response: WearShouldShowTagSelectionResponse? = messenger
            .send(WearRequests.QUERY_SHOULD_SHOW_TAG_SELECTION, mapToBytes(request))
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException
    }

    override suspend fun queryShouldShowTagValueSelection(
        request: WearShouldShowTagValueSelectionRequest,
    ): WearShouldShowTagValueSelectionResponse {
        val response: WearShouldShowTagValueSelectionResponse? = messenger
            .send(WearRequests.QUERY_SHOULD_SHOW_TAG_VALUE_SELECTION, mapToBytes(request))
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException
    }

    override suspend fun querySettings(): WearSettingsDTO {
        val response: WearSettingsDTO? = messenger
            .send(WearRequests.QUERY_SETTINGS)
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException
    }

    override suspend fun setSettings(settings: WearSetSettingsRequest) {
        messenger.send(WearRequests.SET_SETTINGS, mapToBytes(settings))
    }

    override suspend fun openPhoneApp() {
        messenger.send(WearRequests.OPEN_PHONE_APP)
    }

    fun addListener(
        onDataChanged: suspend () -> Unit,
    ) {
        messenger.addListener(onDataChanged)
    }

    private fun <T> mapToBytes(data: T): ByteArray {
        return gson.toJson(data).toByteArray()
    }

    private inline fun <reified T> mapFromBytes(data: ByteArray): T? {
        return runCatching {
            val collectionType = object : TypeToken<T>() {}.type
            gson.fromJson<T>(String(data), collectionType)
        }.getOrNull()
    }
}