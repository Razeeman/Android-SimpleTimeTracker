package com.example.util.simpletimetracker.domain

interface BaseTimePeriodRepo {

    suspend fun getAll(): List<TimePeriod>

    suspend fun add(timePeriod: TimePeriod)

    suspend fun clear()
}