package com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypesFilteredInteractor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.mapper.ChartFilterViewDataMapper
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewData.ChartFilterRecordTypeViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChartFilterViewModel @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypesFilteredInteractor: RecordTypesFilteredInteractor,
    private val chartFilterViewDataMapper: ChartFilterViewDataMapper
) : ViewModel() {

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordTypesViewData() }
            initial
        }
    }

    private var types: List<RecordType> = emptyList()
    private var typeIdsFiltered: MutableList<Long> = mutableListOf()

    fun onRecordTypeClick(item: ChartFilterRecordTypeViewData) {
        viewModelScope.launch {
            if (item.id in typeIdsFiltered) {
                typeIdsFiltered.remove(item.id)
            } else {
                typeIdsFiltered.add(item.id)
            }
            recordTypesFilteredInteractor.setFilteredTypes(typeIdsFiltered)
            updateRecordTypes()
        }
    }

    private fun updateRecordTypes() {
        viewModelScope.launch {
            (recordTypes as MutableLiveData).value = types
                .map { type -> chartFilterViewDataMapper.map(type, typeIdsFiltered) }
                .apply {
                    this as MutableList
                    add(chartFilterViewDataMapper.mapToUntrackedItem(typeIdsFiltered))
                }
        }
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        val typesInStatistics = recordInteractor.getAll()
            .map(Record::typeId)
            .toSet()
        typeIdsFiltered = recordTypesFilteredInteractor.getFilteredTypes()
            .toMutableList()

        return recordTypeInteractor.getAll()
            .filter { it.id in typesInStatistics }
            .also { types = it }
            .map { types -> chartFilterViewDataMapper.map(types, typeIdsFiltered) }
            .apply {
                this as MutableList
                add(chartFilterViewDataMapper.mapToUntrackedItem(typeIdsFiltered))
            }
    }
}
