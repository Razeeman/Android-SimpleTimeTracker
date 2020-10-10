package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.manager.NotificationManager
import com.example.util.simpletimetracker.core.manager.NotificationParams
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import javax.inject.Inject

class NotificationInteractor @Inject constructor(
    private val notificationManager: NotificationManager,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo
) {

    suspend fun checkAndShow(typeId: Long) {
        if (!prefsInteractor.getShowNotifications()) return

        val recordType = recordTypeInteractor.get(typeId)
        val runningRecord = runningRecordInteractor.get(typeId)
        show(recordType, runningRecord)
    }

    suspend fun checkAndHide(typeId: Long) {
        if (!prefsInteractor.getShowNotifications()) return

        hide(typeId)
    }

    suspend fun checkAndShowAll() {
        if (!prefsInteractor.getShowNotifications()) return

        showAll()
    }

    suspend fun updateNotifications() {
        if (prefsInteractor.getShowNotifications()) {
            showAll()
        } else {
            hideAll()
        }
    }

    private suspend fun showAll() {
        val recordTypes = recordTypeInteractor.getAll().map { it.id to it }.toMap()

        runningRecordInteractor.getAll()
            .forEach { runningRecord -> show(recordTypes[runningRecord.id], runningRecord) }
    }

    private suspend fun hideAll() {
        recordTypeInteractor.getAll()
            .map(RecordType::id)
            .forEach { typeId -> hide(typeId) }
    }

    private fun show(recordType: RecordType?, runningRecord: RunningRecord?) {
        if (recordType == null || runningRecord == null) {
            return
        }

        notificationManager.show(
            NotificationParams(
                id = recordType.id.toInt(),
                icon = recordType.icon
                    .let(iconMapper::mapToDrawableResId),
                color = recordType.color
                    .let(colorMapper::mapToColorResId)
                    .let(resourceRepo::getColor),
                text = recordType.name,
                description = runningRecord.timeStarted
                    .let(timeMapper::formatTime)
                    .let { resourceRepo.getString(R.string.notification_time_started, it) }
            )
        )
    }

    private fun hide(typeId: Long) {
        notificationManager.hide(typeId.toInt())
    }
}