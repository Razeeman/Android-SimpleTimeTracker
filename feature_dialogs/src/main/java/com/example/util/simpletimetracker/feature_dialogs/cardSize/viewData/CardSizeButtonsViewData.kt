package com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData

import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData

data class CardSizeButtonsViewData(
    val type: Type,
    override val name: String,
    override val isSelected: Boolean
) : ButtonsRowViewData() {

    override val id: Long = type.ordinal.toLong()

    enum class Type {
        MIN, DEFAULT, MAX
    }
}