package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.utils.EVENT_STARTED_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EVENT_STOPPED_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EXTRA_ACTIVITY_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_COMMENT
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TAG_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TYPE_ICON
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TYPE_NOTE
import com.example.util.simpletimetracker.domain.interactor.ActivityStartedStoppedBroadcastInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
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

        val type = getActivity(typeId) ?: return
        sendBroadcast(
            actionString = EVENT_STARTED_ACTIVITY,
            activityName = type.name,
            comment = comment,
            tagNames = getTagNames(tagIds),
            note = type.note,
            icon = type.icon,
        )
    }

    override suspend fun onActivityStopped(
        typeId: Long,
        tagIds: List<Long>,
        comment: String,
    ) {
        if (!prefsInteractor.getAutomatedTrackingSendEvents()) return

        val type = getActivity(typeId) ?: return
        sendBroadcast(
            actionString = EVENT_STOPPED_ACTIVITY,
            activityName = type.name,
            comment = comment,
            tagNames = getTagNames(tagIds),
            note = type.note,
            icon = type.icon,
        )
    }

    private fun sendBroadcast(
        actionString: String,
        activityName: String,
        comment: String,
        tagNames: List<String>,
        note: String,
        icon: String,
    ) {
        val tagsString = tagNames.joinToString(separator = ",")
        Intent().apply {
            action = actionString
            putExtra(EXTRA_ACTIVITY_NAME, activityName)
            if (comment.isNotEmpty()) putExtra(EXTRA_RECORD_COMMENT, comment)
            if (tagNames.isNotEmpty()) putExtra(EXTRA_RECORD_TAG_NAME, tagsString)
            if (note.isNotEmpty()) putExtra(EXTRA_RECORD_TYPE_NOTE, note)
            if (icon.isNotEmpty()) putExtra(EXTRA_RECORD_TYPE_ICON, icon)
        }.let(context::sendBroadcast)
    }

    private suspend fun getActivity(
        typeId: Long,
    ): RecordType? {
        return recordTypeInteractor.get(typeId)
    }

    private suspend fun getTagNames(
        tagIds: List<Long>,
    ): List<String> {
        val tags = recordTagInteractor.getAll().associateBy(RecordTag::id)
        return tagIds.mapNotNull { tagId -> tags[tagId]?.name }
    }
}