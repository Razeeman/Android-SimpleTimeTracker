package com.example.util.simpletimetracker.feature_notification.external

import android.content.Context
import android.content.Intent
import com.example.util.simpletimetracker.core.utils.EVENT_COMPLETED_GOAL
import com.example.util.simpletimetracker.core.utils.EVENT_STARTED_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EVENT_STOPPED_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EXTRA_ACTIVITY_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_CATEGORY_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_GOAL_TYPE
import com.example.util.simpletimetracker.core.utils.EXTRA_GOAL_VALUE
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_COMMENT
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TAG_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TYPE_ICON
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TYPE_NOTE
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.notifications.interactor.ActivityStartedStoppedBroadcastInteractor
import com.example.util.simpletimetracker.domain.notifications.model.ExternalEventGoalType
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ActivityStartedStoppedBroadcastInteractorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
) : ActivityStartedStoppedBroadcastInteractor {

    override suspend fun onActionActivityStarted(
        typeId: Long,
        tagIds: List<Long>,
        comment: String,
    ) {
        if (!prefsInteractor.getAutomatedTrackingSendEvents()) return

        val type = recordTypeInteractor.get(typeId) ?: return
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

        val type = recordTypeInteractor.get(typeId) ?: return
        sendBroadcast(
            actionString = EVENT_STOPPED_ACTIVITY,
            activityName = type.name,
            comment = comment,
            tagNames = getTagNames(tagIds),
            note = type.note,
            icon = type.icon,
        )
    }

    override suspend fun onGoalReached(
        idData: RecordTypeGoal.IdData,
        goalType: RecordTypeGoal.Type?,
    ) {
        if (!prefsInteractor.getAutomatedTrackingSendEvents()) return

        val type = (idData as? RecordTypeGoal.IdData.Type)?.value
            ?.let { recordTypeInteractor.get(it) }
        val category = (idData as? RecordTypeGoal.IdData.Category)?.value
            ?.let { categoryInteractor.get(it) }
        val tag = (idData as? RecordTypeGoal.IdData.Tag)?.value
            ?.let { recordTagInteractor.get(it) }
        if (type == null && category == null && tag == null) return

        sendGoalBroadcast(
            activityName = type?.name.orEmpty(),
            categoryName = category?.name.orEmpty(),
            tagName = tag?.name.orEmpty(),
            goalType = goalType,
            note = type?.note.orEmpty(),
            icon = type?.icon.orEmpty(),
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

    private fun sendGoalBroadcast(
        activityName: String,
        categoryName: String,
        tagName: String,
        goalType: RecordTypeGoal.Type?,
        note: String,
        icon: String,
    ) {
        val extraGoalType = when (goalType) {
            is RecordTypeGoal.Type.Duration -> ExternalEventGoalType.DURATION.dataValue
            is RecordTypeGoal.Type.Count -> ExternalEventGoalType.COUNT.dataValue
            null -> ""
        }
        val goalValue = goalType?.value.orZero()

        Intent().apply {
            action = EVENT_COMPLETED_GOAL
            if (activityName.isNotEmpty()) putExtra(EXTRA_ACTIVITY_NAME, activityName)
            if (categoryName.isNotEmpty()) putExtra(EXTRA_CATEGORY_NAME, categoryName)
            if (tagName.isNotEmpty()) putExtra(EXTRA_RECORD_TAG_NAME, categoryName)
            if (extraGoalType.isNotEmpty()) putExtra(EXTRA_GOAL_TYPE, extraGoalType)
            if (goalValue != 0L) putExtra(EXTRA_GOAL_VALUE, goalValue)
            if (note.isNotEmpty()) putExtra(EXTRA_RECORD_TYPE_NOTE, note)
            if (icon.isNotEmpty()) putExtra(EXTRA_RECORD_TYPE_ICON, icon)
        }.let(context::sendBroadcast)
    }

    private suspend fun getTagNames(
        tagIds: List<Long>,
    ): List<String> {
        val tags = recordTagInteractor.getAll().associateBy(RecordTag::id)
        return tagIds.mapNotNull { tagId -> tags[tagId]?.name }
    }
}