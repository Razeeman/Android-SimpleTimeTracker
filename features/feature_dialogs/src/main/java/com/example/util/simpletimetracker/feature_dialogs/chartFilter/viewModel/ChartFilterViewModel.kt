package com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.ChartFilterViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.ChartFilterViewDataMapper
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.core.viewData.ChartFilterTypeViewData
import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.interactor.ChartFilterInteractor
import com.example.util.simpletimetracker.navigation.params.screen.ChartFilterDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChartFilterViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val chartFilterViewDataMapper: ChartFilterViewDataMapper,
    private val chartFilterInteractor: ChartFilterInteractor,
    private val chartFilterViewDataInteractor: ChartFilterViewDataInteractor,
) : ViewModel() {

    lateinit var extra: ChartFilterDialogParams

    val filterTypeViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initializeChartFilterType()
                initial.value = loadFilterTypeViewData()
            }
            initial
        }
    }
    val types: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initializeChartFilterType()
                initial.value = listOf(LoaderViewData())
                initial.value = loadTypesViewData()
            }
            initial
        }
    }

    private var filterType: ChartFilterType = ChartFilterType.ACTIVITY

    private var recordTypesCache: List<RecordType>? = null
    private var categoriesCache: List<Category>? = null
    private var recordTagsCache: List<RecordTag>? = null

    private var typeIdsFiltered: MutableList<Long> = mutableListOf()
    private var categoryIdsFiltered: MutableList<Long> = mutableListOf()
    private var recordTagIdsFiltered: MutableList<Long> = mutableListOf()

    fun onFilterTypeClick(viewData: ButtonsRowViewData) {
        viewModelScope.launch {
            if (viewData !is ChartFilterTypeViewData) return@launch
            filterType = viewData.filterType
            chartFilterInteractor.setChartFilterType(extra, filterType)
            updateFilterTypeViewData()
            updateTypesViewData()
        }
    }

    fun onRecordTypeClick(item: RecordTypeViewData) = viewModelScope.launch {
        typeIdsFiltered.addOrRemove(item.id)
        chartFilterInteractor.setFilteredTypes(extra, typeIdsFiltered)
        updateRecordTypesViewData()
    }

    fun onCategoryClick(item: CategoryViewData) = viewModelScope.launch {
        when (item) {
            is CategoryViewData.Category -> {
                categoryIdsFiltered.addOrRemove(item.id)
                chartFilterInteractor.setFilteredCategories(extra, categoryIdsFiltered)
                updateCategoriesViewData()
            }
            is CategoryViewData.Record -> {
                recordTagIdsFiltered.addOrRemove(item.id)
                chartFilterInteractor.setFilteredTags(extra, recordTagIdsFiltered)
                updateTagsViewData()
            }
        }
    }

    fun onShowAllClick() = viewModelScope.launch {
        when (filterType) {
            ChartFilterType.ACTIVITY -> {
                typeIdsFiltered.clear()
                chartFilterInteractor.setFilteredTypes(extra, typeIdsFiltered)
            }
            ChartFilterType.CATEGORY -> {
                categoryIdsFiltered.clear()
                chartFilterInteractor.setFilteredCategories(extra, categoryIdsFiltered)
            }
            ChartFilterType.RECORD_TAG -> {
                recordTagIdsFiltered.clear()
                chartFilterInteractor.setFilteredTags(extra, recordTagIdsFiltered)
            }
        }
        updateTypesViewData()
    }

    fun onHideAllClick() = viewModelScope.launch {
        when (filterType) {
            ChartFilterType.ACTIVITY -> {
                getTypesCache().map { it.id }.let(typeIdsFiltered::addAll)
                typeIdsFiltered.add(UNTRACKED_ITEM_ID)
                chartFilterInteractor.setFilteredTypes(extra, typeIdsFiltered)
            }
            ChartFilterType.CATEGORY -> {
                getCategoriesCache().map { it.id }.let(categoryIdsFiltered::addAll)
                categoryIdsFiltered.add(UNTRACKED_ITEM_ID)
                categoryIdsFiltered.add(UNCATEGORIZED_ITEM_ID)
                chartFilterInteractor.setFilteredCategories(extra, categoryIdsFiltered)
            }
            ChartFilterType.RECORD_TAG -> {
                getTagsCache().map { it.id }.let(recordTagIdsFiltered::addAll)
                recordTagIdsFiltered.add(UNTRACKED_ITEM_ID)
                recordTagIdsFiltered.add(UNCATEGORIZED_ITEM_ID)
                chartFilterInteractor.setFilteredTags(extra, recordTagIdsFiltered)
            }
        }
        updateTypesViewData()
    }

    private suspend fun initializeChartFilterType() {
        filterType = chartFilterInteractor.getChartFilterType(extra)
        typeIdsFiltered = chartFilterInteractor.getFilteredTypes(extra).toMutableList()
        categoryIdsFiltered = chartFilterInteractor.getFilteredCategories(extra).toMutableList()
        recordTagIdsFiltered = chartFilterInteractor.getFilteredTags(extra).toMutableList()
    }

    private suspend fun getTypesCache(): List<RecordType> {
        return recordTypesCache ?: run {
            recordTypeInteractor.getAll().also { recordTypesCache = it }
        }
    }

    private suspend fun getCategoriesCache(): List<Category> {
        return categoriesCache ?: run {
            categoryInteractor.getAll().also { categoriesCache = it }
        }
    }

    private suspend fun getTagsCache(): List<RecordTag> {
        return recordTagsCache ?: run {
            recordTagInteractor.getAll().also { recordTagsCache = it }
        }
    }

    private fun updateFilterTypeViewData() {
        val data = loadFilterTypeViewData()
        filterTypeViewData.set(data)
    }

    private fun loadFilterTypeViewData(): List<ViewHolderType> {
        return chartFilterViewDataMapper.mapToFilterTypeViewData(filterType)
    }

    private fun updateTypesViewData() {
        when (filterType) {
            ChartFilterType.ACTIVITY -> updateRecordTypesViewData()
            ChartFilterType.CATEGORY -> updateCategoriesViewData()
            ChartFilterType.RECORD_TAG -> updateTagsViewData()
        }
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> loadRecordTypesViewData()
            ChartFilterType.CATEGORY -> loadCategoriesViewData()
            ChartFilterType.RECORD_TAG -> loadTagsViewData()
        }
    }

    private fun updateRecordTypesViewData() = viewModelScope.launch {
        val data = loadRecordTypesViewData()
        types.set(data)
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        return chartFilterViewDataInteractor.loadRecordTypesViewData(
            types = getTypesCache(),
            typeIdsFiltered = typeIdsFiltered,
        )
    }

    private fun updateCategoriesViewData() = viewModelScope.launch {
        val data = loadCategoriesViewData()
        types.set(data)
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        return chartFilterViewDataInteractor.loadCategoriesViewData(
            categories = getCategoriesCache(),
            categoryIdsFiltered = categoryIdsFiltered,
        )
    }

    private fun updateTagsViewData() = viewModelScope.launch {
        val data = loadTagsViewData()
        types.set(data)
    }

    private suspend fun loadTagsViewData(): List<ViewHolderType> {
        return chartFilterViewDataInteractor.loadTagsViewData(
            tags = getTagsCache(),
            types = getTypesCache(),
            recordTagsFiltered = recordTagIdsFiltered,
        )
    }
}
