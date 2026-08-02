package com.example.util.simpletimetracker.feature_change_record.api

import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromScreen
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData

data class ChangeRecordEditorMode(
    val config: ChangeRecordConfig,
    val mergeAvailable: () -> Boolean,
    val previewTimeEnded: () -> Long,
    val adjustPreviewTimeEnded: () -> Long,
    val adjustPreviewOriginalTimeEnded: () -> Long,
    val updatePreview: suspend () -> Unit,
    val getChangeCategoryParams: (data: ChangeTagData) -> ChangeRecordTagFromScreen,
    val onSaveClickDelegate: suspend (doAfter: suspend () -> Unit) -> Unit,
    val sendPreviewUpdate: suspend (fullUpdate: Boolean) -> Unit,
    val initializePreviewViewData: suspend () -> Unit,
    val onDeleteClick: () -> Unit,
    val onStatisticsClick: () -> Unit,
    val onTimeStartedChanged: suspend () -> Unit,
    val onTimeEndedChanged: suspend () -> Unit,
)