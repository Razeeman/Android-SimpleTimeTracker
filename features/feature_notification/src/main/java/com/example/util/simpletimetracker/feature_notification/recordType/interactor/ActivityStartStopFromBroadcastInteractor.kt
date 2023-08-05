package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import javax.inject.Inject

class ActivityStartStopFromBroadcastInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordInteractor: RecordInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
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
            limit = 1,
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

    suspend fun onActionTypeClick(
        typeId: Long,
        selectedTypeId: Long,
        typesShift: Int,
    ) {
        val started = addRunningRecordMediator.tryStartTimer(selectedTypeId) {
            notificationTypeInteractor.checkAndShow(
                typeId = typeId,
                typesShift = typesShift,
                selectedTypeId = selectedTypeId,
            )
        }
        if (started) {
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

        val tags = recordTagInteractor.getAll()
            .filter { it.name == name && !it.archived }

        return tags.firstOrNull { it.typeId == typeId }?.id // First return typed tag.
            ?: tags.firstOrNull { it.typeId == 0L }?.id // If no typed - return general.
    }
}