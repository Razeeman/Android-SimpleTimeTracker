package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordDataSelectionDialogResult
import javax.inject.Inject

class ShouldShowRecordDataSelectionInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
) {

    suspend fun execute(
        typeId: Long,
        commentInputAvailable: Boolean,
    ): RecordDataSelectionDialogResult {
        val fields = mutableListOf<RecordDataSelectionDialogResult.Field>()
        if (needToShowTags(typeId)) {
            fields += RecordDataSelectionDialogResult.Field.Tags
        }
        if (needToShowComment(typeId, commentInputAvailable)) {
            fields += RecordDataSelectionDialogResult.Field.Comment
        }
        return  RecordDataSelectionDialogResult(fields)
    }

    private suspend fun needToShowTags(typeId: Long): Boolean {
        if (!prefsInteractor.getShowRecordTagSelection()) return false

        val excludedActivities = prefsInteractor.getRecordTagSelectionExcludeActivities()

        // Check if activity is excluded from tag dialog.
        return if (typeId !in excludedActivities) {
            // Check if activity has tags.
            val assignableTags = getSelectableTagsInteractor.execute(typeId)
                .filterNot { it.archived }
            assignableTags.isNotEmpty()
        } else {
            false
        }
    }

    private suspend fun needToShowComment(
        typeId: Long,
        commentInputAvailable: Boolean,
    ): Boolean {
        if (!commentInputAvailable) return false
        if (!prefsInteractor.getShowCommentInput()) return false

        val excludedActivities = prefsInteractor.getCommentInputExcludeActivities()

        // Check if activity is excluded from comment input.
        return typeId !in excludedActivities
    }
}