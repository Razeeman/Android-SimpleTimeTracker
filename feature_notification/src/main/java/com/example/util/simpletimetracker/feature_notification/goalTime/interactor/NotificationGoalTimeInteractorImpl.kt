package com.example.util.simpletimetracker.feature_notification.goalTime.interactor

import com.example.util.simpletimetracker.core.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.goalTime.manager.NotificationGoalTimeManager
import com.example.util.simpletimetracker.feature_notification.goalTime.manager.NotificationGoalTimeParams
import com.example.util.simpletimetracker.feature_notification.goalTime.scheduler.NotificationGoalTimeScheduler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationGoalTimeInteractorImpl @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val manager: NotificationGoalTimeManager,
    private val scheduler: NotificationGoalTimeScheduler,
    private val timeMapper: TimeMapper,
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper
) : NotificationGoalTimeInteractor {

    override suspend fun checkAndReschedule(typeId: Long) {
        val recordType = recordTypeInteractor.get(typeId)
        val runningRecord = runningRecordInteractor.get(typeId)

        if (recordType == null || runningRecord == null) return

        val goalTime = recordType.goalTime * 1000
        val timeStarted = runningRecord.timeStarted
        val currentDuration = System.currentTimeMillis() - timeStarted

        cancel(typeId)
        if (goalTime > 0L && goalTime > currentDuration) {
            scheduler.schedule(goalTime - currentDuration, typeId)
        }
    }

    override fun cancel(typeId: Long) {
        scheduler.cancelSchedule(typeId)
        manager.hide(typeId)
    }

    override fun show(typeId: Long) {
        GlobalScope.launch {
            val recordType = recordTypeInteractor.get(typeId) ?: return@launch
            val isDarkTheme = prefsInteractor.getDarkMode()

            NotificationGoalTimeParams(
                typeId = recordType.id,
                icon = recordType.icon
                    .let(iconMapper::mapToDrawableResId),
                color = recordType.color
                    .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                    .let(resourceRepo::getColor),
                text = recordType.name,
                description = resourceRepo.getString(
                    R.string.notification_goal_time_description,
                    recordType.goalTime.let(timeMapper::formatDuration)
                )
            ).let(manager::show)
        }
    }
}