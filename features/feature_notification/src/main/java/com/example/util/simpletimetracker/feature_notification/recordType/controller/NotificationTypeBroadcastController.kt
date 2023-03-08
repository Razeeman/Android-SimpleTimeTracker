package com.example.util.simpletimetracker.feature_notification.recordType.controller

import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.feature_notification.recordType.interactor.ActivityStartStopFromBroadcastInteractor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class NotificationTypeBroadcastController @Inject constructor(
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val activityStartStopFromBroadcastInteractor: ActivityStartStopFromBroadcastInteractor,
) {

    fun onActionActivityStart(
        name: String?,
        comment: String?,
        tagName: String?,
    ) {
        name ?: return
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionActivityStart(
                name = name, comment = comment, tagName = tagName
            )
        }
    }

    fun onActionActivityStart(
        typeId: Long,
    ) {
        if (typeId == 0L) return
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionActivityStart(typeId)
        }
    }

    fun onActionActivityStop(
        name: String?
    ) {
        name ?: return
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionActivityStop(name)
        }
    }

    fun onActionActivityStop(
        typeId: Long
    ) {
        if (typeId == 0L) return
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionActivityStop(typeId)
        }
    }

    fun onActionActivityStopAll() {
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionActivityStopAll()
        }
    }

    fun onActionActivityStopShortest() {
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionActivityStopShortest()
        }
    }

    fun onActionActivityStopLongest() {
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionActivityStopLongest()
        }
    }

    fun onActionActivityRestart(
        comment: String?,
        tagName: String?,
    ) {
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionActivityRestart(
                comment = comment, tagName = tagName
            )
        }
    }

    fun onActionTypesPrev(
        typeId: Long,
        shift: Int,
    ) {
        if (typeId == 0L) return
        GlobalScope.launch {
            notificationTypeInteractor.checkAndShow(typeId, shift)
        }
    }

    fun onActionTypesNext(
        typeId: Long,
        shift: Int,
    ) {
        if (typeId == 0L) return
        GlobalScope.launch {
            notificationTypeInteractor.checkAndShow(typeId, shift)
        }
    }

    fun onBootCompleted() {
        GlobalScope.launch {
            notificationTypeInteractor.updateNotifications()
        }
    }
}