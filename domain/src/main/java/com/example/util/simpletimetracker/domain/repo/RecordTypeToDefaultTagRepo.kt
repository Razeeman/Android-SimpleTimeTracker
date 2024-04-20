package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.RecordTypeToDefaultTag

interface RecordTypeToDefaultTagRepo {

    suspend fun getAll(): List<RecordTypeToDefaultTag>

    suspend fun getTagIdsByType(typeId: Long): Set<Long>

    suspend fun getTypeIdsByTag(tagId: Long): Set<Long>

    suspend fun add(recordTypeToTag: RecordTypeToDefaultTag)

    suspend fun addTypes(tagId: Long, typeIds: List<Long>)

    suspend fun removeTypes(tagId: Long, typeIds: List<Long>)

    suspend fun removeAll(tagId: Long)

    suspend fun removeAllByType(typeId: Long)

    suspend fun clear()
}