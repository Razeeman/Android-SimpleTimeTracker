package com.example.util.simpletimetracker.feature_change_record.viewData

import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_change_record.api.model.ChangeRecordActionsBlock

data class ChangeRecordQuickActionsButtonViewData(
    val block: ChangeRecordActionsBlock,
) : ButtonViewData.Id