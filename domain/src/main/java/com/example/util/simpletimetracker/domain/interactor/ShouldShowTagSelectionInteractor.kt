package com.example.util.simpletimetracker.domain.interactor

import javax.inject.Inject

class ShouldShowTagSelectionInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTagInteractor: RecordTagInteractor,
) {

    suspend fun execute(typeId: Long): Boolean {
        // Check if need to show tag selection
        return if (prefsInteractor.getShowRecordTagSelection()) {
            val excludedActivities = prefsInteractor.getRecordTagSelectionExcludeActivities()

            // Check if activity is excluded from tag dialog.
            if (typeId !in excludedActivities) {
                // Check if activity has tags.
                // TODO add query to repo to find out if has tags.
                val tags = recordTagInteractor.getByTypeOrUntyped(typeId)
                    .filterNot { it.archived }
                tags.isNotEmpty()
            } else {
                false
            }
        } else {
            false
        }
    }
}