package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.ComplexRule

// TODO RULES add to backup
interface ComplexRuleRepo {

    suspend fun getAll(): List<ComplexRule>

    suspend fun get(id: Long): ComplexRule?

    suspend fun add(favouriteIcon: ComplexRule): Long

    suspend fun remove(id: Long)

    suspend fun clear()
}