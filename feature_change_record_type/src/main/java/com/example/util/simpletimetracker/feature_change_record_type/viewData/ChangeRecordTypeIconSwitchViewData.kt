package com.example.util.simpletimetracker.feature_change_record_type.viewData

import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.model.IconType

data class ChangeRecordTypeIconSwitchViewData(
    val iconType: IconType,
    override val name: String,
    override val isSelected: Boolean
) : ButtonsRowViewData() {

    override val id: Long = iconType.ordinal.toLong()
}