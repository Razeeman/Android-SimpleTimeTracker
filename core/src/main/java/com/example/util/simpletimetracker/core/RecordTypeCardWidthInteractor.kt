package com.example.util.simpletimetracker.core

import com.example.util.simpletimetracker.core.repo.DeviceRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import javax.inject.Inject

class RecordTypeCardWidthInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val deviceRepo: DeviceRepo,
    private val resourceRepo: ResourceRepo
) {

    suspend fun getCardWidth(): Int {
        val numberOfCards = prefsInteractor.getNumberOfCards()

        return if (numberOfCards == 0) {
            resourceRepo.getDimenInDp(R.dimen.record_type_card_width)
        } else {
            deviceRepo.getScreenWidthInDp() / numberOfCards
        }
    }
}