package com.example.util.simpletimetracker.feature_notification.external

import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_RESPONSE_COMMAND
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_START_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_STOP_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EXTRA_COMMAND_ACTION
import com.example.util.simpletimetracker.core.utils.EXTRA_REQUEST_ID
import com.example.util.simpletimetracker.core.utils.EXTRA_STATUS
import com.example.util.simpletimetracker.domain.notifications.model.ExternalCommandStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationExternalBroadcastController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val externalBroadcastInteractor: ExternalBroadcastInteractor,
    private val externalQueryBroadcastInteractor: ExternalQueryBroadcastInteractor,
) {

    private val mutex = Mutex()

    suspend fun onActionExternalActivityStart(
        name: String?,
        comment: String?,
        tagNames: List<String>,
        timeStarted: String?,
        requestId: String?,
        requestResponse: Boolean,
    ) {
        mutex.withLock {
            val status = if (name == null) {
                ExternalCommandStatus.INVALID_REQUEST
            } else {
                externalBroadcastInteractor.onActionActivityStart(
                    name = name,
                    comment = comment,
                    tagNames = tagNames,
                    timeStarted = timeStarted,
                    requestId = requestId,
                )
            }
            if (requestResponse) {
                sendResponse(
                    action = ACTION_EXTERNAL_START_ACTIVITY,
                    requestId = requestId,
                    status = status,
                )
            }
        }
    }

    suspend fun onActionExternalActivityStop(
        name: String?,
        timeEnded: String?,
        requestId: String?,
        requestResponse: Boolean,
    ) {
        mutex.withLock {
            val status = if (name == null) {
                ExternalCommandStatus.INVALID_REQUEST
            } else {
                externalBroadcastInteractor.onActionActivityStopByName(
                    name = name,
                    timeEnded = timeEnded,
                    requestId = requestId,
                )
            }
            if (requestResponse) {
                sendResponse(
                    action = ACTION_EXTERNAL_STOP_ACTIVITY,
                    requestId = requestId,
                    status = status,
                )
            }
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
        activityNames: List<String>,
        color: String?,
    ) {
        mutex.withLock {
            externalBroadcastInteractor.onRecordTagAdd(
                name = name,
                icon = icon,
                activityNames = activityNames,
                color = color,
            )
        }
    }

    suspend fun onActionExternalQueryActivities(answerType: String?) {
        mutex.withLock {
            externalQueryBroadcastInteractor.onActionQueryActivities(answerType)
        }
    }

    suspend fun onActionExternalQueryRunning(answerType: String?) {
        mutex.withLock {
            externalQueryBroadcastInteractor.onActionQueryRunning(answerType)
        }
    }

    suspend fun onActionExternalQueryRecords(shift: Int) {
        mutex.withLock {
            externalQueryBroadcastInteractor.onActionQueryRecords(shift)
        }
    }

    suspend fun onActionExternalQueryStatistics(shift: Int, filterType: String?) {
        mutex.withLock {
            externalQueryBroadcastInteractor.onActionQueryStatistics(shift, filterType)
        }
    }

    fun sendResponse(
        action: String,
        requestId: String?,
        status: ExternalCommandStatus,
    ) {
        Intent(ACTION_EXTERNAL_RESPONSE_COMMAND).apply {
            putExtra(EXTRA_COMMAND_ACTION, action)
            putExtra(EXTRA_STATUS, status.dataValue)
            if (requestId != null) putExtra(EXTRA_REQUEST_ID, requestId)
        }.let(context::sendBroadcast)
    }
}
