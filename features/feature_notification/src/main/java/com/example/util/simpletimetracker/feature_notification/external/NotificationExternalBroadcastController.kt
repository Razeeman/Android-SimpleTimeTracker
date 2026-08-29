package com.example.util.simpletimetracker.feature_notification.external

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationExternalBroadcastController @Inject constructor(
    private val externalBroadcastInteractor: ExternalBroadcastInteractor,
) {

    private val mutex = Mutex()

    suspend fun onActionExternalActivityStart(
        name: String?,
        comment: String?,
        tagNames: List<String>,
        timeStarted: String?,
    ) {
        name ?: return
        mutex.withLock {
            externalBroadcastInteractor.onActionActivityStart(
                name = name,
                comment = comment,
                tagNames = tagNames,
                timeStarted = timeStarted,
            )
        }
    }

    suspend fun onActionExternalActivityStop(
        name: String?,
        timeEnded: String?,
    ) {
        name ?: return
        mutex.withLock {
            externalBroadcastInteractor.onActionActivityStopByName(
                name = name,
                timeEnded = timeEnded,
            )
        }
    }

    suspend fun onActionExternalActivityStopAll() {
        mutex.withLock {
            externalBroadcastInteractor.onActionActivityStopAll()
        }
    }

    suspend fun onActionExternalActivityStopShortest() {
        mutex.withLock {
            externalBroadcastInteractor.onActionActivityStopShortest()
        }
    }

    suspend fun onActionExternalActivityStopLongest() {
        mutex.withLock {
            externalBroadcastInteractor.onActionActivityStopLongest()
        }
    }

    suspend fun onActionExternalActivityRestart(
        comment: String?,
        tagNames: List<String>,
    ) {
        mutex.withLock {
            externalBroadcastInteractor.onActionActivityRestart(
                comment = comment, tagNames = tagNames,
            )
        }
    }

    suspend fun onActionExternalRecordAdd(
        name: String?,
        timeStarted: String?,
        timeEnded: String?,
        comment: String?,
        tagNames: List<String>,
    ) {
        name ?: return
        timeStarted ?: return
        timeEnded ?: return
        mutex.withLock {
            externalBroadcastInteractor.onRecordAdd(
                name = name,
                timeStarted = timeStarted,
                timeEnded = timeEnded,
                comment = comment,
                tagNames = tagNames,
            )
        }
    }

    suspend fun onActionExternalRecordChange(
        findMode: String?,
        name: String?,
        comment: String?,
        commentMode: String?,
    ) {
        mutex.withLock {
            externalBroadcastInteractor.onRecordChange(
                findModeData = findMode,
                name = name,
                comment = comment,
                commentModeData = commentMode,
            )
        }
    }

    suspend fun onActionExternalRecordTagAdd(
        name: String?,
        icon: String?,
    ) {
        mutex.withLock {
            externalBroadcastInteractor.onRecordTagAdd(
                name = name,
                icon = icon,
            )
        }
    }
}