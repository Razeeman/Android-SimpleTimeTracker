package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.domain.repo.ActivityFilterRepo
import java.util.Locale
import javax.inject.Inject

class ActivityFilterInteractor @Inject constructor(
    private val activityFilterRepo: ActivityFilterRepo,
) {

    suspend fun getAll(): List<ActivityFilter> {
        return activityFilterRepo.getAll()
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    suspend fun get(id: Long): ActivityFilter? {
        return activityFilterRepo.get(id)
    }

    suspend fun add(record: ActivityFilter) {
        activityFilterRepo.add(record)
    }

    suspend fun changeSelected(id: Long, selected: Boolean) {
        activityFilterRepo.changeSelected(id, selected)
    }

    suspend fun changeSelectedAll(selected: Boolean) {
        activityFilterRepo.changeSelectedAll(selected)
    }

    suspend fun remove(id: Long) {
        activityFilterRepo.remove(id)
    }
}