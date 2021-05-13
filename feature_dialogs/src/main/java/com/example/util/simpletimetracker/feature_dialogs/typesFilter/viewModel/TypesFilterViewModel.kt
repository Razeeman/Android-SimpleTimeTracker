package com.example.util.simpletimetracker.feature_dialogs.typesFilter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.category.CategoryViewData
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
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
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.adapter.TypesFilterDividerViewData
import com.example.util.simpletimetracker.navigation.params.TypesFilterParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class TypesFilterViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordInteractor: RecordInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper
) : ViewModel() {

    lateinit var extra: TypesFilterParams

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initializeData()
                initial.value = loadViewData()
            }
            initial
        }
    }
    val typesFilter: LiveData<TypesFilterParams> by lazy {
        MutableLiveData(extra)
    }

    private var types: List<RecordType> = emptyList()
    private var activityTags: List<Category> = emptyList()

    fun onRecordTypeClick(item: RecordTypeViewData) {
        var currentFilter = typesFilter.value ?: return

        if (currentFilter.filterType != ChartFilterType.ACTIVITY) {
            currentFilter = TypesFilterParams(
                selectedIds = emptyList(),
                filterType = ChartFilterType.ACTIVITY
            )
        }

        updateItemInFilter(currentFilter, item.id)
    }

    fun onCategoryClick(item: CategoryViewData) {
        var currentFilter = typesFilter.value ?: return

        if (currentFilter.filterType != ChartFilterType.CATEGORY) {
            currentFilter = TypesFilterParams(
                selectedIds = emptyList(),
                filterType = ChartFilterType.CATEGORY
            )
        }

        updateItemInFilter(currentFilter, item.id)
    }

    private fun updateItemInFilter(filter: TypesFilterParams, id: Long) {
        val selectedIds = filter.selectedIds.toMutableList()
        selectedIds.addOrRemove(id)
        val newFilter = filter.copy(selectedIds = selectedIds)
        typesFilter.set(newFilter)
        updateViewData()
    }

    fun onShowAllClick() {
        (typesFilter as MutableLiveData).value = TypesFilterParams(types.map { it.id })
        updateViewData()
    }

    fun onHideAllClick() {
        (typesFilter as MutableLiveData).value = TypesFilterParams()
        updateViewData()
    }

    private suspend fun initializeData() {
        val typesWithRecords = recordInteractor.getAll()
            .map(Record::typeId)
            .toSet()
        val activityTagsWithRecords = recordTypeCategoryInteractor.getAll()
            .filter { it.recordTypeId in typesWithRecords }
            .map(RecordTypeCategory::categoryId)
            .toSet()

        types = recordTypeInteractor.getAll()
            .filter { it.id in typesWithRecords }
        activityTags = categoryInteractor.getAll()
            .filter { it.id in activityTagsWithRecords }
    }

    private fun updateViewData() = viewModelScope.launch {
        val data = loadViewData()
        viewData.set(data)
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        val result: MutableList<ViewHolderType> = mutableListOf()
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val filter = typesFilter.value ?: return emptyList()

        val typesViewData = types.map { type ->
            val isFiltered = filter.filterType != ChartFilterType.ACTIVITY ||
                type.id !in filter.selectedIds

            recordTypeViewDataMapper.mapFiltered(
                recordType = type,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered
            )
        }

        val activityTagsViewData = activityTags.map { tag ->
            val isFiltered = filter.filterType != ChartFilterType.CATEGORY ||
                tag.id !in filter.selectedIds

            categoryViewDataMapper.mapFiltered(
                category = tag,
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered
            )
        }

        activityTagsViewData.let(result::addAll)
        TypesFilterDividerViewData(1).let(result::add)
        typesViewData.let(result::addAll)

        return result
    }
}
