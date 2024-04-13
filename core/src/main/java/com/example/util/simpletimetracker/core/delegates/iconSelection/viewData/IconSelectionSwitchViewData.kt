package com.example.util.simpletimetracker.core.delegates.iconSelection.viewData

import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.model.IconType

data class IconSelectionSwitchViewData(
    val iconType: IconType,
    override val name: String,
    override val isSelected: Boolean,
) : ButtonsRowViewData() {

    override val id: Long = iconType.ordinal.toLong()
}