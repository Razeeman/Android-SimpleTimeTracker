package com.example.util.simpletimetracker.feature_dialogs.cardSize.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData.CardSizeButtonsViewData
import com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData.CardSizeDefaultButtonViewData
import javax.inject.Inject

class CardSizeViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper
) {

    fun toToRecordTypeViewData(recordType: RecordType, numberOfCards: Int): RecordTypeViewData {
        return recordTypeViewDataMapper.map(recordType, numberOfCards)
    }

    fun toToButtonsViewData(numberOfCards: Int): List<ViewHolderType> {
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