package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.DayOfWeek

interface GetCurrentDayInteractor {

    suspend fun execute(timestamp: Long): DayOfWeek
}