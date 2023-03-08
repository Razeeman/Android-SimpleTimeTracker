package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
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
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
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
    private val resourceRepo: ResourceRepo,
) : NotificationTypeInteractor {

    override suspend fun checkAndShow(typeId: Long, typesShift: Int) {
        if (!prefsInteractor.getShowNotifications()) return

        val recordType = recordTypeInteractor.get(typeId)
        val recordTypes = recordTypeInteractor.getAll()
        val runningRecord = runningRecordInteractor.get(typeId)
        val recordTags = recordTagInteractor.getAll().filter { it.id in runningRecord?.tagIds.orEmpty() }
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()

        show(
            recordType = recordType,
            runningRecord = runningRecord,
            recordTags = recordTags,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            showSeconds = showSeconds,
            types = recordTypes,
            typesShift = typesShift,
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
        val recordTypes = recordTypeInteractor.getAll().associateBy { it.id }
        val recordTags = recordTagInteractor.getAll()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()

        runningRecordInteractor.getAll()
            .forEach { runningRecord ->
                show(
                    recordType = recordTypes[runningRecord.id],
                    runningRecord = runningRecord,
                    recordTags = recordTags.filter { it.id in runningRecord.tagIds },
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    showSeconds = showSeconds,
                    types = recordTypes.values.toList()
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
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
        types: List<RecordType>,
        typesShift: Int = 0,
    ) {
        if (recordType == null || runningRecord == null) {
            return
        }

        NotificationTypeParams(
            id = recordType.id,
            icon = recordType.icon
                .let(iconMapper::mapIcon),
            color = recordType.color
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
            text = getNotificationText(recordType, recordTags),
            timeStarted = runningRecord.timeStarted
                .let {
                    timeMapper.formatTime(
                        time = it,
                        useMilitaryTime = useMilitaryTime,
                        showSeconds = showSeconds,
                    )
                }
                .let { resourceRepo.getString(R.string.notification_time_started, it) },
            startedTimeStamp = runningRecord.timeStarted,
            goalTime = recordType.goalTime
                .takeIf { it > 0 }
                ?.let(timeMapper::formatDuration)
                ?.let { resourceRepo.getString(R.string.notification_record_type_goal_time, it) }
                .orEmpty(),
            stopButton = resourceRepo.getString(R.string.notification_record_type_stop),
            types = types
                .filter { !it.hidden }
                .map { type ->
                    NotificationTypeParams.Type(
                        id = type.id,
                        icon = type.icon.let(iconMapper::mapIcon),
                        color = type.color.let { colorMapper.mapToColorInt(it, isDarkTheme) },
                    )
                },
            typesShift = typesShift,
            controlIconPrev = RecordTypeIcon.Image(R.drawable.arrow_left),
            controlIconNext = RecordTypeIcon.Image(R.drawable.arrow_right),
            controlIconColor = colorMapper.toInactiveColor(isDarkTheme),
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