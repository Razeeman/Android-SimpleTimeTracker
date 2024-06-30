package com.example.util.simpletimetracker.feature_pomodoro.settings.model

import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData

sealed interface PomodoroHintButtonType : HintBigViewData.ButtonType {
    object PostPermissions : PomodoroHintButtonType
    object ExactAlarms : PomodoroHintButtonType
}