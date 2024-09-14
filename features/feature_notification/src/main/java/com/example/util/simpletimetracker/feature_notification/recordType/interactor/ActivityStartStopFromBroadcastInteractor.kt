package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.domain.REPEAT_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordsUpdateInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.Record
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ActivityStartStopFromBroadcastInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val addRecordMediator: AddRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordInteractor: RecordInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val recordRepeatInteractor: RecordRepeatInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val recordsUpdateInteractor: RecordsUpdateInteractor,
    private val completeTypesStateInteractor: CompleteTypesStateInteractor,
) {

    suspend fun onActionActivityStart(
        name: String,
        comment: String?,
        tagName: String?,
    ) {
        val typeId = getTypeIdByName(name) ?: return
        val runningRecord = runningRecordInteractor.get(typeId)
        if (runningRecord != null) return // Already running.
        val tagId = findTagIdByName(tagName, typeId)

        addRunningRecordMediator.startTimer(
            typeId = typeId,
            comment = comment.orEmpty(),
            tagIds = listOfNotNull(tagId),
        )
    }

    suspend fun onActionActivityStop(
        name: String,
    ) {
        val typeId = getTypeIdByName(name) ?: return
        onActionActivityStop(typeId)
    }

    suspend fun onActionActivityStop(
        typeId: Long,
    ) {
        val runningRecord = runningRecordInteractor.get(typeId)
            ?: return // Not running.

        removeRunningRecordMediator.removeWithRecordAdd(
            runningRecord = runningRecord,
        )
    }

    suspend fun onActionActivityStopAll() {
        runningRecordInteractor.getAll()
            .forEach { removeRunningRecordMediator.removeWithRecordAdd(it) }
    }

    suspend fun onActionActivityStopShortest() {
        runningRecordInteractor.getAll()
            .maxByOrNull { it.timeStarted }
            ?.let { removeRunningRecordMediator.removeWithRecordAdd(it) }
    }

    suspend fun onActionActivityStopLongest() {
        runningRecordInteractor.getAll()
            .minByOrNull { it.timeStarted }
            ?.let { removeRunningRecordMediator.removeWithRecordAdd(it) }
    }

    suspend fun onActionActivityRestart(
        comment: String?,
        tagName: String?,
    ) {
        val previousRecord = recordInteractor.getPrev(
            timeStarted = System.currentTimeMillis(),
        ).firstOrNull() ?: return
        val typeId = previousRecord.typeId
        val tagId = findTagIdByName(tagName, typeId)

        addRunningRecordMediator.startTimer(
            typeId = typeId,
            comment = comment
                ?: previousRecord.comment,
            tagIds = listOfNotNull(tagId)
                .takeUnless { tagName == null }
                ?: previousRecord.tagIds,
        )
    }

    suspend fun onRecordAdd(
        name: String,
        timeStarted: String,
        timeEnded: String,
        comment: String?,
        tagName: String?,
    ) {
        val typeId = getTypeIdByName(name) ?: return
        val newTimeStarted = parseTimestamp(timeStarted) ?: return
        val newTimeEnded = parseTimestamp(timeEnded) ?: return
        val tagId = findTagIdByName(tagName, typeId)

        Record(
            id = 0, // Zero creates new record.
            typeId = typeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeEnded,
            comment = comment.orEmpty(),
            tagIds = listOfNotNull(tagId),
        ).let {
            addRecordMediator.add(it)
            recordsUpdateInteractor.send()
        }
    }

    suspend fun onActionTypeClick(
        typeId: Long,
        selectedTypeId: Long,
        typesShift: Int,
    ) {
        if (selectedTypeId == REPEAT_BUTTON_ITEM_ID) {
            recordRepeatInteractor.repeat()
            return
        }
        val started = addRunningRecordMediator.tryStartTimer(selectedTypeId) {
            notificationTypeInteractor.checkAndShow(
                typeId = typeId,
                typesShift = typesShift,
                selectedTypeId = selectedTypeId,
            )
        }
        if (started) {
            val type = recordTypeInteractor.get(selectedTypeId)
            if (type?.defaultDuration.orZero() > 0) {
                completeTypesStateInteractor.notificationTypeIds += selectedTypeId
                notificationTypeInteractor.checkAndShow(
                    typeId = typeId,
                    typesShift = typesShift,
                )
                delay(500)
                completeTypesStateInteractor.notificationTypeIds -= selectedTypeId
                notificationTypeInteractor.checkAndShow(
                    typeId = typeId,
                    typesShift = typesShift,
                )
            }

            notificationTypeInteractor.checkAndShow(
                typeId = typeId,
                typesShift = typesShift,
            )
        }
    }

    suspend fun onActionTagClick(
        typeId: Long,
        selectedTypeId: Long,
        tagId: Long,
        typesShift: Int,
    ) {
        addRunningRecordMediator.startTimer(
            typeId = selectedTypeId,
            comment = "",
            tagIds = listOfNotNull(tagId.takeUnless { it == 0L }),
        )
        notificationTypeInteractor.checkAndShow(
            typeId = typeId,
            typesShift = typesShift,
        )
    }

    private suspend fun getTypeIdByName(name: String): Long? {
        return recordTypeInteractor.getAll().firstOrNull { it.name == name }?.id
    }

    private suspend fun findTagIdByName(
        name: String?,
        typeId: Long,
    ): Long? {
        name ?: return null
        return getSelectableTagsInteractor.execute(typeId)
            .firstOrNull { it.name == name && !it.archived }?.id
    }

    /**
     * Supported formats:
     * [dateTimeFormat],
     * UTC timestamp in milliseconds.
     */
    private fun parseTimestamp(timeString: String): Long? {
        return parseDateTime(timeString)
            ?: timeString.toLongOrNull()
    }

    private fun parseDateTime(timeString: String): Long? {
        return synchronized(dateTimeFormat) {
            runCatching {
                dateTimeFormat.parse(timeString)
            }.getOrNull()?.time
        }
    }

    companion object {
        private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}