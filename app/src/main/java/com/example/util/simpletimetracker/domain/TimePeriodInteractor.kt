package com.example.util.simpletimetracker.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TimePeriodInteractor @Inject constructor(
    private val timePeriodRepo: BaseTimePeriodRepo
) {

    suspend fun getAll(): List<TimePeriod> = withContext(Dispatchers.IO) {
        timePeriodRepo.getAll()
    }

    suspend fun add(timePeriod: TimePeriod) = withContext(Dispatchers.IO) {
        timePeriodRepo.add(timePeriod)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        timePeriodRepo.clear()
    }
}