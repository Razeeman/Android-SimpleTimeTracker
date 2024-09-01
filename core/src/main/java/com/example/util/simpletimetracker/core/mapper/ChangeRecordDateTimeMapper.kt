package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.ChangeRecordDateTimeState
import javax.inject.Inject

class ChangeRecordDateTimeMapper @Inject constructor(
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
) {

    fun map(
        param: Param,
        field: Field,
        useMilitaryTimeFormat: Boolean,
        showSeconds: Boolean,
    ): ChangeRecordDateTimeState {
        return ChangeRecordDateTimeState(
            hint = when (param) {
                is Param.DateTime -> when (field) {
                    is Field.Start -> R.string.change_record_date_time_start
                    is Field.End -> R.string.change_record_date_time_end
                }
                is Param.Duration -> R.string.change_record_date_time_duration
            }.let(resourceRepo::getString),
            state = when (param) {
                is Param.DateTime -> {
                    ChangeRecordDateTimeState.State.DateTime(
                        timeMapper.getFormattedDateTime(
                            time = param.timestamp,
                            useMilitaryTime = useMilitaryTimeFormat,
                            showSeconds = showSeconds,
                        ),
                    )
                }
                is Param.Duration -> {
                    ChangeRecordDateTimeState.State.Duration(
                        timeMapper.formatInterval(
                            interval = param.duration,
                            forceSeconds = showSeconds,
                            useProportionalMinutes = false,
                        ),
                    )
                }
            },
        )
    }

    sealed interface Param {
        data class DateTime(val timestamp: Long) : Param
        data class Duration(val duration: Long) : Param
    }

    sealed interface Field {
        object Start : Field
        object End : Field
    }
}