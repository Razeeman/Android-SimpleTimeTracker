package com.example.util.simpletimetracker.feature_dialogs.cardSize.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_dialogs.cardSize.mapper.CardSizeViewDataMapper
import com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData.CardSizeButtonsViewData
import com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData.CardSizeDefaultButtonViewData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class CardSizeViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val cardSizeViewDataMapper: CardSizeViewDataMapper
) : ViewModel() {

    // TODO fix loader
    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordTypes() }
            initial
        }
    }
    val buttons: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData<List<ViewHolderType>>().let { initial ->
            initial.value = loadButtonsViewData()
            initial
        }
    }
    val defaultButton: LiveData<CardSizeDefaultButtonViewData> by lazy {
        MutableLiveData<CardSizeDefaultButtonViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadDefaultButtonViewData()
            }
            initial
        }
    }

    private var numberOfCards: Int = runBlocking { prefsInteractor.getNumberOfCards() }
    private var types: List<RecordType> = emptyList()

    fun onDismiss() {
        GlobalScope.launch {
            prefsInteractor.setNumberOfCards(numberOfCards)
        }
    }

    fun onButtonClick(viewData: ButtonsRowViewData) {
        if (viewData !is CardSizeButtonsViewData) return
        numberOfCards = viewData.numberOfCards
        updateRecordTypes()
        updateButtonsViewData()
        updateDefaultButton()
    }

    fun onDefaultButtonClick() {
        numberOfCards = 0
        updateRecordTypes()
        updateButtonsViewData()
        updateDefaultButton()
    }

    private fun updateButtonsViewData() {
        (buttons as MutableLiveData).value = loadButtonsViewData()
    }

    private fun updateDefaultButton() = viewModelScope.launch {
        (defaultButton as MutableLiveData).value = loadDefaultButtonViewData()
    }

    private fun updateRecordTypes() = viewModelScope.launch {
        (recordTypes as MutableLiveData).value = loadRecordTypes()
    }

    private fun loadButtonsViewData(): List<ViewHolderType> {
        return cardSizeViewDataMapper.toToButtonsViewData(numberOfCards)
    }

    private suspend fun loadDefaultButtonViewData(): CardSizeDefaultButtonViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return cardSizeViewDataMapper.toDefaultButtonViewData(numberOfCards, isDarkTheme)
    }

    private suspend fun loadRecordTypes(): List<RecordTypeViewData> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        if (types.isEmpty()) {
            types = recordTypeInteractor.getAll().filter { !it.hidden }
        }
        return types.map { type ->
            cardSizeViewDataMapper.toToRecordTypeViewData(type, numberOfCards, isDarkTheme)
        }
    }
}
