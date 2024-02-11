package com.example.util.simpletimetracker.feature_change_record_tag.viewData

import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData

data class ChangeRecordTagTypeSwitchViewData(
    val tagType: RecordTagType,
    override val name: String,
    override val isSelected: Boolean,
) : ButtonsRowViewData() {

    override val id: Long = tagType.ordinal.toLong()
}