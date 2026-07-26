package com.example.util.simpletimetracker.feature_change_record.viewModel.base

class ChangeRecordEditorStateBuilder(private val currentState: ChangeRecordEditorState) {
    var newTypeId = currentState.newTypeId
    var newTimeEnded = currentState.newTimeEnded
    var newTimeStarted = currentState.newTimeStarted
    var newTimeSplit = currentState.newTimeSplit
    var newSplitBeforeTypeId = currentState.newSplitBeforeTypeId
    var newTags = currentState.newTags
    var originalRecordId = currentState.originalRecordId
    var originalTypeId = currentState.originalTypeId
    var originalTags = currentState.originalTags
    var originalTimeStarted = currentState.originalTimeStarted
    var originalTimeEnded = currentState.originalTimeEnded

    fun build(): ChangeRecordEditorState {
        return currentState.copy(
            newTypeId = newTypeId,
            newTimeEnded = newTimeEnded,
            newTimeStarted = newTimeStarted,
            newTimeSplit = newTimeSplit,
            newSplitBeforeTypeId = newSplitBeforeTypeId,
            newTags = newTags,
            originalRecordId = originalRecordId,
            originalTypeId = originalTypeId,
            originalTags = originalTags,
            originalTimeStarted = originalTimeStarted,
            originalTimeEnded = originalTimeEnded,
        )
    }
}