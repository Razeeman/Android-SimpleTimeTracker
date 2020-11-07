package com.example.util.simpletimetracker.feature_dialogs.typesFilter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
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
        updateRecordTypes()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
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
            updateRecordTypes()
        }
    }

    fun onShowAllClick() {
        (typesSelected as MutableLiveData).value = types.map { it.id }
        updateRecordTypes()
    }

    fun onHideAllClick() {
        (typesSelected as MutableLiveData).value = emptyList()
        updateRecordTypes()
    }

    private fun updateRecordTypes() = viewModelScope.launch {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val typesSelected = typesSelected.value.orEmpty()

        if (types.isEmpty()) types = loadRecordTypes()

        (recordTypes as MutableLiveData).value = types.map { type ->
            recordTypeViewDataMapper.mapFiltered(
                type,
                numberOfCards,
                isDarkTheme,
                type.id !in typesSelected
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
