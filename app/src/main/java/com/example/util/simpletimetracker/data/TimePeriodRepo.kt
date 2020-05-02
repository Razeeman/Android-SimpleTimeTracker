package com.example.util.simpletimetracker.data

import com.example.util.simpletimetracker.domain.BaseTimePeriodRepo
import com.example.util.simpletimetracker.domain.TimePeriod
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimePeriodRepo @Inject constructor(
    private val timePeriodDao: TimePeriodDao,
    private val timePeriodMapper: TimePeriodMapper
) : BaseTimePeriodRepo {

    override suspend fun getAll(): List<TimePeriod> {
        return timePeriodDao.getAll().map(timePeriodMapper::map)
    }

    override suspend fun add(timePeriod: TimePeriod) {
        timePeriodDao.insert(
            timePeriod.let(timePeriodMapper::map)
        )
    }
}