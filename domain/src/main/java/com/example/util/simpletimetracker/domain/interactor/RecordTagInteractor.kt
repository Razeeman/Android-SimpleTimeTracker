package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import javax.inject.Inject

class RecordTagInteractor @Inject constructor(
    private val repo: RecordTagRepo
) {

    suspend fun getAll(): List<RecordTag> {
        return repo.getAll()
    }

    suspend fun get(id: Long): RecordTag? {
        return repo.get(id)
    }

    suspend fun getByType(typeId: Long): List<RecordTag> {
        return repo.getByType(typeId)
    }

    suspend fun add(tag: RecordTag) {
        var newItem = tag

        // If there is already an item with this name - override
        repo.getByType(tag.typeId)
            .firstOrNull { it.name == newItem.name }
            ?.let { savedItem ->
                newItem = tag.copy(
                    id = savedItem.id,
                    archived = false
                )
            }

        repo.add(newItem)
    }

    suspend fun archive(id: Long) {
        repo.archive(id)
    }

    suspend fun restore(id: Long) {
        repo.restore(id)
    }

    suspend fun remove(id: Long) {
        repo.remove(id)
    }

    suspend fun removeByType(typeId: Long) {
        repo.removeByType(typeId)
    }
}