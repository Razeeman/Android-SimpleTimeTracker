package com.example.util.simpletimetracker.feature_change_running_record.interactor

import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.core.mapper.ChangeRecordDateTimeMapper
import com.example.util.simpletimetracker.domain.extension.dropMillis
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.feature_change_record.api.model.ChangeRecordDateTimeFieldsState
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import javax.inject.Inject

class ChangeRunningRecordViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val getRunningRecordViewDataMediator: GetRunningRecordViewDataMediator,
    private val filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor,
    private val changeRecordDateTimeMapper: ChangeRecordDateTimeMapper,
) {

    suspend fun getPreviewViewData(
        record: RunningRecord,
        params: ChangeRunningRecordParams,
        dateTimeFieldState: ChangeRecordDateTimeFieldsState,
    ): ChangeRunningRecordViewData {
        val type = recordTypeInteractor.get(record.id)
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val durationFormat = prefsInteractor.getDurationFormat()
        val fromRecords = params.from is ChangeRunningRecordParams.From.Records
        val goals = filterGoalsByDayOfWeekInteractor
            .execute(recordTypeGoalInteractor.getByType(type?.id.orZero()))

        val recordPreview = if (type != null) {
            getRunningRecordViewDataMediator.execute(
                type = type,
                tags = recordTagInteractor.getAll(),
                goals = goals,
                record = record,
                nowIconVisible = fromRecords,
                goalsVisible = !fromRecords,
                totalDurationVisible = !fromRecords,
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                durationFormat = durationFormat,
                showSeconds = showSeconds,
            )
        } else {
            null
        }

        return ChangeRunningRecordViewData(
            recordPreview = recordPreview,
            dateTimeStarted = changeRecordDateTimeMapper.map(
                param = when (dateTimeFieldState.start) {
                    is ChangeRecordDateTimeFieldsState.State.DateTime -> {
                        ChangeRecordDateTimeMapper.Param.DateTime(record.timeStarted)
                    }
                    is ChangeRecordDateTimeFieldsState.State.Duration -> {
                        ChangeRecordDateTimeMapper.Param.Duration(
                            record.timeEnded - record.timeStarted.dropMillis(),
                        )
                    }
                },
                field = ChangeRecordDateTimeMapper.Field.Start,
                useMilitaryTimeFormat = useMilitaryTime,
                showSeconds = when (dateTimeFieldState.start) {
                    is ChangeRecordDateTimeFieldsState.State.DateTime -> showSeconds
                    is ChangeRecordDateTimeFieldsState.State.Duration -> true
                },
                durationFormat = durationFormat,
            ),
        )
    }
}