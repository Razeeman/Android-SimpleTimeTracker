package com.example.util.simpletimetracker.core.utils

import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.domain.interactor.UpdateRunningRecordFromChangeScreenInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_views.RunningRecordView
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr

/**
 * Sets card background depending if it was clicked before (eg. opening a chooser by clicking on card).
 */
fun CardView.setChooserColor(opened: Boolean) {
    val colorAttr = if (opened) {
        R.attr.appInputFieldBorderColor
    } else {
        R.attr.appBackgroundColor
    }
    context.getThemedAttr(colorAttr).let(::setCardBackgroundColor)
}

fun updateRunningRecordPreview(
    currentList: List<ViewHolderType>,
    recyclerView: RecyclerView,
    update: UpdateRunningRecordFromChangeScreenInteractor.Update,
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

            // Update if goal was shown and need update.
            if (it.itemGoalTime.isNotEmpty() && update.goalText.isNotEmpty()) {
                it.itemGoalTime = update.goalText
                it.itemGoalTimeComplete = update.goalComplete
            }

            update.additionalData?.let { additionalUpdate ->
                it.itemTagName = additionalUpdate.tagName
                it.itemTimeStarted = additionalUpdate.timeStarted
                it.itemComment = additionalUpdate.comment
            }
        }
}