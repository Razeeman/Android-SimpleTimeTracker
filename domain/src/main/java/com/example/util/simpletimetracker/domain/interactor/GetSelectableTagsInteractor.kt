package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordTag
import javax.inject.Inject

class GetSelectableTagsInteractor @Inject constructor(
    private val recordTypeToTagInteractor: RecordTypeToTagInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val filterSelectableTagsInteractor: FilterSelectableTagsInteractor,
) {

    suspend fun execute(typeId: Long?): List<RecordTag> {
        val tags = recordTagInteractor.getAll()
        val typesToTags = recordTypeToTagInteractor.getAll()
        val typeIds = if (typeId != null && typeId != 0L) {
            listOf(typeId)
        } else {
            emptyList()
        }
        val selectableTagIds = filterSelectableTagsInteractor.execute(
            tagIds = tags.map { it.id },
            typesToTags = typesToTags,
            typeIds = typeIds,
        )

        return tags.filter { it.id in selectableTagIds }
    }
}