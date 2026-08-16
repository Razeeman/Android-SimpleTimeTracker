package com.example.util.simpletimetracker.feature_notification.recordType.controller

import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivitySwitchInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.feature_notification.activitySwitch.mapper.NotificationControlsMapper
import com.example.util.simpletimetracker.feature_notification.recordType.interactor.ActivityStartStopFromBroadcastInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTypeBroadcastController @Inject constructor(
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationActivitySwitchInteractor: NotificationActivitySwitchInteractor,
    private val activityStartStopFromBroadcastInteractor: ActivityStartStopFromBroadcastInteractor,
    private val notificationControlsMapper: NotificationControlsMapper,
) {

    fun onActionActivityStop(
        typeId: Long,
    ) {
        if (typeId == 0L) return
        safeLaunch {
            activityStartStopFromBroadcastInteractor.onActionActivityStop(
                typeId = typeId,
            )
        }
    }

    fun onActionTypeClick(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        typesShift: Int,
    ) {
        if (selectedTypeId <= 0L) return
        safeLaunch {
            activityStartStopFromBroadcastInteractor.onActionTypeClick(
                from = notificationControlsMapper.mapExtraToFrom(
                    extra = from,
                    recordTypeId = typeId,
                ) ?: return@safeLaunch,
                selectedTypeId = selectedTypeId,
                typesShift = typesShift,
            )
        }
    }

    fun onActionRepeat() {
        safeLaunch {
            activityStartStopFromBroadcastInteractor.onActionRepeat()
        }
    }

    fun onActionApplyTags(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        selectedTags: List<RecordBase.Tag>,
        typesShift: Int,
    ) {
        safeLaunch {
            activityStartStopFromBroadcastInteractor.onActionApplyTags(
                from = notificationControlsMapper.mapExtraToFrom(
                    extra = from,
                    recordTypeId = typeId,
                ) ?: return@safeLaunch,
                selectedTypeId = selectedTypeId,
                selectedTags = selectedTags,
                typesShift = typesShift,
            )
        }
    }

    fun onActionClearTags(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        typesShift: Int,
        tagsShift: Int,
        isMultipleTagAvailable: Boolean,
        requiredValueSelectionTagIds: List<Long>,
    ) {
        safeLaunch {
            activityStartStopFromBroadcastInteractor.onActionClearTags(
                from = notificationControlsMapper.mapExtraToFrom(
                    extra = from,
                    recordTypeId = typeId,
                ) ?: return@safeLaunch,
                selectedTypeId = selectedTypeId,
                typesShift = typesShift,
                tagsShift = tagsShift,
                isMultipleTagAvailable = isMultipleTagAvailable,
                requiredValueSelectionTagIds = requiredValueSelectionTagIds,
            )
        }
    }

    fun onActionTagClick(
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
        safeLaunch {
            activityStartStopFromBroadcastInteractor.onActionTagClick(
                from = notificationControlsMapper.mapExtraToFrom(
                    extra = from,
                    recordTypeId = typeId,
                ) ?: return@safeLaunch,
                selectedTypeId = selectedTypeId,
                tagId = tagId,
                typesShift = typesShift,
                tagsShift = tagsShift,
                selectedTags = selectedTags,
                isMultipleTagAvailable = isMultipleTagAvailable,
                requiredValueSelectionTagIds = requiredValueSelectionTagIds,
            )
        }
    }

    fun onActionTagValueSave(
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
        safeLaunch {
            activityStartStopFromBroadcastInteractor.onActionTagValueSave(
                from = notificationControlsMapper.mapExtraToFrom(
                    extra = from,
                    recordTypeId = typeId,
                ) ?: return@safeLaunch,
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
    }

    fun onRequestUpdate(
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
        safeLaunch {
            activityStartStopFromBroadcastInteractor.onRequestUpdate(
                from = notificationControlsMapper.mapExtraToFrom(
                    extra = from,
                    recordTypeId = typeId,
                ) ?: return@safeLaunch,
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
    }

    fun onTypeCancel(
        typeId: Long,
    ) {
        safeLaunch {
            notificationTypeInteractor.checkAndShow(typeId)
        }
    }

    fun onActivitySwitchCancel() {
        safeLaunch {
            notificationActivitySwitchInteractor.updateNotification()
        }
    }

    fun onBootCompleted() {
        safeLaunch {
            notificationTypeInteractor.updateNotifications()
            notificationActivitySwitchInteractor.updateNotification()
        }
    }

    private fun safeLaunch(
        block: suspend CoroutineScope.() -> Unit,
    ) {
        allowDiskRead { MainScope() }.launch(block = block)
    }
}