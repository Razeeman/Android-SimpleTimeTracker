package com.example.util.simpletimetracker.domain.interactor

import javax.inject.Inject

class ShouldShowTagSelectionInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
) {

    suspend fun execute(typeId: Long): Boolean {
        // Check if need to show tag selection
        return if (prefsInteractor.getShowRecordTagSelection()) {
            val excludedActivities = prefsInteractor.getRecordTagSelectionExcludeActivities()

            // Check if activity is excluded from tag dialog.
            if (typeId !in excludedActivities) {
                // Check if activity has tags.
                val assignableTags = getSelectableTagsInteractor.execute(typeId)
                    .filterNot { it.archived }
                assignableTags.isNotEmpty()
            } else {
                false
            }
        } else {
            false
        }
    }
}