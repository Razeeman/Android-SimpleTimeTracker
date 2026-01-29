package com.example.util.simpletimetracker.feature_dialogs.cardSize.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.utils.SuspendedValue
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_dialogs.cardSize.mapper.CardSizeViewDataMapper
import com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData.CardSizeButtonsViewData
import com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData.CardSizeDefaultButtonViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardSizeViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val cardSizeViewDataMapper: CardSizeViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
) : BaseViewModel() {

    val recordTypes: LiveData<List<ViewHolderType>> by lazySuspend {
        viewModelScope.launch { updateRecordTypes() }
        listOf(LoaderViewData())
    }
    val buttons: LiveData<List<ViewHolderType>> by lazySuspend {
        loadButtonsViewData()
    }
    val defaultButton: LiveData<CardSizeDefaultButtonViewData> by lazySuspend {
        loadDefaultButtonViewData()
    }

    private val numberOfCards = SuspendedValue(prefsInteractor::getNumberOfCards)
    private var types: List<RecordType> = emptyList()

    fun onDismiss() {
        MainScope().launch {
            prefsInteractor.setNumberOfCards(numberOfCards.get())
        }
    }

    fun onButtonClick(viewData: ButtonsRowViewData) {
        if (viewData !is CardSizeButtonsViewData) return
        viewModelScope.launch {
            numberOfCards.set(viewData.numberOfCards)
            updateRecordTypes()
            updateButtonsViewData()
            updateDefaultButton()
        }
    }

    fun onDefaultButtonClick() = viewModelScope.launch {
        numberOfCards.set(0)
        updateRecordTypes()
        updateButtonsViewData()
        updateDefaultButton()
    }

    private suspend fun updateButtonsViewData() {
        val data = loadButtonsViewData()
        buttons.set(data)
    }

    private suspend fun updateDefaultButton() {
        val data = loadDefaultButtonViewData()
        defaultButton.set(data)
    }

    private suspend fun updateRecordTypes() {
        val data = loadRecordTypes()
        recordTypes.set(data)
    }

    private suspend fun loadButtonsViewData(): List<ViewHolderType> {
        return cardSizeViewDataMapper.toToButtonsViewData(numberOfCards.get())
    }

    private suspend fun loadDefaultButtonViewData(): CardSizeDefaultButtonViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return cardSizeViewDataMapper.toDefaultButtonViewData(
            numberOfCards = numberOfCards.get(),
            isDarkTheme = isDarkTheme,
        )
    }

    private suspend fun loadRecordTypes(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        if (types.isEmpty()) {
            types = recordTypeInteractor.getAll().filter { !it.hidden }
        }

        return types
            .map { type ->
                cardSizeViewDataMapper.toToRecordTypeViewData(
                    recordType = type,
                    numberOfCards = numberOfCards.get(),
                    isDarkTheme = isDarkTheme,
                )
            }
            .takeUnless { it.isEmpty() }
            ?: recordTypeViewDataMapper.mapToEmpty()
    }
}
