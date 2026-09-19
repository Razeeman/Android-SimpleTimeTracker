package com.example.util.simpletimetracker.feature_running_records.api

import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.RecordShortcutViewData

interface OnShortcutClickInteractor {
    suspend fun execute(data: RecordShortcutViewData): ExecuteResult
    suspend fun onSpinnerPositionSelected(data: RecordShortcutViewData, position: Int)
    fun onButtonClick(data: RecordShortcutViewData)

    enum class ExecuteResult {
        DataChanged,
        Navigation,
    }
}