package com.example.util.simpletimetracker.feature_change_running_record.mapper

import com.example.util.simpletimetracker.domain.interactor.UpdateRunningRecordFromChangeScreenInteractor
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import javax.inject.Inject

class ChangeRunningRecordMapper @Inject constructor() {

    fun map(
        fullUpdate: Boolean,
        recordPreview: RunningRecordViewData,
    ): UpdateRunningRecordFromChangeScreenInteractor.Update {
        return UpdateRunningRecordFromChangeScreenInteractor.Update(
            id = recordPreview.id,
            timer = recordPreview.timer,
            timerTotal = recordPreview.timerTotal,
            goalText = recordPreview.goalTime.text,
            goalComplete = recordPreview.goalTime.complete,
            additionalData = if (fullUpdate) {
                UpdateRunningRecordFromChangeScreenInteractor.AdditionalData(
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