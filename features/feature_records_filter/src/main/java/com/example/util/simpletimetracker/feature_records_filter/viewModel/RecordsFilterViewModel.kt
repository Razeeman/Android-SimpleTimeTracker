package com.example.util.simpletimetracker.feature_records_filter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.RecordFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_records_filter.interactor.RecordsFilterViewDataInteractor
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterViewDataMapper
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class RecordsFilterViewModel @Inject constructor(
    private val viewDataInteractor: RecordsFilterViewDataInteractor,
    private val recordsFilterViewDataMapper: RecordsFilterViewDataMapper,
    private val recordTypeInteractor: RecordTypeInteractor,
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

    val filterSelectionContent: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadFilterSelectionViewData()
            }
            initial
        }
    }

    val recordsViewData: LiveData<RecordsFilterSelectedRecordsViewData> by lazy {
        return@lazy MutableLiveData<RecordsFilterSelectedRecordsViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = RecordsFilterSelectedRecordsViewData(
                    selectedRecordsCount = "",
                    recordsViewData = listOf(LoaderViewData()),
                )
                initial.value = loadRecordsViewData()
            }
            initial
        }
    }

    val filterSelectionVisibility: LiveData<Boolean> by lazy {
        MutableLiveData(loadFilterSelectionVisibility())
    }

    val keyboardVisibility: LiveData<Boolean> = MutableLiveData(false)

    private val filters: MutableList<RecordsFilter> = mutableListOf()
    private var filterSelectionState: RecordsFilterSelectionState = RecordsFilterSelectionState.Hidden
    private var recordsLoadJob: Job? = null

    // Cache
    private var types: List<RecordType> = emptyList()
    private var recordTypeCategories: List<RecordTypeCategory> = emptyList()
    private var categories: List<Category> = emptyList()
    private var recordTags: List<RecordTag> = emptyList()

    fun onFilterClick(item: RecordFilterViewData) {
        filterSelectionState = when (val currentFilterState = filterSelectionState) {
            is RecordsFilterSelectionState.Hidden -> {
                RecordsFilterSelectionState.Visible(item.type)
            }
            is RecordsFilterSelectionState.Visible -> {
                if (currentFilterState.type == item.type) {
                    RecordsFilterSelectionState.Hidden
                } else {
                    RecordsFilterSelectionState.Visible(item.type)
                }
            }
        }

        keyboardVisibility.set(false)
        updateFilters()
        updateFilterSelectionViewData()
        updateFilterSelectionVisibility()
    }

    fun onFilterRemoveClick(item: RecordFilterViewData) {
        removeFilter(item.type)
    }

    fun onFiltersSelected() {
        filterSelectionState = RecordsFilterSelectionState.Hidden
        updateFilterSelectionVisibility()
    }

    fun onRecordTypeClick(item: RecordTypeViewData) {
        val currentFilter = filters
            .filterIsInstance<RecordsFilter.Activity>()
            .firstOrNull()

        if (currentFilter == null) {
            val filter = RecordsFilter.Activity(listOf(item.id))
            filters.add(filter)
        } else {
            val newIds = currentFilter.typeIds
                .toMutableList()
                .apply { addOrRemove(item.id) }
            filters.removeAll { it is RecordsFilter.Activity }
            if (newIds.isNotEmpty()) filters.add(RecordsFilter.Activity(newIds))
        }

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    fun onCategoryClick(item: CategoryViewData) {
        // TODO add categories to activity filter
    }

    fun onCommentChange(text: String) {
        val currentFilter = filters
            .filterIsInstance<RecordsFilter.Comment>()
            .firstOrNull()

        if (currentFilter == null && text.isNotEmpty()) {
            val filter = RecordsFilter.Comment(text)
            filters.add(filter)
        } else {
            filters.removeAll { it is RecordsFilter.Comment }
            if (text.isNotEmpty()) filters.add(RecordsFilter.Comment(text))
        }

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    fun onRecordClick(
        item: RecordViewData,
        @Suppress("UNUSED_PARAMETER") sharedElements: Pair<Any, String>
    ) {
        // TODO add manual filter
    }

    private fun removeFilter(type: RecordFilterViewData.Type) {
        val filterClass = recordsFilterViewDataMapper.mapToClass(type)
        filters.removeAll { filterClass.isInstance(it) }
        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    private suspend fun getTypesCache(): List<RecordType> {
        return types.takeUnless { it.isEmpty() } ?: recordTypeInteractor.getAll()
    }

    private fun updateFilterSelectionVisibility() {
        val data = loadFilterSelectionVisibility()
        filterSelectionVisibility.set(data)
    }

    private fun loadFilterSelectionVisibility(): Boolean {
        return filterSelectionState is RecordsFilterSelectionState.Visible
    }

    private fun updateFilters() = viewModelScope.launch {
        val data = loadFiltersViewData()
        filtersViewData.set(data)
    }

    private suspend fun loadFiltersViewData(): List<ViewHolderType> {
        return viewDataInteractor.getFiltersViewData(filterSelectionState, filters)
    }

    private fun updateRecords() {
        recordsLoadJob?.cancel()
        recordsLoadJob = viewModelScope.launch {
            recordsViewData.set(
                RecordsFilterSelectedRecordsViewData(
                    selectedRecordsCount = "",
                    recordsViewData = listOf(LoaderViewData()),
                )
            )
            val data = loadRecordsViewData()
            recordsViewData.set(data)
        }
    }

    private suspend fun loadRecordsViewData(): RecordsFilterSelectedRecordsViewData {
        return viewDataInteractor.getViewData(filters)
    }

    private fun updateFilterSelectionViewData() = viewModelScope.launch {
        val data = loadFilterSelectionViewData()
        filterSelectionContent.set(data)
    }

    private suspend fun loadFilterSelectionViewData(): List<ViewHolderType> {
        val type = (filterSelectionState as? RecordsFilterSelectionState.Visible)
            ?.type ?: return emptyList()

        return when (type) {
            RecordFilterViewData.Type.ACTIVITY -> {
                viewDataInteractor.getActivityFilterSelectionViewData(
                    filters = filters,
                    types = getTypesCache(),
                )
            }
            RecordFilterViewData.Type.COMMENT -> {
                viewDataInteractor.getCommentFilterSelectionViewData(
                    filters = filters,
                )
            }
            else -> emptyList()
        }
    }
}
