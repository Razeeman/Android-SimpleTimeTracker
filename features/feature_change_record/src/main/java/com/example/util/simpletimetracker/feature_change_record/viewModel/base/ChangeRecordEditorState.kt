package com.example.util.simpletimetracker.feature_change_record.viewModel.base

import com.example.util.simpletimetracker.domain.record.model.RecordBase

data class ChangeRecordEditorState(
    var newTypeId: Long = 0,
    var newTimeEnded: Long = 0,
    var newTimeStarted: Long = 0,
    var newTimeSplit: Long = 0,
    var newSplitBeforeTypeId: Long? = null,
    var newTags: List<RecordBase.Tag> = emptyList(),
    var originalRecordId: Long = 0,
    var originalTypeId: Long = 0,
    var originalTags: List<RecordBase.Tag> = emptyList(),
    var originalTimeStarted: Long = 0,
    var originalTimeEnded: Long = 0,
)