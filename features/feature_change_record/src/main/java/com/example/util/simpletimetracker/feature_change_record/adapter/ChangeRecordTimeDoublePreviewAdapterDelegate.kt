package com.example.util.simpletimetracker.feature_change_record.adapter

import androidx.core.view.isVisible
import com.example.util.simpletimetracker.core.utils.setChooserColor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.model.TimeAdjustmentState
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeDoublePreviewViewData as ViewData
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordTimeDoublePreviewItemBinding as Binding

fun createChangeRecordTimeDoublePreviewAdapterDelegate(
    onTimeStartedClick: (ViewData) -> Unit,
    onTimeEndedClick: (ViewData) -> Unit,
    onAdjustTimeStartedClick: (ViewData) -> Unit,
    onAdjustTimeEndedClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvChangeRecordChangeCurrentPreviewTimeStarted.text = item.dateTimeStarted
        tvChangeRecordChangeCurrentPreviewTimeEnded.text = item.dateTimeFinished
        fieldChangeRecordChangeCurrentPreviewTimeEnded.isVisible = item.isTimeEndedAvailable
        btnChangeRecordChangeCurrentPreviewTimeStartedAdjust.isVisible = item.isTimeEndedAvailable
        btnChangeRecordChangeCurrentPreviewTimeEndedAdjust.isVisible = item.isTimeEndedAvailable
        btnChangeRecordChangeCurrentPreviewTimeStartedAdjust
            .setChooserColor(item.state == TimeAdjustmentState.TIME_STARTED)
        btnChangeRecordChangeCurrentPreviewTimeEndedAdjust
            .setChooserColor(item.state == TimeAdjustmentState.TIME_ENDED)

        fieldChangeRecordChangeCurrentPreviewTimeStarted.setOnClickWith(item, onTimeStartedClick)
        fieldChangeRecordChangeCurrentPreviewTimeEnded.setOnClickWith(item, onTimeEndedClick)
        btnChangeRecordChangeCurrentPreviewTimeStartedAdjust.setOnClickWith(item, onAdjustTimeStartedClick)
        btnChangeRecordChangeCurrentPreviewTimeEndedAdjust.setOnClickWith(item, onAdjustTimeEndedClick)
    }
}

data class ChangeRecordTimeDoublePreviewViewData(
    val block: ChangeRecordActionsBlock,
    val dateTimeStarted: String,
    val dateTimeFinished: String,
    val isTimeEndedAvailable: Boolean,
    val state: TimeAdjustmentState,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData
}