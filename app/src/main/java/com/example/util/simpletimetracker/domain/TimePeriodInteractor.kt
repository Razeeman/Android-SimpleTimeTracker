package com.example.util.simpletimetracker.domain

import javax.inject.Inject

class TimePeriodInteractor @Inject constructor(
    private val timePeriodRepo: BaseTimePeriodRepo
) {

    suspend fun getAll(): List<TimePeriod> {
        return timePeriodRepo.getAll()
    }

    suspend fun add(timePeriod: TimePeriod) {
        timePeriodRepo.add(timePeriod)
    }
}