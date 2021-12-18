package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import com.example.util.simpletimetracker.core.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeParams
import javax.inject.Inject

class NotificationTypeInteractorImpl @Inject constructor(
    private val notificationTypeManager: NotificationTypeManager,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo
) : NotificationTypeInteractor {

    override suspend fun checkAndShow(typeId: Long) {
        if (!prefsInteractor.getShowNotifications()) return

        val recordType = recordTypeInteractor.get(typeId)
        val runningRecord = runningRecordInteractor.get(typeId)
        val recordTags = recordTagInteractor.getAll().filter { it.id in runningRecord?.tagIds.orEmpty() }
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        show(
            recordType = recordType,
            runningRecord = runningRecord,
            recordTags = recordTags,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime
        )
    }

    override suspend fun checkAndHide(typeId: Long) {
        if (!prefsInteractor.getShowNotifications()) return

        hide(typeId)
    }

    override suspend fun updateNotifications() {
        if (prefsInteractor.getShowNotifications()) {
            showAll()
        } else {
            hideAll()
        }
    }

    private suspend fun showAll() {
        val recordTypes = recordTypeInteractor.getAll().map { it.id to it }.toMap()
        val recordTags = recordTagInteractor.getAll()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        runningRecordInteractor.getAll()
            .forEach { runningRecord ->
                show(
                    recordType = recordTypes[runningRecord.id],
                    runningRecord = runningRecord,
                    recordTags = recordTags.filter { it.id in runningRecord.tagIds },
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime
                )
            }
    }

    private suspend fun hideAll() {
        recordTypeInteractor.getAll()
            .map(RecordType::id)
            .forEach { typeId -> hide(typeId) }
    }

    private fun show(
        recordType: RecordType?,
        runningRecord: RunningRecord?,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean
    ) {
        if (recordType == null || runningRecord == null) {
            return
        }

        NotificationTypeParams(
            id = recordType.id.toInt(),
            icon = recordType.icon
                .let(iconMapper::mapIcon),
            color = recordType.color
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
            text = getNotificationText(recordType, recordTags),
            timeStarted = runningRecord.timeStarted
                .let { timeMapper.formatTime(it, useMilitaryTime) }
                .let { resourceRepo.getString(R.string.notification_time_started, it) },
            startedTimeStamp = runningRecord.timeStarted,
            goalTime = recordType.goalTime
                .takeIf { it > 0 }
                ?.let(timeMapper::formatDuration)
                ?.let { resourceRepo.getString(R.string.notification_record_type_goal_time, it) }
                .orEmpty()
        ).let(notificationTypeManager::show)
    }

    private fun getNotificationText(
        recordType: RecordType,
        recordTags: List<RecordTag>,
    ): String {
        val tag = recordTags.getFullName()

        return if (tag.isEmpty()) {
            recordType.name
        } else {
            "${recordType.name} - $tag"
        }
    }

    private fun hide(typeId: Long) {
        notificationTypeManager.hide(typeId.toInt())
    }
}