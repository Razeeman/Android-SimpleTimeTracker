package com.example.util.simpletimetracker.feature_change_running_record.mapper

import com.example.util.simpletimetracker.domain.record.interactor.UpdateRunningRecordsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.UpdateRunningRecordsInteractor.GoalState
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.GoalTimeViewData.Subtype
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import javax.inject.Inject

class ChangeRunningRecordMapper @Inject constructor() {

    fun map(
        fullUpdate: Boolean,
        recordPreview: RunningRecordViewData,
    ): UpdateRunningRecordsInteractor.Update {
        return UpdateRunningRecordsInteractor.Update(
            id = recordPreview.id,
            timer = recordPreview.timer,
            timerTotal = recordPreview.timerTotal,
            goalTimes = recordPreview.goalTimes.map { goalTime ->
                UpdateRunningRecordsInteractor.GoalTime(
                    text = goalTime.text,
                    state = when (goalTime.state) {
                        is Subtype.Hidden -> GoalState.Hidden
                        is Subtype.Goal -> GoalState.Goal
                        is Subtype.Limit -> GoalState.Limit
                    },
                )
            },
            additionalData = if (fullUpdate) {
                UpdateRunningRecordsInteractor.AdditionalData(
                    tagName = recordPreview.tagName,
                    timeStarted = recordPreview.timeStarted,
                    comment = recordPreview.comment,
                )
            } else {
                null
            },
        )
    }
}