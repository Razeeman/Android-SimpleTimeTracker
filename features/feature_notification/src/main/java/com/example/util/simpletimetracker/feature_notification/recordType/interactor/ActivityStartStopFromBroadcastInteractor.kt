package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import com.example.util.simpletimetracker.core.ShouldCloseAfterOneTagInteractor
import com.example.util.simpletimetracker.core.interactor.LoadPreselectedTagsInteractor
import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.domain.base.REPEAT_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivitySwitchInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.interactor.NeedTagValueSelectionInteractor
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.APPLY_TAGS_ID
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.UNTAGGED_TAG_ID
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
    private val loadPreselectedTagsInteractor: LoadPreselectedTagsInteractor,
    private val shouldCloseAfterOneTagInteractor: ShouldCloseAfterOneTagInteractor,
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
        if (selectedTypeId == REPEAT_BUTTON_ITEM_ID) {
            recordRepeatInteractor.repeat()
            return
        }

        val started = addRunningRecordMediator.tryStartTimer(
            typeId = selectedTypeId,
            // Switch controls are updated separately right from here,
            // so no need to update after record change.
            updateNotificationSwitch = false,
            commentInputAvailable = false, // TODO open activity? Or RemoteInput?
        ) {
            val preselectedTags = loadPreselectedTagsInteractor.execute(selectedTypeId)
                .map { RecordBase.Tag(tagId = it, numericValue = null) }
            val isMultipleTagAvailable = isMultipleTagChoiceAvailable(
                selectedTypeId = selectedTypeId,
                selectedTags = preselectedTags,
            )

            // Update to show tag selection.
            update(
                from = from,
                typesShift = typesShift,
                tagsShift = 0,
                selectedTypeId = selectedTypeId,
                isMultipleTagAvailable = isMultipleTagAvailable,
                selectedTags = preselectedTags,
                editingTagId = null,
                editingTagValueInput = null,
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

    suspend fun onActionTagClick(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        tagId: Long,
        typesShift: Int,
        selectedTags: List<RecordBase.Tag> = emptyList(),
        editingTagId: Long? = null,
        editingTagValueInput: String? = null,
        tagsShift: Int = 0,
        isMultipleTagAvailable: Boolean,
    ) {
        if (tagId == APPLY_TAGS_ID) {
            startFromTagSelection(
                from = from,
                selectedTypeId = selectedTypeId,
                selectedTags = selectedTags,
                typesShift = typesShift,
            )
            return
        }
        if (tagId == UNTAGGED_TAG_ID) {
            startFromTagSelection(
                from = from,
                selectedTypeId = selectedTypeId,
                selectedTags = emptyList(),
                typesShift = typesShift,
            )
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
                editingTagValueInput = getExistingTagValueInput(
                    tagId = tagId,
                    selectedTags = selectedTags,
                    editingTagId = editingTagId,
                    editingTagValueInput = editingTagValueInput,
                ),
            )
            return
        }

        val updatedTags = selectedTags + RecordBase.Tag(
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
        )
    }

    suspend fun onActionTagValueSave(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        tagId: Long,
        tagValue: String?,
        typesShift: Int,
        selectedTags: List<RecordBase.Tag> = emptyList(),
        tagsShift: Int = 0,
        isMultipleTagAvailable: Boolean,
    ) {
        val actualTagValue = parseTagValueInput(tagValue)
        val updatedTags = selectedTags
            .filterNot { it.tagId == tagId } + RecordBase.Tag(
            tagId = tagId,
            numericValue = actualTagValue,
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
        )
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
        )
    }

    private fun getExistingTagValueInput(
        tagId: Long,
        selectedTags: List<RecordBase.Tag>,
        editingTagId: Long?,
        editingTagValueInput: String?,
    ): String? {
        if (editingTagId == tagId) {
            return editingTagValueInput
        }
        return selectedTags
            .firstOrNull { it.tagId == tagId }
            ?.numericValue
            ?.let(::formatTagValueInput)
    }

    private fun formatTagValueInput(value: Double?): String? {
        return value
            ?.toString()
            ?.replace('.', TAG_VALUE_DECIMAL_DELIMITER)
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
                )
            }
        }
    }

    private fun parseTagValueInput(value: String?): Double? {
        return value
            ?.replace(TAG_VALUE_DECIMAL_DELIMITER, '.')
            ?.replace(TAG_VALUE_MINUS_SIGN, '-')
            ?.toDoubleOrNull()
    }

    private suspend fun isMultipleTagChoiceAvailable(
        selectedTypeId: Long,
        selectedTags: List<RecordBase.Tag> = emptyList(),
    ): Boolean {
        val shouldCloseAfterOne = shouldCloseAfterOneTagInteractor.execute(
            typeId = selectedTypeId,
            closeAfterOne = prefsInteractor.getRecordTagSelectionCloseAfterOne(),
            excludedActivities = prefsInteractor.getCloseAfterOneTagExcludeActivities().toSet(),
        )
        return selectedTags.isNotEmpty() || !shouldCloseAfterOne
    }
}