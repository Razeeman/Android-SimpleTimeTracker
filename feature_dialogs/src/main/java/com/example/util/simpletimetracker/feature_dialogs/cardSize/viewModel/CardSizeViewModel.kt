package com.example.util.simpletimetracker.feature_dialogs.cardSize.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_dialogs.cardSize.mapper.CardSizeViewDataMapper
import com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData.CardSizeButtonsViewData
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class CardSizeViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val cardSizeViewDataMapper: CardSizeViewDataMapper
) : ViewModel() {

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        updateRecordTypes()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }
    val progress: LiveData<Int> by lazy {
        MutableLiveData<Int>().let { initial ->
            initial.value = loadProgress()
            initial
        }
    }
    val buttonsViewData: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData<List<ViewHolderType>>().let { initial ->
            initial.value = loadButtonsViewData()
            initial
        }
    }

    private var currentWidth: Int = runBlocking { prefsInteractor.getCardSize() } // TODO remove blocking call
    private var types: List<RecordType> = emptyList()

    fun onProgressChanged(progress: Int) {
        currentWidth = cardSizeViewDataMapper.progressToWidth(progress)
        updateRecordTypes()
        updateButtonsViewData()
    }

    fun onDismiss() {
        viewModelScope.launch {
            prefsInteractor.setCardSize(currentWidth)
        }
    }

    fun onButtonClick(viewData: ButtonsRowViewData) {
        if (viewData !is CardSizeButtonsViewData) return
        currentWidth = cardSizeViewDataMapper.buttonTypeToWidth(viewData.type)
        updateProgress()
        updateRecordTypes()
        updateButtonsViewData()
    }

    private fun updateProgress() {
        (progress as MutableLiveData).value = loadProgress()
    }

    private fun updateButtonsViewData() {
        (buttonsViewData as MutableLiveData).value = loadButtonsViewData()
    }

    private fun updateRecordTypes() = viewModelScope.launch {
        if (types.isEmpty()) types = loadRecordTypes()
        (recordTypes as MutableLiveData).value = types
            .map { type -> recordTypeViewDataMapper.map(type, currentWidth) }
    }

    private fun loadProgress() : Int {
        return cardSizeViewDataMapper.widthToProgress(currentWidth)
    }

    private fun loadButtonsViewData() : List<ViewHolderType> {
        return cardSizeViewDataMapper.mapToButtonsViewData(currentWidth)
    }

    private suspend fun loadRecordTypes(): List<RecordType> {
        return recordTypeInteractor.getAll().filter { !it.hidden }
    }
}
