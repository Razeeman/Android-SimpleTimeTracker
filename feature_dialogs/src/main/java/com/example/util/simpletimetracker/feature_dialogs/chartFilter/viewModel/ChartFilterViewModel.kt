package com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.extension.post
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.core.adapter.category.CategoryViewData
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.mapper.ChartFilterViewDataMapper
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewData.ChartFilterTypeViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChartFilterViewModel @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
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
            if (item.id in typeIdsFiltered) {
                typeIdsFiltered.remove(item.id)
            } else {
                typeIdsFiltered.add(item.id)
            }
            prefsInteractor.setFilteredTypes(typeIdsFiltered)
            updateRecordTypesViewData()
        }
    }

    fun onCategoryClick(item: CategoryViewData) {
        viewModelScope.launch {
            if (item.id in categoryIdsFiltered) {
                categoryIdsFiltered.remove(item.id)
            } else {
                categoryIdsFiltered.add(item.id)
            }
            prefsInteractor.setFilteredCategories(categoryIdsFiltered)
            updateCategoriesViewData()
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
            .apply {
                this as MutableList
                chartFilterViewDataMapper
                    .mapToUntrackedItem(typeIdsFiltered, numberOfCards, isDarkTheme)
                    .let(::add)
            }
    }

    private suspend fun loadRecordTypes(): List<RecordType> {
        val typesInStatistics = recordInteractor.getAll()
            .map(Record::typeId)
            .toSet()
        typeIdsFiltered = prefsInteractor.getFilteredTypes()
            .toMutableList()

        return recordTypeInteractor.getAll()
            .filter { it.id in typesInStatistics }
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
        val typesInStatistics = recordInteractor.getAll()
            .map(Record::typeId)
            .toSet()
        val categoriesInStatistics = recordTypeCategoryInteractor.getAll()
            .filter { it.recordTypeId in typesInStatistics }
            .map(RecordTypeCategory::categoryId)
            .toSet()
        categoryIdsFiltered = prefsInteractor.getFilteredCategories()
            .toMutableList()

        return categoryInteractor.getAll()
            .filter { it.id in categoriesInStatistics }
    }
}
