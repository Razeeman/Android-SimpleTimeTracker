package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.ComplexRule

interface ComplexRuleRepo {

    suspend fun isEmpty(): Boolean

    suspend fun getAll(): List<ComplexRule>

    suspend fun get(id: Long): ComplexRule?

    suspend fun add(favouriteIcon: ComplexRule): Long

    suspend fun disable(id: Long)

    suspend fun enable(id: Long)

    suspend fun remove(id: Long)

    suspend fun clear()
}