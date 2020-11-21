package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import javax.inject.Inject

class RecordTypeCategoryInteractor @Inject constructor(
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo
) {

    suspend fun get(typeId: Long): List<Long> {
        return recordTypeCategoryRepo.getCategoryIdsByType(typeId)
    }
    suspend fun add(typeId: Long, categoryIds: List<Long>) {
        recordTypeCategoryRepo.add(typeId, categoryIds)
    }

    suspend fun remove(typeId: Long, categoryIds: List<Long>) {
        recordTypeCategoryRepo.remove(typeId, categoryIds)
    }

    suspend fun clear() {
        recordTypeCategoryRepo.clear()
    }
}