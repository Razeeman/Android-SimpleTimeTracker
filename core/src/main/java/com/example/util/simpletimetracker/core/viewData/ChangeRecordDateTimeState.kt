package com.example.util.simpletimetracker.core.viewData

import com.example.util.simpletimetracker.core.mapper.TimeMapper

data class ChangeRecordDateTimeState(
    val hint: String,
    val state: State,
) {

    sealed interface State {
        data class DateTime(val data: TimeMapper.DateTime) : State
        data class Duration(val data: String) : State
    }
}