package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.utils.EVENT_STARTED_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EVENT_STOPPED_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EXTRA_ACTIVITY_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_COMMENT
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TAG_NAME
import com.example.util.simpletimetracker.domain.interactor.ActivityStartedStoppedBroadcastInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordTag
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ActivityStartedStoppedBroadcastInteractorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
) : ActivityStartedStoppedBroadcastInteractor {

    override suspend fun onActionActivityStarted(
        typeId: Long,
        tagIds: List<Long>,
        comment: String,
    ) {
        if (!prefsInteractor.getAutomatedTrackingSendEvents()) return

        sendBroadcast(
            actionString = EVENT_STARTED_ACTIVITY,
            activityName = getActivityName(typeId) ?: return,
            comment = comment,
            tagNames = getTagNames(tagIds),
        )
    }

    override suspend fun onActivityStopped(
        typeId: Long,
        tagIds: List<Long>,
        comment: String,
    ) {
        if (!prefsInteractor.getAutomatedTrackingSendEvents()) return

        sendBroadcast(
            actionString = EVENT_STOPPED_ACTIVITY,
            activityName = getActivityName(typeId) ?: return,
            comment = comment,
            tagNames = getTagNames(tagIds),
        )
    }

    private fun sendBroadcast(
        actionString: String,
        activityName: String,
        comment: String,
        tagNames: List<String>,
    ) {
        val tagsString = tagNames.joinToString(separator = ",")
        Intent().apply {
            action = actionString
            putExtra(EXTRA_ACTIVITY_NAME, activityName)
            if (comment.isNotEmpty()) putExtra(EXTRA_RECORD_COMMENT, comment)
            if (tagNames.isNotEmpty()) putExtra(EXTRA_RECORD_TAG_NAME, tagsString)
        }.let(context::sendBroadcast)
    }

    private suspend fun getActivityName(
        typeId: Long,
    ): String? {
        return recordTypeInteractor.get(typeId)?.name
    }

    private suspend fun getTagNames(
        tagIds: List<Long>,
    ): List<String> {
        val tags = recordTagInteractor.getAll().associateBy(RecordTag::id)
        return tagIds.mapNotNull { tagId -> tags[tagId]?.name }
    }
}