package com.example.util.simpletimetracker.feature_change_record.viewModel.base

import com.example.util.simpletimetracker.domain.record.model.RecordBase

data class ChangeRecordEditorState(
    val newTypeId: Long = 0,
    val newTimeEnded: Long = 0,
    val newTimeStarted: Long = 0,
    val newTimeSplit: Long = 0,
    val newSplitBeforeTypeId: Long? = null,
    val newTags: List<RecordBase.Tag> = emptyList(),
    val originalRecordId: Long = 0,
    val originalTypeId: Long = 0,
    val originalTags: List<RecordBase.Tag> = emptyList(),
    val originalTimeStarted: Long = 0,
    val originalTimeEnded: Long = 0,
)