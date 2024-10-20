package com.example.util.simpletimetracker.feature_tag_selection.interactor

import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import javax.inject.Inject

class RecordTagSelectionViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTagViewDataInteractor: RecordTagViewDataInteractor,
) {

    suspend fun getViewData(typeId: Long, selectedTags: List<Long>): List<ViewHolderType> {
        val closeAfterOneTagSelected: Boolean = prefsInteractor.getRecordTagSelectionCloseAfterOne()
        val result: MutableList<ViewHolderType> = mutableListOf()

        recordTagViewDataInteractor.getViewData(
            selectedTags = selectedTags,
            typeId = typeId,
            multipleChoiceAvailable = !closeAfterOneTagSelected,
            showAddButton = false,
            showArchived = false,
            showUntaggedButton = true,
        ).data.let(result::addAll)

        return result
    }
}