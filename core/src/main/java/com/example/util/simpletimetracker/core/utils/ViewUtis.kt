package com.example.util.simpletimetracker.core.utils

import android.view.View
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.domain.record.interactor.UpdateRunningRecordsInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView.CheckState
import com.example.util.simpletimetracker.feature_views.RunningRecordView
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr

/**
 * Sets card background depending if it was clicked before (e.g. opening a chooser by clicking on card).
 */
fun View.setChooserColor(opened: Boolean) {
    val colorAttr = if (opened) {
        R.attr.appInputFieldBorderColor
    } else {
        R.attr.appBackgroundColor
    }
    val color = context.getThemedAttr(colorAttr)
    if (this is CardView) {
        setCardBackgroundColor(color)
    } else {
        setBackgroundColor(color)
    }
}

fun updateRunningRecordPreview(
    currentList: List<ViewHolderType>,
    recyclerView: RecyclerView,
    update: UpdateRunningRecordsInteractor.Update,
) {
    val itemIndex = currentList
        .indexOfFirst { it is RunningRecordViewData && it.id == update.id }
        .takeUnless { it == -1 }
        ?: return

    recyclerView.findViewHolderForAdapterPosition(itemIndex)
        ?.itemView?.findViewById<RunningRecordView>(R.id.viewRunningRecordItem)
        ?.let {
            it.itemTimer = update.timer

            if (it.itemTimerTotal.isNotEmpty() && update.timerTotal.isNotEmpty()) {
                it.itemTimerTotal = update.timerTotal
            }

            // Update if goals were shown and need update.
            if (it.itemGoalTimes.isNotEmpty() && update.goalTimes.isNotEmpty()) {
                it.itemGoalTimes = update.goalTimes.map { goalTime ->
                    RunningRecordView.GoalTime(
                        text = goalTime.text,
                        checkState = when (goalTime.state) {
                            null -> CheckState.HIDDEN
                            is RecordTypeGoal.Subtype.Goal -> CheckState.GOAL_REACHED
                            is RecordTypeGoal.Subtype.Limit -> CheckState.LIMIT_REACHED
                        },
                    )
                }
            }

            update.additionalData?.let { additionalUpdate ->
                it.itemTagName = additionalUpdate.tagName
                it.itemTimeStarted = additionalUpdate.timeStarted
                it.itemComment = additionalUpdate.comment
            }
        }
}