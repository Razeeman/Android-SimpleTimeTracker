package com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.post
import com.example.util.simpletimetracker.core.utils.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.mapper.ChartFilterViewDataMapper
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewData.ChartFilterTypeViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChartFilterViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val chartFilterViewDataMapper: ChartFilterViewDataMapper
) : ViewModel() {

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
    private var recordTypes: List<RecordType> = emptyList()
    private var categories: List<Category> = emptyList()
    private var typeIdsFiltered: MutableList<Long> = mutableListOf()
    private var categoryIdsFiltered: MutableList<Long> = mutableListOf()

    fun onFilterTypeClick(viewData: ButtonsRowViewData) {
        if (viewData !is ChartFilterTypeViewData) return
        viewModelScope.launch {
            filterType = viewData.filterType
            prefsInteractor.setChartFilterType(filterType)
            updateFilterTypeViewData()
            updateTypesViewData()
        }
    }

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            typeIdsFiltered.addOrRemove(item.id)
            prefsInteractor.setFilteredTypes(typeIdsFiltered)
            updateRecordTypesViewData()
        }
    }

    fun onCategoryClick(item: CategoryViewData) {
        viewModelScope.launch {
            categoryIdsFiltered.addOrRemove(item.id)
            prefsInteractor.setFilteredCategories(categoryIdsFiltered)
            updateCategoriesViewData()
        }
    }

    fun onShowAllClick() {
        viewModelScope.launch {
            when (filterType) {
                ChartFilterType.ACTIVITY -> {
                    typeIdsFiltered.clear()
                    prefsInteractor.setFilteredTypes(typeIdsFiltered)
                }
                ChartFilterType.CATEGORY -> {
                    categoryIdsFiltered.clear()
                    prefsInteractor.setFilteredCategories(categoryIdsFiltered)
                }
            }
            updateTypesViewData()
        }
    }

    fun onHideAllClick() {
        viewModelScope.launch {
            when (filterType) {
                ChartFilterType.ACTIVITY -> {
                    recordTypes.map { it.id }.let(typeIdsFiltered::addAll)
                    typeIdsFiltered.add(UNTRACKED_ITEM_ID)
                    prefsInteractor.setFilteredTypes(typeIdsFiltered)
                }
                ChartFilterType.CATEGORY -> {
                    categories.map { it.id }.let(categoryIdsFiltered::addAll)
                    prefsInteractor.setFilteredCategories(categoryIdsFiltered)
                }
            }
            updateTypesViewData()
        }
    }

    private suspend fun initializeChartFilterType() {
        filterType = prefsInteractor.getChartFilterType()
    }

    private fun updateFilterTypeViewData() {
        val data = loadFilterTypeViewData()
        filterTypeViewData.post(data)
    }

    private fun loadFilterTypeViewData(): List<ViewHolderType> {
        return chartFilterViewDataMapper.mapToFilterTypeViewData(filterType)
    }

    private fun updateTypesViewData() {
        when (filterType) {
            ChartFilterType.ACTIVITY -> updateRecordTypesViewData()
            ChartFilterType.CATEGORY -> updateCategoriesViewData()
        }
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> loadRecordTypesViewData()
            ChartFilterType.CATEGORY -> loadCategoriesViewData()
        }
    }

    private fun updateRecordTypesViewData() = viewModelScope.launch {
        val data = loadRecordTypesViewData()
        types.post(data)
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        if (recordTypes.isEmpty()) recordTypes = loadRecordTypes()

        return recordTypes
            .map { type ->
                chartFilterViewDataMapper
                    .mapRecordType(type, typeIdsFiltered, numberOfCards, isDarkTheme)
            }
            .plus(
                chartFilterViewDataMapper
                    .mapToUntrackedItem(typeIdsFiltered, numberOfCards, isDarkTheme)
            )
    }

    private suspend fun loadRecordTypes(): List<RecordType> {
        typeIdsFiltered = prefsInteractor.getFilteredTypes()
            .toMutableList()

        return recordTypeInteractor.getAll()
    }

    private fun updateCategoriesViewData() = viewModelScope.launch {
        val data = loadCategoriesViewData()
        types.post(data)
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        if (categories.isEmpty()) categories = loadCategories()

        return categories
            .map { type ->
                chartFilterViewDataMapper
                    .mapCategory(type, categoryIdsFiltered, isDarkTheme)
            }
            .takeUnless { it.isEmpty() }
            ?: chartFilterViewDataMapper.mapCategoriesEmpty()
    }

    private suspend fun loadCategories(): List<Category> {
        categoryIdsFiltered = prefsInteractor.getFilteredCategories()
            .toMutableList()

        return categoryInteractor.getAll()
    }
}
