package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.repo.RecordTypeToDefaultTagRepo
import javax.inject.Inject

class RecordTypeToDefaultTagInteractor @Inject constructor(
    private val repo: RecordTypeToDefaultTagRepo,
) {

    suspend fun getTags(typeId: Long): Set<Long> {
        return repo.getTagIdsByType(typeId)
    }

    suspend fun getTypes(tagId: Long): Set<Long> {
        return repo.getTypeIdsByTag(tagId)
    }

    suspend fun addTypes(tagId: Long, typeIds: List<Long>) {
        repo.addTypes(tagId, typeIds)
    }

    suspend fun removeTypes(categoryId: Long, typeIds: List<Long>) {
        repo.removeTypes(categoryId, typeIds)
    }
}