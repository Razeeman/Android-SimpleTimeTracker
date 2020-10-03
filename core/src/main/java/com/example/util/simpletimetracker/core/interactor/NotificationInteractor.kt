package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.manager.NotificationManager
import com.example.util.simpletimetracker.core.manager.NotificationParams
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import javax.inject.Inject

class NotificationInteractor @Inject constructor(
    private val notificationManager: NotificationManager,
    private val recordTypeInteractor: RecordTypeInteractor
) {

    suspend fun showNotification(typeId: Long) {
        val text = recordTypeInteractor.get(typeId)?.name.orEmpty()
        notificationManager.show(NotificationParams(typeId.toInt(), text))
    }

    fun hideNotification(typeId: Long) {
        notificationManager.hide(typeId.toInt())
    }
}