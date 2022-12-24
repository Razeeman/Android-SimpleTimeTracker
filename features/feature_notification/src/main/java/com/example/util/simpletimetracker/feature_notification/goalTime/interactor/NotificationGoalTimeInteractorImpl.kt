package com.example.util.simpletimetracker.feature_notification.goalTime.interactor

import com.example.util.simpletimetracker.core.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.GoalTimeType
import com.example.util.simpletimetracker.domain.model.RangeLength
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
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val manager: NotificationGoalTimeManager,
    private val scheduler: NotificationGoalTimeScheduler,
    private val timeMapper: TimeMapper,
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper,
    private val rangeMapper: RangeMapper,
) : NotificationGoalTimeInteractor {

    override suspend fun checkAndReschedule(typeId: Long) {
        val recordType = recordTypeInteractor.get(typeId)
        val runningRecord = runningRecordInteractor.get(typeId)
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

        if (recordType == null || runningRecord == null) return

        cancel(typeId)

        // Session
        val sessionGoalTime = recordType.goalTime * 1000
        val sessionCurrent = System.currentTimeMillis() - runningRecord.timeStarted

        if (sessionGoalTime > 0L && sessionGoalTime > sessionCurrent) {
            scheduler.schedule(sessionGoalTime - sessionCurrent, typeId, GoalTimeType.Session)
        }

        // Daily
        val dailyGoalTime = recordType.dailyGoalTime * 1000
        val (todayRangeStart, todayRangeEnd) = timeMapper.getRangeStartAndEnd(
            rangeLength = RangeLength.Day,
            shift = 0,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val dailyCurrent = recordInteractor.getFromRange(todayRangeStart, todayRangeEnd)
            .filter { it.typeId == typeId }
            .map { rangeMapper.clampToRange(it, todayRangeStart, todayRangeEnd) }
            .let(rangeMapper::mapToDuration) + sessionCurrent

        if (dailyGoalTime > 0 && dailyGoalTime > dailyCurrent) {
            scheduler.schedule(dailyGoalTime - dailyCurrent, typeId, GoalTimeType.Day)
        }

        // Weekly
        val weeklyGoalTime = recordType.weeklyGoalTime * 1000
        val (weekRangeStart, weekRangeEnd) = timeMapper.getRangeStartAndEnd(
            rangeLength = RangeLength.Week,
            shift = 0,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val weeklyCurrent = recordInteractor.getFromRange(weekRangeStart, weekRangeEnd)
            .filter { it.typeId == typeId }
            .map { rangeMapper.clampToRange(it, weekRangeStart, weekRangeEnd) }
            .let(rangeMapper::mapToDuration) + sessionCurrent

        if (weeklyGoalTime > 0 && weeklyGoalTime > weeklyCurrent) {
            scheduler.schedule(weeklyGoalTime - weeklyCurrent, typeId, GoalTimeType.Week)
        }
    }

    override fun cancel(typeId: Long) {
        listOf(
            GoalTimeType.Session,
            GoalTimeType.Day,
            GoalTimeType.Week
        ).forEach {
            cancel(typeId, it)
        }
    }

    override fun cancel(typeId: Long, goalTimeType: GoalTimeType) {
        scheduler.cancelSchedule(typeId, goalTimeType)
        manager.hide(typeId, goalTimeType)
    }

    override fun show(typeId: Long, goalTimeType: GoalTimeType) {
        GlobalScope.launch {
            val recordType = recordTypeInteractor.get(typeId) ?: return@launch
            val isDarkTheme = prefsInteractor.getDarkMode()

            val goalTimeString = when (goalTimeType) {
                is GoalTimeType.Session -> recordType.goalTime
                is GoalTimeType.Day -> recordType.dailyGoalTime
                is GoalTimeType.Week -> recordType.weeklyGoalTime
            }.let(timeMapper::formatDuration)
            val goalTimeTypeString = when (goalTimeType) {
                is GoalTimeType.Session -> R.string.change_record_type_session_goal_time
                is GoalTimeType.Day -> R.string.change_record_type_daily_goal_time
                is GoalTimeType.Week -> R.string.change_record_type_weekly_goal_time
            }.let(resourceRepo::getString).let { " ($it)" }

            NotificationGoalTimeParams(
                typeId = recordType.id,
                goalTimeType = goalTimeType,
                icon = recordType.icon
                    .let(iconMapper::mapIcon),
                color = recordType.color
                    .let { colorMapper.mapToColorInt(it, isDarkTheme) },
                text = recordType.name,
                description = resourceRepo.getString(
                    R.string.notification_goal_time_description,
                    goalTimeString
                ) + goalTimeTypeString
            ).let(manager::show)
        }
    }
}