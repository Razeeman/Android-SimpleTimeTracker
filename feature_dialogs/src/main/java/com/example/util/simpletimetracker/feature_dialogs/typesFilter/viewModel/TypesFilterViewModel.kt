package com.example.util.simpletimetracker.feature_dialogs.typesFilter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.extension.post
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.extra.TypesFilterExtra
import kotlinx.coroutines.launch
import javax.inject.Inject

class TypesFilterViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordInteractor: RecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper
) : ViewModel() {

    lateinit var extra: TypesFilterExtra

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadRecordTypesViewData()
            }
            initial
        }
    }
    val typesSelected: LiveData<List<Long>> by lazy {
        MutableLiveData(extra.selectedTypes)
    }

    private var types: List<RecordType> = emptyList()

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            val currentTypesSelected = typesSelected.value.orEmpty().toMutableList()

            if (item.id in currentTypesSelected) {
                currentTypesSelected.remove(item.id)
            } else {
                currentTypesSelected.add(item.id)
            }

            (typesSelected as MutableLiveData).value = currentTypesSelected
            updateRecordTypesViewData()
        }
    }

    fun onShowAllClick() {
        (typesSelected as MutableLiveData).value = types.map { it.id }
        updateRecordTypesViewData()
    }

    fun onHideAllClick() {
        (typesSelected as MutableLiveData).value = emptyList()
        updateRecordTypesViewData()
    }

    private fun updateRecordTypesViewData() = viewModelScope.launch {
        val data = loadRecordTypesViewData()
        recordTypes.post(data)
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val typesSelected = typesSelected.value.orEmpty()

        if (types.isEmpty()) types = loadRecordTypes()

        return types.map { type ->
            recordTypeViewDataMapper.mapFiltered(
                recordType = type,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
                isFiltered = type.id !in typesSelected
            )
        }
    }

    private suspend fun loadRecordTypes(): List<RecordType> {
        val typesWithRecords = recordInteractor.getAll()
            .map(Record::typeId)
            .toSet()

        return recordTypeInteractor.getAll()
            .filter { it.id in typesWithRecords }
    }
}
