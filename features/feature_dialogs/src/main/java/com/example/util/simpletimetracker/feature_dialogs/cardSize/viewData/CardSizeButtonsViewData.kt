package com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData

import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData

data class CardSizeButtonsViewData(
    val numberOfCards: Int,
    override val name: String,
    override val isSelected: Boolean,
) : ButtonsRowViewData() {

    override val id: Long = numberOfCards.toLong()
}