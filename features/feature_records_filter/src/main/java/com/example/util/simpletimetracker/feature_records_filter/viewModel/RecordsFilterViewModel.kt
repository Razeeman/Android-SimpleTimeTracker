package com.example.util.simpletimetracker.feature_records_filter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.RecordFilterViewData
import com.example.util.simpletimetracker.feature_records_filter.interactor.RecordsFilterViewDataInteractor
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterViewDataMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class RecordsFilterViewModel @Inject constructor(
    private val recordsFilterViewDataInteractor: RecordsFilterViewDataInteractor,
    private val recordsFilterViewDataMapper: RecordsFilterViewDataMapper,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
) : ViewModel() {

    val filtersViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadFiltersViewData()
            }
            initial
        }
    }

    val recordsViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadRecordsViewData()
            }
            initial
        }
    }

    private val filters: MutableList<RecordsFilter> = mutableListOf()
    private var recordsLoadJob: Job? = null

    fun onFilterClick(item: RecordFilterViewData) {
        viewModelScope.launch {
            if (item.enabled) {
                onFilterRemoveClick(item)
            } else {
                when (item.type) {
                    RecordFilterViewData.Type.ACTIVITY -> {
                        val type = recordTypeInteractor.getAll().firstOrNull() ?: return@launch
                        filters.add(RecordsFilter.Activity(listOf(type.id)))
                    }
                    RecordFilterViewData.Type.COMMENT -> {
                        filters.add(RecordsFilter.Comment("comment"))
                    }
                    RecordFilterViewData.Type.DATE -> {
                        val day = TimeUnit.DAYS.toMillis(1)
                        val range = Range(System.currentTimeMillis() - day, System.currentTimeMillis())
                        filters.add(RecordsFilter.Date(range))
                    }
                    RecordFilterViewData.Type.SELECTED_TAGS -> {
                        val tag = recordTagInteractor.getAll().firstOrNull() ?: return@launch
                        filters.add(RecordsFilter.SelectedTags(listOf(RecordsFilter.Tag.Tagged(tag.id))))
                    }
                    RecordFilterViewData.Type.FILTERED_TAGS -> {
                        val tag = recordTagInteractor.getAll().firstOrNull() ?: return@launch
                        filters.add(RecordsFilter.FilteredTags(listOf(RecordsFilter.Tag.Tagged(tag.id))))
                    }
                }
                updateFilters()
                updateRecords()
            }
        }
    }

    fun onFilterRemoveClick(item: RecordFilterViewData) {
        val filterClass = recordsFilterViewDataMapper.mapToClass(item.type)
        filters.removeAll { filterClass.isInstance(it) }
        updateFilters()
        updateRecords()
    }

    fun onRecordClick(item: RecordViewData, sharedElements: Pair<Any, String>) {
        // TODO
    }

    private fun updateFilters() = viewModelScope.launch {
        val data = loadFiltersViewData()
        filtersViewData.set(data)
    }

    private suspend fun loadFiltersViewData(): List<ViewHolderType> {
        return recordsFilterViewDataInteractor.getFiltersViewData(filters)
    }

    private fun updateRecords() {
        recordsLoadJob?.cancel()
        recordsLoadJob = viewModelScope.launch {
            recordsViewData.set(listOf(LoaderViewData()))
            val data = loadRecordsViewData()
            recordsViewData.set(data)
        }
    }

    private suspend fun loadRecordsViewData(): List<ViewHolderType> {
        return recordsFilterViewDataInteractor.getViewData(filters)
    }
}
