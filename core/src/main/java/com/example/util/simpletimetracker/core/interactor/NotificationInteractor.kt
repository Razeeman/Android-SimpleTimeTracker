package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.manager.NotificationManager
import com.example.util.simpletimetracker.core.manager.NotificationParams
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RunningRecord
import javax.inject.Inject

class NotificationInteractor @Inject constructor(
    private val notificationManager: NotificationManager,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo
) {

    suspend fun showAllNotifications() {
        runningRecordInteractor.getAll()
            .map(RunningRecord::id)
            .forEach { typeId -> showNotification(typeId) }
    }

    suspend fun showNotification(typeId: Long) {
        val recordType = recordTypeInteractor.get(typeId)
        val runningRecord = runningRecordInteractor.get(typeId)

        if (recordType == null || runningRecord == null) {
            hideNotification(typeId)
            return
        }

        notificationManager.show(
            NotificationParams(
                id = typeId.toInt(),
                icon = recordType.icon
                    .let(iconMapper::mapToDrawableResId),
                color = recordType.color
                    .let(colorMapper::mapToColorResId)
                    .let(resourceRepo::getColor),
                text = recordType.name
            )
        )
    }

    fun hideNotification(typeId: Long) {
        notificationManager.hide(typeId.toInt())
    }
}