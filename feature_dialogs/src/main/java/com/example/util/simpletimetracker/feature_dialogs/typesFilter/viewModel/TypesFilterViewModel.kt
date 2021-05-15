package com.example.util.simpletimetracker.feature_dialogs.typesFilter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.category.CategoryViewData
import com.example.util.simpletimetracker.core.adapter.divider.DividerViewData
import com.example.util.simpletimetracker.core.adapter.hint.HintViewData
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.TagType
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.navigation.params.TypesFilterParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class TypesFilterViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordInteractor: RecordInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val resourceRepo: ResourceRepo
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
        when (item.type) {
            TagType.RECORD_TYPE -> switchToCategoryFilter(currentFilter, item)
            TagType.RECORD -> updateRecordTagFilter(currentFilter, item.id)
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
        item: CategoryViewData
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
        id: Long
    ) {
        val selectedRecordTags = filter.filteredRecordTags.toMutableList()
        selectedRecordTags.addOrRemove(id)
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
        recordTypeCategories = recordTypeCategoryInteractor.getAll()

        val records = recordInteractor.getAll()
        val typesWithRecords = records
            .map(Record::typeId)
            .toSet()
        val activityTagsWithRecords = recordTypeCategories
            .filter { it.recordTypeId in typesWithRecords }
            .map(RecordTypeCategory::categoryId)
            .toSet()
        val recordTagsWithRecords = records
            .map(Record::tagId)
            .toSet()

        types = recordTypeInteractor.getAll()
            .filter { it.id in typesWithRecords }
        activityTags = categoryInteractor.getAll()
            .filter { it.id in activityTagsWithRecords }
        recordTags = recordTagInteractor.getAll()
            .filter { it.id in recordTagsWithRecords }
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
        val typesMap = types.map { it.id to it }.toMap()
        val selectedTypes = types.map(RecordType::id).filter { typeId ->
            when (filter.filterType) {
                ChartFilterType.ACTIVITY -> typeId in filter.selectedIds
                ChartFilterType.CATEGORY -> typeId in recordTypeCategories
                    .filter { it.categoryId in filter.selectedIds }
                    .map { it.recordTypeId }
            }
        }

        val typesViewData = types.map { type ->
            recordTypeViewDataMapper.mapFiltered(
                recordType = type,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
                isFiltered = type.id !in selectedTypes
            )
        }

        val activityTagsViewData = activityTags.map { tag ->
            val isFiltered = filter.filterType != ChartFilterType.CATEGORY ||
                tag.id !in filter.selectedIds

            categoryViewDataMapper.mapActivityTag(
                category = tag,
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered
            )
        }

        val recordTagsViewData = recordTags
            .filter { it.typeId in selectedTypes }
            .mapNotNull { tag ->
                categoryViewDataMapper.mapRecordTag(
                    tag = tag,
                    type = typesMap[tag.typeId] ?: return@mapNotNull null,
                    isDarkTheme = isDarkTheme,
                    isFiltered = tag.id in filter.filteredRecordTags
                )
            }

        if (activityTagsViewData.isNotEmpty()) {
            HintViewData(resourceRepo.getString(R.string.types_filter_activity_tag_hint)).let(result::add)
            activityTagsViewData.let(result::addAll)
        }

        if (typesViewData.isNotEmpty()) {
            if (activityTagsViewData.isNotEmpty()) {
                DividerViewData(1).let(result::add)
            }
            HintViewData(resourceRepo.getString(R.string.types_filter_activity_hint)).let(result::add)
            typesViewData.let(result::addAll)
        }

        if (recordTagsViewData.isNotEmpty()) {
            DividerViewData(2).let(result::add)
            HintViewData(resourceRepo.getString(R.string.types_filter_record_tag_hint)).let(result::add)
            recordTagsViewData.let(result::addAll)
        }

        return result
    }
}
