package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordTypeGoal

interface NotificationGoalCountInteractor {

    suspend fun checkAndShow(typeId: Long)

    fun cancel(idData: RecordTypeGoal.IdData)
}