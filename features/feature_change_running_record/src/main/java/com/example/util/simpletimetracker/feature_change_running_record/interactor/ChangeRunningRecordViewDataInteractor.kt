package com.example.util.simpletimetracker.feature_change_running_record.interactor

import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import javax.inject.Inject

class ChangeRunningRecordViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val timeMapper: TimeMapper,
    private val getRunningRecordViewDataMediator: GetRunningRecordViewDataMediator,
) {

    suspend fun getPreviewViewData(
        record: RunningRecord,
        params: ChangeRunningRecordParams,
    ): ChangeRunningRecordViewData {
        val type = recordTypeInteractor.get(record.id)
        val goals = recordTypeGoalInteractor.getByType(type?.id.orZero())
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val fromRecords = params.from is ChangeRunningRecordParams.From.Records

        val recordPreview = if (type != null) {
            getRunningRecordViewDataMediator.execute(
                type = type,
                tags = recordTagInteractor.getAll().filter { it.id in record.tagIds },
                goals = goals,
                record = record,
                nowIconVisible = fromRecords,
                goalsVisible = !fromRecords,
                totalDurationVisible = !fromRecords,
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                useProportionalMinutes = useProportionalMinutes,
                showSeconds = showSeconds,
            )
        } else {
            null
        }

        return ChangeRunningRecordViewData(
            recordPreview = recordPreview,
            dateTimeStarted = timeMapper.formatDateTime(
                time = record.timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
        )
    }
}