package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.DeviceRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import javax.inject.Inject

class RecordTypeCardSizeMapper @Inject constructor(
    private val deviceRepo: DeviceRepo,
    private val resourceRepo: ResourceRepo
) {

    private val defaultWidth: Int by lazy { resourceRepo.getDimenInDp(R.dimen.record_type_card_width) }
    private val defaultHeight: Int by lazy { resourceRepo.getDimenInDp(R.dimen.record_type_card_height) }
    private val rowHeight: Int by lazy { resourceRepo.getDimenInDp(R.dimen.record_type_card_height_row) }
    private val widthMargin: Int by lazy { resourceRepo.getDimenInDp(R.dimen.record_type_card_screen_width_margin) }

    fun toCardWidth(numberOfCards: Int): Int {
        return if (numberOfCards == 0) defaultWidth else (deviceRepo.getScreenWidthInDp() - widthMargin) / numberOfCards
    }

    fun toCardHeight(numberOfCards: Int): Int {
        return if (numberOfCards in 1..3) rowHeight else defaultHeight
    }

    fun toCardAsRow(numberOfCards: Int): Boolean {
        return numberOfCards in 1..3
    }
}