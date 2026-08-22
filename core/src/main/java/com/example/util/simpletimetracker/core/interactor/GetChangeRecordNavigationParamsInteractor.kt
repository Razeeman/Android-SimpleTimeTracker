package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.extension.toRecordParams
import com.example.util.simpletimetracker.core.mapper.ChangeRecordDateTimeMapper
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import javax.inject.Inject

class GetChangeRecordNavigationParamsInteractor @Inject constructor(
    private val changeRecordDateTimeMapper: ChangeRecordDateTimeMapper,
) {

    fun execute(
        item: RecordViewData,
        from: ChangeRecordParams.From,
        shift: Int,
        useMilitaryTimeFormat: Boolean,
        showSeconds: Boolean,
        durationFormat: DurationFormat,
        sharedElements: Pair<Any, String>?,
    ): ChangeRecordParams {
        val preview = ChangeRecordParams.Preview(
            name = item.name,
            tagName = item.tagName,
            timeStarted = item.timeStarted,
            timeFinished = item.timeFinished,
            timeStartedDateTime = changeRecordDateTimeMapper.map(
                param = ChangeRecordDateTimeMapper.Param.DateTime(item.timeStartedTimestamp),
                field = ChangeRecordDateTimeMapper.Field.Start,
                useMilitaryTimeFormat = useMilitaryTimeFormat,
                showSeconds = showSeconds,
                durationFormat = durationFormat,
            ).toRecordParams(),
            timeEndedDateTime = changeRecordDateTimeMapper.map(
                param = ChangeRecordDateTimeMapper.Param.DateTime(item.timeEndedTimestamp),
                field = ChangeRecordDateTimeMapper.Field.End,
                useMilitaryTimeFormat = useMilitaryTimeFormat,
                showSeconds = showSeconds,
                durationFormat = durationFormat,
            ).toRecordParams(),
            duration = item.duration,
            durationTotal = item.durationTotal,
            iconId = item.iconId.toParams(),
            color = item.color,
            comment = item.comment,
        )

        return when (item) {
            is RecordViewData.Tracked -> ChangeRecordParams.Tracked(
                transitionName = sharedElements?.second.orEmpty(),
                id = item.id,
                from = from,
                daysFromToday = shift,
                preview = preview,
            )
            is RecordViewData.Untracked -> ChangeRecordParams.Untracked(
                transitionName = sharedElements?.second.orEmpty(),
                timeStarted = item.timeStartedTimestamp,
                timeEnded = item.timeEndedTimestamp,
                daysFromToday = shift,
                preview = preview,
            )
        }
    }

    fun execute(
        item: RunningRecordViewData,
        from: ChangeRunningRecordParams.From,
        useMilitaryTimeFormat: Boolean,
        showSeconds: Boolean,
        durationFormat: DurationFormat,
        sharedElements: Pair<Any, String>?,
    ): ChangeRunningRecordParams {
        val preview = ChangeRunningRecordParams.Preview(
            name = item.name,
            tagName = item.tagName,
            timeStarted = item.timeStarted,
            timeStartedDateTime = changeRecordDateTimeMapper.map(
                param = ChangeRecordDateTimeMapper.Param.DateTime(item.timeStartedTimestamp),
                field = ChangeRecordDateTimeMapper.Field.Start,
                useMilitaryTimeFormat = useMilitaryTimeFormat,
                showSeconds = showSeconds,
                durationFormat = durationFormat,
            ).toRecordParams(),
            duration = item.timer,
            durationTotal = item.timerTotal,
            goalTime = item.goalTime.toParams(),
            iconId = item.iconId.toParams(),
            color = item.color,
            comment = item.comment,
        )

        return ChangeRunningRecordParams(
            transitionName = sharedElements?.second.orEmpty(),
            id = item.id,
            from = from,
            preview = preview,
        )
    }
}