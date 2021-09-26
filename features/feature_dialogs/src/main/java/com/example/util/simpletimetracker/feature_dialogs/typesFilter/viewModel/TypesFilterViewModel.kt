package com.example.util.simpletimetracker.feature_dialogs.typesFilter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.interactor.TypesFilterViewDataInteractor
import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class TypesFilterViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val typesFilterViewDataInteractor: TypesFilterViewDataInteractor
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
    private var recordTypeCategories: List<RecordTypeCategory> = emptyList()
    private var activityTags: List<Category> = emptyList()
    private var recordTags: List<RecordTag> = emptyList()

    fun onRecordTypeClick(item: RecordTypeViewData) {
        val currentFilter = typesFilter.value ?: return
        switchToActivityFilter(currentFilter, item)
    }

    fun onCategoryClick(item: CategoryViewData) {
        val currentFilter = typesFilter.value ?: return
        when (item) {
            is CategoryViewData.Activity -> switchToCategoryFilter(currentFilter, item)
            is CategoryViewData.Record -> updateRecordTagFilter(currentFilter, item)
        }
    }

    fun onShowAllClick() {
        (typesFilter as MutableLiveData).value = TypesFilterParams(
            filterType = ChartFilterType.ACTIVITY,
            selectedIds = types.map { it.id },
            filteredRecordTags = emptyList()
        )
        updateViewData()
    }

    fun onHideAllClick() {
        (typesFilter as MutableLiveData).value = TypesFilterParams(
            filterType = ChartFilterType.ACTIVITY,
            selectedIds = emptyList(),
            filteredRecordTags = emptyList()
        )
        updateViewData()
    }

    private fun switchToActivityFilter(
        currentFilter: TypesFilterParams,
        item: RecordTypeViewData
    ) {
        var newFilter = currentFilter

        if (currentFilter.filterType != ChartFilterType.ACTIVITY) {
            // Switch from tags to types in these tags
            val currentTypes = recordTypeCategories
                .filter { it.categoryId in currentFilter.selectedIds }
                .map { it.recordTypeId }
                .filter { it in types.map(RecordType::id) }
                .distinct()

            newFilter = TypesFilterParams(
                filterType = ChartFilterType.ACTIVITY,
                selectedIds = currentTypes,
                filteredRecordTags = currentFilter.filteredRecordTags
            )
        }

        updateItemInFilter(newFilter, item.id)
    }

    private fun switchToCategoryFilter(
        currentFilter: TypesFilterParams,
        item: CategoryViewData.Activity
    ) {
        var newFilter = currentFilter

        if (currentFilter.filterType != ChartFilterType.CATEGORY) {
            newFilter = TypesFilterParams(
                filterType = ChartFilterType.CATEGORY,
                selectedIds = emptyList(),
                filteredRecordTags = emptyList()
            )
        }

        updateItemInFilter(newFilter, item.id)
    }

    private fun updateRecordTagFilter(
        filter: TypesFilterParams,
        item: CategoryViewData.Record
    ) {
        val selectedRecordTags = filter.filteredRecordTags.toMutableList()
        when (item) {
            is CategoryViewData.Record.Tagged -> TypesFilterParams.FilteredRecordTag.Tagged(item.id)
            is CategoryViewData.Record.Untagged -> TypesFilterParams.FilteredRecordTag.Untagged(item.id)
        }.let { selectedRecordTags.addOrRemove(it) }
        val newFilter = filter.copy(filteredRecordTags = selectedRecordTags)
        typesFilter.set(newFilter)
        updateViewData()
    }

    private fun updateItemInFilter(
        filter: TypesFilterParams,
        id: Long
    ) {
        val selectedIds = filter.selectedIds.toMutableList()
        selectedIds.addOrRemove(id)
        val newFilter = filter.copy(selectedIds = selectedIds)
        typesFilter.set(newFilter)
        updateViewData()
    }

    private suspend fun initializeData() {
        types = recordTypeInteractor.getAll()
        activityTags = categoryInteractor.getAll()
        recordTags = recordTagInteractor.getAll()
        recordTypeCategories = recordTypeCategoryInteractor.getAll()
    }

    private fun updateViewData() = viewModelScope.launch {
        val data = loadViewData()
        viewData.set(data)
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        return typesFilterViewDataInteractor.getViewData(
            filter = typesFilter.value ?: return emptyList(),
            types = types,
            recordTypeCategories = recordTypeCategories,
            activityTags = activityTags,
            recordTags = recordTags
        )
    }
}
