package com.example.util.simpletimetracker.feature_notification.recordType.controller

import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivitySwitchInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.feature_notification.activitySwitch.mapper.NotificationControlsMapper
import com.example.util.simpletimetracker.feature_notification.recordType.interactor.ActivityStartStopFromBroadcastInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTypeBroadcastController @Inject constructor(
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationActivitySwitchInteractor: NotificationActivitySwitchInteractor,
    private val activityStartStopFromBroadcastInteractor: ActivityStartStopFromBroadcastInteractor,
    private val notificationControlsMapper: NotificationControlsMapper,
) {

    suspend fun onActionActivityStop(
        typeId: Long,
    ) {
        if (typeId == 0L) return
        activityStartStopFromBroadcastInteractor.onActionActivityStop(
            typeId = typeId,
        )
    }

    suspend fun onActionTypeClick(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        typesShift: Int,
    ) {
        if (selectedTypeId <= 0L) return
        activityStartStopFromBroadcastInteractor.onActionTypeClick(
            from = notificationControlsMapper.mapExtraToFrom(
                extra = from,
                recordTypeId = typeId,
            ) ?: return,
            selectedTypeId = selectedTypeId,
            typesShift = typesShift,
        )
    }

    suspend fun onActionRepeat() {
        activityStartStopFromBroadcastInteractor.onActionRepeat()
    }

    suspend fun onActionApplyTags(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        selectedTags: List<RecordBase.Tag>,
        typesShift: Int,
    ) {
        activityStartStopFromBroadcastInteractor.onActionApplyTags(
            from = notificationControlsMapper.mapExtraToFrom(
                extra = from,
                recordTypeId = typeId,
            ) ?: return,
            selectedTypeId = selectedTypeId,
            selectedTags = selectedTags,
            typesShift = typesShift,
        )
    }

    suspend fun onActionClearTags(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        typesShift: Int,
        tagsShift: Int,
        isMultipleTagAvailable: Boolean,
        requiredValueSelectionTagIds: List<Long>,
    ) {
        activityStartStopFromBroadcastInteractor.onActionClearTags(
            from = notificationControlsMapper.mapExtraToFrom(
                extra = from,
                recordTypeId = typeId,
            ) ?: return,
            selectedTypeId = selectedTypeId,
            typesShift = typesShift,
            tagsShift = tagsShift,
            isMultipleTagAvailable = isMultipleTagAvailable,
            requiredValueSelectionTagIds = requiredValueSelectionTagIds,
        )
    }

    suspend fun onActionTagClick(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        tagId: Long,
        typesShift: Int,
        tagsShift: Int,
        selectedTags: List<RecordBase.Tag>,
        isMultipleTagAvailable: Boolean,
        requiredValueSelectionTagIds: List<Long>,
    ) {
        if (tagId <= 0L) return
        activityStartStopFromBroadcastInteractor.onActionTagClick(
            from = notificationControlsMapper.mapExtraToFrom(
                extra = from,
                recordTypeId = typeId,
            ) ?: return,
            selectedTypeId = selectedTypeId,
            tagId = tagId,
            typesShift = typesShift,
            tagsShift = tagsShift,
            selectedTags = selectedTags,
            isMultipleTagAvailable = isMultipleTagAvailable,
            requiredValueSelectionTagIds = requiredValueSelectionTagIds,
        )
    }

    suspend fun onActionTagValueSave(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        tagId: Long,
        tagValue: String?,
        typesShift: Int,
        tagsShift: Int,
        selectedTags: List<RecordBase.Tag>,
        isMultipleTagAvailable: Boolean,
        requiredValueSelectionTagIds: List<Long>,
    ) {
        activityStartStopFromBroadcastInteractor.onActionTagValueSave(
            from = notificationControlsMapper.mapExtraToFrom(
                extra = from,
                recordTypeId = typeId,
            ) ?: return,
            selectedTypeId = selectedTypeId,
            tagId = tagId,
            tagValue = tagValue,
            typesShift = typesShift,
            tagsShift = tagsShift,
            selectedTags = selectedTags,
            isMultipleTagAvailable = isMultipleTagAvailable,
            requiredValueSelectionTagIds = requiredValueSelectionTagIds,
        )
    }

    suspend fun onRequestUpdate(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        selectedTags: List<RecordBase.Tag>,
        editingTagId: Long?,
        editingTagValueInput: String?,
        typesShift: Int,
        tagsShift: Int,
        isMultipleTagAvailable: Boolean,
        requiredValueSelectionTagIds: List<Long>,
    ) {
        activityStartStopFromBroadcastInteractor.onRequestUpdate(
            from = notificationControlsMapper.mapExtraToFrom(
                extra = from,
                recordTypeId = typeId,
            ) ?: return,
            selectedTypeId = selectedTypeId,
            selectedTags = selectedTags,
            editingTagId = editingTagId,
            editingTagValueInput = editingTagValueInput,
            typesShift = typesShift,
            tagsShift = tagsShift,
            isMultipleTagAvailable = isMultipleTagAvailable,
            requiredValueSelectionTagIds = requiredValueSelectionTagIds,
        )
    }

    suspend fun onTypeCancel(
        typeId: Long,
    ) {
        notificationTypeInteractor.checkAndShow(typeId)
    }

    suspend fun onActivitySwitchCancel() {
        notificationActivitySwitchInteractor.updateNotification()
    }

    suspend fun onBootCompleted() {
        notificationTypeInteractor.updateNotifications()
        notificationActivitySwitchInteractor.updateNotification()
    }
}