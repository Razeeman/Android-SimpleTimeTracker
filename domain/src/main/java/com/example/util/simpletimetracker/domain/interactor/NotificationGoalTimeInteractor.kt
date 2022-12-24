package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.GoalTimeType

interface NotificationGoalTimeInteractor {

    suspend fun checkAndReschedule(typeId: Long)

    fun cancel(typeId: Long)

    fun cancel(typeId: Long, goalTimeType: GoalTimeType)

    fun show(typeId: Long, goalTimeType: GoalTimeType)
}