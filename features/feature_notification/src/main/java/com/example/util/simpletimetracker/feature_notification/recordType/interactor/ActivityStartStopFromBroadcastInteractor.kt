package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.interactor.IsMultipleTagChoiceAvailableInteractor
import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivitySwitchInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.interactor.NeedTagValueSelectionInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager
import com.example.util.simpletimetracker.feature_notification.core.TAG_VALUE_DECIMAL_DELIMITER
import com.example.util.simpletimetracker.feature_notification.core.TAG_VALUE_MINUS_SIGN
import kotlinx.coroutines.delay
import javax.inject.Inject

class ActivityStartStopFromBroadcastInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationActivitySwitchInteractor: NotificationActivitySwitchInteractor,
    private val recordRepeatInteractor: RecordRepeatInteractor,
    private val completeTypesStateInteractor: CompleteTypesStateInteractor,
    private val needTagValueSelectionInteractor: NeedTagValueSelectionInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val isMultipleTagChoiceAvailableInteractor: IsMultipleTagChoiceAvailableInteractor,
) {

    suspend fun onActionActivityStop(
        typeId: Long,
    ) {
        val runningRecord = runningRecordInteractor.get(typeId) ?: run {
            notificationTypeInteractor.checkAndHide(typeId)
            return // Not running.
        }

        removeRunningRecordMediator.removeWithRecordAdd(
            runningRecord = runningRecord,
        )
    }

    suspend fun onActionTypeClick(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        typesShift: Int,
    ) {
        val started = addRunningRecordMediator.tryStartTimer(
            typeId = selectedTypeId,
            // Switch controls are updated separately right from here,
            // so no need to update after record change.
            updateNotificationSwitch = false,
            commentInputAvailable = false, // TODO open activity? Or RemoteInput?
        ) {
            val preselectedTags = it.preselectedTags
            val requiredValueSelectionTagIds = it.requiredValueSelectionTagIds
            val isMultipleTagAvailable = isMultipleTagChoiceAvailable(
                selectedTypeId = selectedTypeId,
            )

            // Update to show tag selection.
            update(
                from = from,
                typesShift = typesShift,
                tagsShift = 0,
                selectedTypeId = selectedTypeId,
                isMultipleTagAvailable = isMultipleTagAvailable,
                selectedTags = preselectedTags,
                editingTagId = requiredValueSelectionTagIds.firstOrNull { id ->
                    isRequiredTagValueSelectionMissingValue(preselectedTags, id)
                },
                editingTagValueInput = null,
                requiredValueSelectionTagIds = requiredValueSelectionTagIds,
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
            } else {
                update(from, typesShift)
            }
        }
    }

    suspend fun onActionRepeat() {
        recordRepeatInteractor.repeat()
    }

    suspend fun onActionApplyTags(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        selectedTags: List<RecordBase.Tag>,
        typesShift: Int,
    ) {
        startFromTagSelection(
            from = from,
            selectedTypeId = selectedTypeId,
            selectedTags = selectedTags,
            typesShift = typesShift,
        )
    }

    suspend fun onActionClearTags(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        typesShift: Int,
        tagsShift: Int,
        isMultipleTagAvailable: Boolean,
        requiredValueSelectionTagIds: List<Long>,
    ) {
        if (isMultipleTagAvailable) {
            update(
                from = from,
                typesShift = typesShift,
                tagsShift = tagsShift,
                selectedTypeId = selectedTypeId,
                isMultipleTagAvailable = true,
                selectedTags = emptyList(), // Reset tags.
                editingTagId = null,
                editingTagValueInput = null,
                requiredValueSelectionTagIds = requiredValueSelectionTagIds,
            )
        } else {
            startFromTagSelection(
                from = from,
                selectedTypeId = selectedTypeId,
                selectedTags = emptyList(),
                typesShift = typesShift,
            )
        }
    }

    suspend fun onActionTagClick(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        tagId: Long,
        typesShift: Int,
        selectedTags: List<RecordBase.Tag>,
        tagsShift: Int,
        isMultipleTagAvailable: Boolean,
        requiredValueSelectionTagIds: List<Long>,
    ) {
        if (!isMultipleTagAvailable && selectedTags.any { it.tagId == tagId }) {
            // Disallow deselection for preselected tags.
            return
        }

        val currentTagIds = selectedTags.map(RecordBase.Tag::tagId)
        if (tagId in currentTagIds) {
            val updatedTags = selectedTags.filterNot { it.tagId == tagId }
            update(
                from = from,
                typesShift = typesShift,
                tagsShift = tagsShift,
                selectedTypeId = selectedTypeId,
                isMultipleTagAvailable = isMultipleTagAvailable,
                selectedTags = updatedTags,
                editingTagId = null,
                editingTagValueInput = null,
                requiredValueSelectionTagIds = requiredValueSelectionTagIds,
            )
            return
        }

        val needValueSelection = needTagValueSelectionInteractor.execute(
            selectedTagIds = currentTagIds,
            clickedTagId = tagId,
        )
        if (needValueSelection) {
            update(
                from = from,
                typesShift = typesShift,
                tagsShift = tagsShift,
                selectedTypeId = selectedTypeId,
                isMultipleTagAvailable = isMultipleTagAvailable,
                selectedTags = selectedTags,
                editingTagId = tagId,
                editingTagValueInput = null,
                requiredValueSelectionTagIds = requiredValueSelectionTagIds,
            )
            return
        }

        val updatedTags = selectedTags
            .filterNot { it.tagId == tagId } + RecordBase.Tag(
            tagId = tagId,
            numericValue = null,
        )
        maybeStartWithSelectedTags(
            from = from,
            selectedTypeId = selectedTypeId,
            selectedTags = updatedTags,
            editingTagId = null,
            editingTagValueInput = null,
            typesShift = typesShift,
            tagsShift = tagsShift,
            isMultipleTagAvailable = isMultipleTagAvailable,
            requiredValueSelectionTagIds = requiredValueSelectionTagIds,
        )
    }

    suspend fun onActionTagValueSave(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        tagId: Long,
        tagValue: String?,
        typesShift: Int,
        selectedTags: List<RecordBase.Tag>,
        tagsShift: Int,
        isMultipleTagAvailable: Boolean,
        requiredValueSelectionTagIds: List<Long>,
    ) {
        val updatedTags = selectedTags
            .filterNot { it.tagId == tagId } + RecordBase.Tag(
            tagId = tagId,
            numericValue = parseTagValueInput(tagValue),
        )
        val nextRequiredTagId = requiredValueSelectionTagIds.firstOrNull { id ->
            isRequiredTagValueSelectionMissingValue(updatedTags, id)
        }
        if (tagId in requiredValueSelectionTagIds) {
            // Ignore "close after one" if tag requires value.
            update(
                from = from,
                typesShift = typesShift,
                tagsShift = tagsShift,
                selectedTypeId = selectedTypeId,
                selectedTags = updatedTags,
                editingTagId = nextRequiredTagId,
                editingTagValueInput = null,
                isMultipleTagAvailable = isMultipleTagAvailable,
                requiredValueSelectionTagIds = requiredValueSelectionTagIds,
            )
        } else {
            maybeStartWithSelectedTags(
                from = from,
                selectedTypeId = selectedTypeId,
                selectedTags = updatedTags,
                editingTagId = nextRequiredTagId,
                editingTagValueInput = null,
                typesShift = typesShift,
                tagsShift = tagsShift,
                isMultipleTagAvailable = isMultipleTagAvailable,
                requiredValueSelectionTagIds = requiredValueSelectionTagIds,
            )
        }
    }

    suspend fun onRequestUpdate(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        typesShift: Int,
        tagsShift: Int,
        selectedTags: List<RecordBase.Tag>,
        editingTagId: Long?,
        editingTagValueInput: String?,
        isMultipleTagAvailable: Boolean,
        requiredValueSelectionTagIds: List<Long>,
    ) {
        update(
            from = from,
            typesShift = typesShift,
            tagsShift = tagsShift,
            selectedTypeId = selectedTypeId,
            isMultipleTagAvailable = isMultipleTagAvailable,
            selectedTags = selectedTags,
            editingTagId = editingTagId,
            editingTagValueInput = editingTagValueInput,
            requiredValueSelectionTagIds = requiredValueSelectionTagIds,
        )
    }

    private suspend fun startFromTagSelection(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        selectedTags: List<RecordBase.Tag>,
        typesShift: Int,
    ) {
        if (from !is NotificationControlsManager.From.ActivitySwitch) {
            // Hide tag selection on current notification.
            // Switch would be hidden on start timer.
            update(from, typesShift)
        }
        addRunningRecordMediator.startTimer(
            typeId = selectedTypeId,
            comment = "",
            tags = selectedTags,
            useSelectedTags = true,
        )
    }

    private suspend fun maybeStartWithSelectedTags(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        selectedTags: List<RecordBase.Tag>,
        editingTagId: Long?,
        editingTagValueInput: String?,
        typesShift: Int,
        tagsShift: Int,
        isMultipleTagAvailable: Boolean,
        requiredValueSelectionTagIds: List<Long>,
    ) {
        if (editingTagId == null && !isMultipleTagAvailable) {
            startFromTagSelection(
                from = from,
                selectedTypeId = selectedTypeId,
                selectedTags = selectedTags,
                typesShift = typesShift,
            )
            return
        }

        update(
            from = from,
            typesShift = typesShift,
            tagsShift = tagsShift,
            selectedTypeId = selectedTypeId,
            selectedTags = selectedTags,
            editingTagId = editingTagId,
            editingTagValueInput = editingTagValueInput,
            isMultipleTagAvailable = isMultipleTagAvailable,
            requiredValueSelectionTagIds = requiredValueSelectionTagIds,
        )
    }

    private suspend fun update(
        from: NotificationControlsManager.From,
        typesShift: Int,
    ) {
        update(
            from = from,
            typesShift = typesShift,
            tagsShift = 0,
            selectedTypeId = 0L,
            isMultipleTagAvailable = false,
            selectedTags = emptyList(),
            editingTagId = null,
            editingTagValueInput = null,
            requiredValueSelectionTagIds = emptyList(),
        )
    }

    private suspend fun update(
        from: NotificationControlsManager.From,
        typesShift: Int,
        tagsShift: Int,
        selectedTypeId: Long,
        isMultipleTagAvailable: Boolean,
        selectedTags: List<RecordBase.Tag>,
        editingTagId: Long?,
        editingTagValueInput: String?,
        requiredValueSelectionTagIds: List<Long>,
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
                    selectedTags = selectedTags,
                    editingTagId = editingTagId,
                    editingTagValueInput = editingTagValueInput,
                    isMultipleTagAvailable = isMultipleTagAvailable,
                    requiredValueSelectionTagIds = requiredValueSelectionTagIds,
                )
            }
            is NotificationControlsManager.From.ActivitySwitch -> {
                notificationActivitySwitchInteractor.updateNotification(
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                    selectedTypeId = selectedTypeId,
                    selectedTags = selectedTags,
                    editingTagId = editingTagId,
                    editingTagValueInput = editingTagValueInput,
                    isMultipleTagAvailable = isMultipleTagAvailable,
                    requiredValueSelectionTagIds = requiredValueSelectionTagIds,
                )
            }
        }
    }

    private fun parseTagValueInput(value: String?): Double? {
        // toDoubleOrNull need a dot as a separator.
        return value
            ?.replace(TAG_VALUE_DECIMAL_DELIMITER, '.')
            ?.replace(TAG_VALUE_MINUS_SIGN, '-')
            ?.toDoubleOrNull()
    }

    private suspend fun isMultipleTagChoiceAvailable(
        selectedTypeId: Long,
    ): Boolean {
        return isMultipleTagChoiceAvailableInteractor.execute(
            typeId = selectedTypeId,
            closeAfterOne = prefsInteractor.getRecordTagSelectionCloseAfterOne(),
            excludedActivities = prefsInteractor.getCloseAfterOneTagExcludeActivities().toSet(),
        )
    }

    private fun isRequiredTagValueSelectionMissingValue(
        newTags: List<RecordBase.Tag>,
        tagId: Long,
    ): Boolean {
        return newTags.any { it.tagId == tagId && it.numericValue == null }
    }
}