package com.example.util.simpletimetracker.data

import com.example.util.simpletimetracker.domain.TimePeriod
import javax.inject.Inject

class TimePeriodMapper @Inject constructor() {

    fun map(dbo: TimePeriodDBO): TimePeriod {
        return TimePeriod(
            id = dbo.id,
            name = dbo.name,
            timeStarted = dbo.timeStarted,
            timeEnded = dbo.timeEnded
        )
    }

    fun map(domain: TimePeriod): TimePeriodDBO {
        return TimePeriodDBO(
            id = 0,
            name = domain.name,
            timeStarted = domain.timeStarted,
            timeEnded = domain.timeEnded
        )
    }
}