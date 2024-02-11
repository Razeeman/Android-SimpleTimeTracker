package com.example.util.simpletimetracker.feature_dialogs.cardSize.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_dialogs.cardSize.mapper.CardSizeViewDataMapper
import com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData.CardSizeButtonsViewData
import com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData.CardSizeDefaultButtonViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class CardSizeViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val cardSizeViewDataMapper: CardSizeViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
) : ViewModel() {

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadRecordTypes()
            }
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
        val data = loadButtonsViewData()
        buttons.set(data)
    }

    private fun updateDefaultButton() = viewModelScope.launch {
        val data = loadDefaultButtonViewData()
        defaultButton.set(data)
    }

    private fun updateRecordTypes() = viewModelScope.launch {
        val data = loadRecordTypes()
        recordTypes.set(data)
    }

    private fun loadButtonsViewData(): List<ViewHolderType> {
        return cardSizeViewDataMapper.toToButtonsViewData(numberOfCards)
    }

    private suspend fun loadDefaultButtonViewData(): CardSizeDefaultButtonViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return cardSizeViewDataMapper.toDefaultButtonViewData(numberOfCards, isDarkTheme)
    }

    private suspend fun loadRecordTypes(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        if (types.isEmpty()) {
            types = recordTypeInteractor.getAll().filter { !it.hidden }
        }

        return types
            .map { type ->
                cardSizeViewDataMapper.toToRecordTypeViewData(type, numberOfCards, isDarkTheme)
            }
            .takeUnless { it.isEmpty() }
            ?: recordTypeViewDataMapper.mapToEmpty()
    }
}
