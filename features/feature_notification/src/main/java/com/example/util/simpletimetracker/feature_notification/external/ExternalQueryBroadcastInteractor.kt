package com.example.util.simpletimetracker.feature_notification.external

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_RESPONSE_ACTIVITIES
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_RESPONSE_RUNNING
import com.example.util.simpletimetracker.core.utils.EXTRA_DATA
import com.example.util.simpletimetracker.domain.color.mapper.AppColorMapper
import com.example.util.simpletimetracker.domain.notifications.model.ExternalAnswerType
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.wear_api.WearActivityDTO
import com.example.util.simpletimetracker.wear_api.WearCurrentActivityDTO
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Answers ACTION_EXTERNAL_QUERY_ACTIVITIES / ACTION_EXTERNAL_QUERY_RUNNING with
 * ACTION_EXTERNAL_RESPONSE_ACTIVITIES / ACTION_EXTERNAL_RESPONSE_RUNNING broadcasts,
 * so third-party apps can read current data without direct db access.
 */
class ExternalQueryBroadcastInteractor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val appColorMapper: AppColorMapper,
) {

    // TODO get from DI
    private val gson = Gson()

    suspend fun onActionQueryActivities(answerTypeData: String?) {
        if (!prefsInteractor.getAutomatedTrackingReceiveQueries()) return
        val answerType = mapAnswerType(answerTypeData)

        val types = recordTypeInteractor.getAll()
            .filter { recordType -> !recordType.hidden }

        val data = when (answerType) {
            ExternalAnswerType.JSON -> {
                gson.toJson(types.map(::mapToActivityDTO))
            }
            ExternalAnswerType.SIMPLE -> {
                types.joinToString(separator = ",") { type ->
                    "${encodeSimpleName(type.name)}:${mapColorHex(type)}"
                }
            }
        }

        sendResponse(ACTION_EXTERNAL_RESPONSE_ACTIVITIES, data)
    }

    suspend fun onActionQueryRunning(answerTypeData: String?) {
        if (!prefsInteractor.getAutomatedTrackingReceiveQueries()) return
        val answerType = mapAnswerType(answerTypeData)

        val runningRecords = runningRecordInteractor.getAll()

        val data = when (answerType) {
            ExternalAnswerType.JSON -> {
                val tags = recordTagInteractor.getAll()
                gson.toJson(runningRecords.map { mapToRunningDTO(it, tags) })
            }
            ExternalAnswerType.SIMPLE -> {
                val typeNames = recordTypeInteractor.getAll()
                    .associate { type -> type.id to type.name }
                runningRecords
                    .mapNotNull { record ->
                        val name = typeNames[record.id] ?: return@mapNotNull null
                        "${encodeSimpleName(name)}:${record.timeStarted / 1000}"
                    }
                    .joinToString(separator = ",")
            }
        }

        sendResponse(ACTION_EXTERNAL_RESPONSE_RUNNING, data)
    }

    private fun mapAnswerType(data: String?): ExternalAnswerType {
        return ExternalAnswerType.entries.firstOrNull { it.dataValue == data }
            ?: ExternalAnswerType.SIMPLE
    }

    // TODO probably would be better to use separate DTOs to allow versioning.
    private fun mapToActivityDTO(recordType: RecordType): WearActivityDTO {
        return WearActivityDTO(
            id = recordType.id,
            name = recordType.name,
            icon = recordType.icon,
            color = appColorMapper.mapToColorInt(recordType.color).toLong(),
        )
    }

    private fun mapToRunningDTO(
        record: RunningRecord,
        tags: List<RecordTag>,
    ): WearCurrentActivityDTO {
        val tagDataMap = record.tags.associateBy { it.tagId }
        val tagsData = tags.mapNotNull { tag ->
            val tagData = tagDataMap[tag.id] ?: return@mapNotNull null
            WearCurrentActivityDTO.TagDTO(
                name = tag.name,
                numericValue = tagData.numericValue,
                valueSuffix = tag.valueSuffix,
            )
        }
        return WearCurrentActivityDTO(
            id = record.id,
            startedAt = record.timeStarted,
            tags = tagsData,
        )
    }

    private fun mapColorHex(recordType: RecordType): String {
        val colorInt = appColorMapper.mapToColorInt(recordType.color)
        return String.format("%06x", colorInt and 0xFFFFFF)
    }

    private fun encodeSimpleName(name: String): String {
        // Names are URI-encoded so commas and colons don't conflict with the simple format delimiters.
        return Uri.encode(name)
    }

    private fun sendResponse(action: String, data: String) {
        Intent(action)
            .putExtra(EXTRA_DATA, data)
            .let(context::sendBroadcast)
    }
}
