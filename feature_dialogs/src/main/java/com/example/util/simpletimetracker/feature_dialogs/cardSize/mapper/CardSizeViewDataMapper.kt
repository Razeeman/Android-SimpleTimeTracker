package com.example.util.simpletimetracker.feature_dialogs.cardSize.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.repo.DeviceRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData.CardSizeButtonsViewData
import com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData.CardSizeDefaultButtonViewData
import javax.inject.Inject

class CardSizeViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val deviceRepo: DeviceRepo
) {

    private val screenWidth: Int by lazy {
        deviceRepo.getScreenWidthInDp()
    }
    private val defaultCardWidth: Int by lazy {
        resourceRepo.getDimenInDp(R.dimen.record_type_card_width)
    }

    fun numberOfCardsToWidth(numberOfCards: Int): Int {
        if (numberOfCards <= 0) return defaultCardWidth
        return screenWidth / numberOfCards
    }

    fun mapToButtonsViewData(numberOfCards: Int): List<ViewHolderType> {
        return (1..6).map { buttonNumber ->
            CardSizeButtonsViewData(
                numberOfCards = buttonNumber,
                name = buttonNumber.toString(),
                isSelected = numberOfCards == buttonNumber
            )
        }
    }

    fun toDefaultButtonViewData(numberOfCards: Int): CardSizeDefaultButtonViewData {
        return CardSizeDefaultButtonViewData(
            color = (if (numberOfCards == 0) R.color.colorPrimary else R.color.blue_grey_300)
                .let(resourceRepo::getColor)
        )
    }
}