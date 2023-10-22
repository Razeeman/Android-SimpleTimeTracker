package com.example.util.simpletimetracker.feature_dialogs.defaultTypesSelection.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_dialogs.defaultTypesSelection.interactor.GetDefaultRecordTypesInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DefaultTypesSelectionViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val getDefaultRecordTypesInteractor: GetDefaultRecordTypesInteractor,
) : ViewModel() {

    val types: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadRecordTypesViewData()
            }
            initial
        }
    }
    val close: LiveData<Unit> = MutableLiveData()
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)

    private var recordTypes: List<RecordType> = emptyList()
    private var typeIdsFiltered: MutableList<Long> = mutableListOf()

    fun onRecordTypeClick(item: RecordTypeViewData) {
        typeIdsFiltered.addOrRemove(item.id)
        updateSaveButtonEnabled()
        updateRecordTypesViewData()
    }

    fun onShowAllClick() {
        typeIdsFiltered.clear()
        updateSaveButtonEnabled()
        updateRecordTypesViewData()
    }

    fun onHideAllClick() {
        typeIdsFiltered.addAll(recordTypes.map { it.id })
        updateSaveButtonEnabled()
        updateRecordTypesViewData()
    }

    fun onSaveClick() {
        if (typeIdsFiltered.size == recordTypes.size) return

        saveButtonEnabled.set(false)
        viewModelScope.launch {
            recordTypes.filter { it.id !in typeIdsFiltered }.forEach {
                // Remove ids for correct adding in database.
                recordTypeInteractor.add(it.copy(id = 0))
            }
            prefsInteractor.setCardOrder(CardOrder.COLOR)
            close.set(Unit)
        }
    }

    private fun updateSaveButtonEnabled() {
        saveButtonEnabled.set(typeIdsFiltered.size != recordTypes.size)
    }

    private fun updateRecordTypesViewData() = viewModelScope.launch {
        val data = loadRecordTypesViewData()
        types.set(data)
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        if (recordTypes.isEmpty()) recordTypes = loadRecordTypes()

        return recordTypes.map { recordType ->
            recordTypeViewDataMapper.mapFiltered(
                recordType = recordType,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
                isFiltered = recordType.id in typeIdsFiltered,
                isChecked = null,
            )
        }
    }

    private suspend fun loadRecordTypes(): List<RecordType> {
        return getDefaultRecordTypesInteractor.execute()
    }
}
