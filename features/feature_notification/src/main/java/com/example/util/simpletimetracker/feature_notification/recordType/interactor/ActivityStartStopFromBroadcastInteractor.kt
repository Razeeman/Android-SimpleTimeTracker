package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.domain.REPEAT_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orEmpty
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationActivitySwitchInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordsUpdateInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager
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
    private val notificationActivitySwitchInteractor: NotificationActivitySwitchInteractor,
    private val recordRepeatInteractor: RecordRepeatInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val recordsUpdateInteractor: RecordsUpdateInteractor,
    private val completeTypesStateInteractor: CompleteTypesStateInteractor,
) {

    suspend fun onActionActivityStart(
        name: String,
        comment: String?,
        tagNames: List<String>,
    ) {
        val typeId = getTypeIdByName(name) ?: return
        val runningRecord = runningRecordInteractor.get(typeId)
        if (runningRecord != null) return // Already running.
        val tagIds = findTagIdByName(tagNames, typeId)

        addRunningRecordMediator.startTimer(
            typeId = typeId,
            comment = comment.orEmpty(),
            tagIds = tagIds,
        )
    }

    suspend fun onActionActivityStop(
        name: String,
    ) {
        val typeId = getTypeIdByName(name) ?: return
        onActionActivityStop(typeId = typeId, fromControls = false)
    }

    suspend fun onActionActivityStop(
        typeId: Long,
        fromControls: Boolean,
    ) {
        val runningRecord = runningRecordInteractor.get(typeId)
            ?: return // Not running.

        removeRunningRecordMediator.removeWithRecordAdd(
            runningRecord = runningRecord,
            updateNotificationSwitch = !fromControls,
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
        tagNames: List<String>,
    ) {
        val previousRecord = recordInteractor.getPrev(
            timeStarted = System.currentTimeMillis(),
        ).firstOrNull() ?: return
        val typeId = previousRecord.typeId
        val tagIds = findTagIdByName(tagNames, typeId)

        addRunningRecordMediator.startTimer(
            typeId = typeId,
            comment = comment
                ?: previousRecord.comment,
            tagIds = tagIds
                .takeUnless { tagNames.isEmpty() }
                ?: previousRecord.tagIds,
        )
    }

    suspend fun onRecordAdd(
        name: String,
        timeStarted: String,
        timeEnded: String,
        comment: String?,
        tagNames: List<String>,
    ) {
        val typeId = getTypeIdByName(name) ?: return
        val newTimeStarted = parseTimestamp(timeStarted) ?: return
        val newTimeEnded = parseTimestamp(timeEnded) ?: return
        val tagIds = findTagIdByName(tagNames, typeId)

        Record(
            id = 0, // Zero creates new record.
            typeId = typeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeEnded,
            comment = comment.orEmpty(),
            tagIds = tagIds,
        ).let {
            addRecordMediator.add(it)
            recordsUpdateInteractor.send()
        }
    }

    suspend fun onActionTypeClick(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        typesShift: Int,
    ) {
        if (selectedTypeId == REPEAT_BUTTON_ITEM_ID) {
            recordRepeatInteractor.repeat()
            return
        }

        // Switch controls are updated separately right from here,
        // so no need to update after record change.
        if (from is NotificationControlsManager.From.ActivitySwitch) {
            if (runningRecordInteractor.get(selectedTypeId) != null) {
                onActionActivityStop(
                    typeId = selectedTypeId,
                    fromControls = true,
                )
                update(from, typesShift)
                return
            }
        }
        val updateNotificationSwitch = from !is NotificationControlsManager.From.ActivitySwitch
        val started = addRunningRecordMediator.tryStartTimer(
            typeId = selectedTypeId,
            updateNotificationSwitch = updateNotificationSwitch,
        ) {
            // Update to show tag selection.
            update(
                from = from,
                typesShift = typesShift,
                tagsShift = 0,
                selectedTypeId = selectedTypeId,
            )
        }
        if (started) {
            val type = recordTypeInteractor.get(selectedTypeId)
            if (type?.defaultDuration.orZero() > 0) {
                completeTypesStateInteractor.notificationTypeIds += selectedTypeId
                update(from, typesShift)
                delay(500)
                completeTypesStateInteractor.notificationTypeIds -= selectedTypeId
                update(from, typesShift)
            }

            update(from, typesShift)
        }
    }

    suspend fun onActionTagClick(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        tagId: Long,
        typesShift: Int,
    ) {
        addRunningRecordMediator.startTimer(
            typeId = selectedTypeId,
            comment = "",
            tagIds = listOfNotNull(tagId.takeUnless { it == 0L }),
            updateNotificationSwitch = from !is NotificationControlsManager.From.ActivitySwitch,
        )
        update(from, typesShift)
    }

    suspend fun onRequestUpdate(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        typesShift: Int,
        tagsShift: Int,
    ) {
        update(
            from = from,
            typesShift = typesShift,
            tagsShift = tagsShift,
            selectedTypeId = selectedTypeId,
        )
    }

    private suspend fun update(
        from: NotificationControlsManager.From,
        typesShift: Int,
        tagsShift: Int = 0,
        selectedTypeId: Long = 0,
    ) {
        when (from) {
            is NotificationControlsManager.From.ActivityNotification -> {
                val typeId = from.recordTypeId
                if (typeId == 0L) return
                notificationTypeInteractor.checkAndShow(
                    typeId = from.recordTypeId,
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                    selectedTypeId = selectedTypeId,
                )
            }
            is NotificationControlsManager.From.ActivitySwitch -> {
                notificationActivitySwitchInteractor.updateNotification(
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                    selectedTypeId = selectedTypeId,
                )
            }
        }
    }

    private suspend fun getTypeIdByName(name: String): Long? {
        return recordTypeInteractor.getAll().firstOrNull { it.name == name }?.id
    }

    private suspend fun findTagIdByName(
        names: List<String>,
        typeId: Long,
    ): List<Long> {
        if (names.isEmpty()) return emptyList()
        return getSelectableTagsInteractor.execute(typeId)
            .filter { it.name in names && !it.archived }
            .map { it.id }
            .orEmpty()
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