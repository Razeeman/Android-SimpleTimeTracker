package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.manager.NotificationManager
import com.example.util.simpletimetracker.core.manager.NotificationParams
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import javax.inject.Inject

class NotificationInteractor @Inject constructor(
    private val notificationManager: NotificationManager,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo
) {

    suspend fun showAll() {
        runningRecordInteractor.getAll()
            .map(RunningRecord::id)
            .forEach { typeId -> show(typeId) }
    }

    suspend fun hideAll() {
        recordTypeInteractor.getAll()
            .map(RecordType::id)
            .forEach { typeId -> hide(typeId) }
    }

    suspend fun show(typeId: Long) {
        val recordType = recordTypeInteractor.get(typeId)
        val runningRecord = runningRecordInteractor.get(typeId)

        if (recordType == null || runningRecord == null) {
            hide(typeId)
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
                text = recordType.name,
                description = runningRecord.timeStarted
                    .let(timeMapper::formatTime)
            )
        )
    }

    fun hide(typeId: Long) {
        notificationManager.hide(typeId.toInt())
    }
}