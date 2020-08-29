package com.example.util.simpletimetracker.core

import com.example.util.simpletimetracker.core.repo.DeviceRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import javax.inject.Inject

class RecordTypeCardWidthMapper @Inject constructor(
    private val deviceRepo: DeviceRepo,
    private val resourceRepo: ResourceRepo
) {

    fun toCardWidth(numberOfCards: Int): Int {
        return if (numberOfCards == 0) {
            resourceRepo.getDimenInDp(R.dimen.record_type_card_width)
        } else {
            deviceRepo.getScreenWidthInDp() / numberOfCards
        }
    }

    fun toCardHeight(numberOfCards: Int): Int {
        return if (numberOfCards in 1..3) {
            resourceRepo.getDimenInDp(R.dimen.record_type_card_height_row)
        } else {
            resourceRepo.getDimenInDp(R.dimen.record_type_card_height)
        }
    }

    fun toCardAsRow(numberOfCards: Int): Boolean {
        return numberOfCards in 1..3
    }
}