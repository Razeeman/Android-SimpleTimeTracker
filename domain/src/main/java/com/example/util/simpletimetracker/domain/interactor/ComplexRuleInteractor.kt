package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.ComplexRule
import com.example.util.simpletimetracker.domain.repo.ComplexRuleRepo
import javax.inject.Inject

class ComplexRuleInteractor @Inject constructor(
    private val repo: ComplexRuleRepo,
) {

    suspend fun getAll(): List<ComplexRule> {
        return repo.getAll()
    }

    suspend fun get(id: Long): ComplexRule? {
        return repo.get(id)
    }

    suspend fun add(icon: ComplexRule): Long {
        return repo.add(icon)
    }

    suspend fun remove(id: Long) {
        repo.remove(id)
    }
}