package com.example.util.simpletimetracker.feature_notification.recordType.controller

import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivitySwitchInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationTypeInteractor
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

    fun onActionTagClick(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        tagId: Long,
        typesShift: Int,
    ) {
        safeLaunch {
            activityStartStopFromBroadcastInteractor.onActionTagClick(
                from = notificationControlsMapper.mapExtraToFrom(
                    extra = from,
                    recordTypeId = typeId,
                ) ?: return@safeLaunch,
                selectedTypeId = selectedTypeId,
                tagId = tagId,
                typesShift = typesShift,
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
            )
        }
    }

    fun onRequestUpdate(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        selectedTagId: Long,
        selectedTagValue: String?,
        typesShift: Int,
        tagsShift: Int,
    ) {
        safeLaunch {
            activityStartStopFromBroadcastInteractor.onRequestUpdate(
                from = notificationControlsMapper.mapExtraToFrom(
                    extra = from,
                    typeId,
                ) ?: return@safeLaunch,
                selectedTypeId = selectedTypeId,
                selectedTagId = selectedTagId,
                selectedTagValue = selectedTagValue,
                typesShift = typesShift,
                tagsShift = tagsShift,
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