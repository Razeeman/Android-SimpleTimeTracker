package com.example.util.simpletimetracker.feature_records_filter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toModel
import com.example.util.simpletimetracker.domain.extension.getAllTypeIds
import com.example.util.simpletimetracker.domain.extension.getCategoryIds
import com.example.util.simpletimetracker.domain.extension.getDate
import com.example.util.simpletimetracker.domain.extension.getFilteredTags
import com.example.util.simpletimetracker.domain.extension.getManuallyFilteredRecordIds
import com.example.util.simpletimetracker.domain.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.extension.getTypeIds
import com.example.util.simpletimetracker.domain.extension.getTypeIdsFromCategories
import com.example.util.simpletimetracker.domain.extension.hasCategoryFilter
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.Range
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
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParam
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class RecordsFilterViewModel @Inject constructor(
    private val viewDataInteractor: RecordsFilterViewDataInteractor,
    private val recordsFilterViewDataMapper: RecordsFilterViewDataMapper,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val router: Router,
) : ViewModel() {

    private lateinit var extra: RecordsFilterParams

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
                    selectedRecordsCount = recordsFilterViewDataMapper.mapRecordsCount(
                        extra = extra,
                        count = 0,
                        filter = emptyList(),
                    ),
                    recordsViewData = listOf(LoaderViewData()),
                    filteredRecordsTypeId = null,
                )
                initial.value = loadRecordsViewData()
            }
            initial
        }
    }
    val filterSelectionVisibility: LiveData<Boolean> by lazy {
        MutableLiveData(loadFilterSelectionVisibility())
    }
    val changedFilters: LiveData<RecordsFilterResultParams> = MutableLiveData()
    val keyboardVisibility: LiveData<Boolean> = MutableLiveData(false)

    private val filters: MutableList<RecordsFilter> by lazy {
        extra.filters.map(RecordsFilterParam::toModel).toMutableList()
    }
    private var filterSelectionState: RecordsFilterSelectionState = RecordsFilterSelectionState.Hidden
    private val defaultRange: Range by lazy { viewDataInteractor.getDefaultDateRange() }
    private var recordsLoadJob: Job? = null

    // Cache
    private var types: List<RecordType> = emptyList()
    private var recordTypeCategories: List<RecordTypeCategory>? = null
    private var categories: List<Category>? = null
    private var recordTags: List<RecordTag>? = null

    fun init(extra: RecordsFilterParams) {
        this.extra = extra
        filterSelectionState = RecordsFilterSelectionState.Visible(
            if (filters.hasCategoryFilter()) {
                RecordFilterViewData.Type.CATEGORY
            } else {
                RecordFilterViewData.Type.ACTIVITY
            }
        )
    }

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

    fun onRecordTypeClick(item: RecordTypeViewData) = viewModelScope.launch {
        handleTypeClick(item.id)

        checkTagFilterConsistency()
        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    fun onCategoryClick(item: CategoryViewData) = viewModelScope.launch {
        when (item) {
            is CategoryViewData.Category -> handleCategoryClick(item.id)
            is CategoryViewData.Record -> handleTagClick(item)
        }

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    fun onCommentChange(text: String) {
        filters.removeAll { it is RecordsFilter.Comment }
        if (text.isNotEmpty()) filters.add(RecordsFilter.Comment(text))

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    fun onRangeTimeStartedClick() {
        viewModelScope.launch {
            val range = filters.getDate() ?: defaultRange

            viewDataInteractor.getDateTimeDialogParams(
                tag = TIME_STARTED_TAG,
                timestamp = range.timeStarted,
            ).let(router::navigate)
        }
    }

    fun onRangeTimeEndedClick() {
        viewModelScope.launch {
            val range = filters.getDate() ?: defaultRange

            viewDataInteractor.getDateTimeDialogParams(
                tag = TIME_ENDED_TAG,
                timestamp = range.timeEnded,
            ).let(router::navigate)
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        var (rangeStart, rangeEnd) = filters.getDate() ?: defaultRange

        when (tag) {
            TIME_STARTED_TAG -> {
                if (timestamp != rangeStart) {
                    rangeStart = timestamp
                    if (timestamp > rangeEnd) rangeEnd = timestamp
                }
            }
            TIME_ENDED_TAG -> {
                if (timestamp != rangeEnd) {
                    rangeEnd = timestamp
                    if (timestamp < rangeStart) rangeStart = timestamp
                }
            }
        }

        filters.removeAll { it is RecordsFilter.Date }
        filters.add(RecordsFilter.Date(Range(rangeStart, rangeEnd)))

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    fun onRecordClick(
        item: RecordViewData,
        @Suppress("UNUSED_PARAMETER") sharedElements: Pair<Any, String>
    ) {
        handleRecordClick(item.getUniqueId())

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    private suspend fun handleTypeClick(id: Long) {
        val currentIds = filters.getTypeIds().toMutableList()
        val currentIdsFromCategories = filters.getTypeIdsFromCategories(getRecordTypeCategoriesCache())

        // Switch from categories to types in these categories.
        if (currentIdsFromCategories.isNotEmpty()) {
            filters.removeAll { it is RecordsFilter.Category }
            currentIds.addAll(currentIdsFromCategories)
            filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.ACTIVITY)
        }

        val newIds = currentIds.toMutableList().apply { addOrRemove(id) }

        filters.removeAll { it is RecordsFilter.Activity }
        if (newIds.isNotEmpty()) filters.add(RecordsFilter.Activity(newIds))
    }

    private suspend fun handleCategoryClick(id: Long) {
        val currentIds = filters.getCategoryIds().toMutableList()

        filters.removeAll { it is RecordsFilter.Activity }
        filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.CATEGORY)

        val newIds = currentIds.toMutableList().apply { addOrRemove(id) }

        filters.removeAll { it is RecordsFilter.Category }
        if (newIds.isNotEmpty()) filters.add(RecordsFilter.Category(newIds))

        checkTagFilterConsistency()
    }

    private fun handleTagClick(item: CategoryViewData.Record) {
        val currentState = (filterSelectionState as? RecordsFilterSelectionState.Visible)
            ?.type ?: return

        val currentTags = when (currentState) {
            RecordFilterViewData.Type.SELECTED_TAGS -> filters.getSelectedTags()
            RecordFilterViewData.Type.FILTERED_TAGS -> filters.getFilteredTags()
            else -> return
        }

        val newTags = when (item) {
            is CategoryViewData.Record.Tagged -> RecordsFilter.Tag.Tagged(item.id)
            is CategoryViewData.Record.Untagged -> RecordsFilter.Tag.Untagged
        }.let { currentTags.toMutableList().apply { addOrRemove(it) } }

        when (currentState) {
            RecordFilterViewData.Type.SELECTED_TAGS -> {
                filters.removeAll { it is RecordsFilter.SelectedTags }
                if (newTags.isNotEmpty()) filters.add(RecordsFilter.SelectedTags(newTags))
            }
            RecordFilterViewData.Type.FILTERED_TAGS -> {
                filters.removeAll { it is RecordsFilter.FilteredTags }
                if (newTags.isNotEmpty()) filters.add(RecordsFilter.FilteredTags(newTags))
            }
            else -> return
        }
    }

    private fun handleRecordClick(id: Long) {
        val newIds = filters.getManuallyFilteredRecordIds()
            .toMutableList()
            .apply { addOrRemove(id) }
        filters.removeAll { it is RecordsFilter.ManuallyFiltered }
        if (newIds.isNotEmpty()) filters.add(RecordsFilter.ManuallyFiltered(newIds))
    }

    private suspend fun checkTagFilterConsistency() {
        // Update tags according to selected activities
        val newTypeIds: List<Long> = filters.getAllTypeIds(getRecordTypeCategoriesCache())

        suspend fun update(tags: List<RecordsFilter.Tag>): List<RecordsFilter.Tag> {
            return tags.filter {
                when (it) {
                    is RecordsFilter.Tag.Tagged -> {
                        it.tagId in getTagsCache()
                            .filter { tag -> tag.typeId in newTypeIds || tag.typeId == 0L }
                            .map { tag -> tag.id }
                    }
                    is RecordsFilter.Tag.Untagged -> {
                        true
                    }
                }
            }
        }

        val newSelectedTags = update(filters.getSelectedTags())

        filters.removeAll { filter -> filter is RecordsFilter.SelectedTags }
        if (newSelectedTags.isNotEmpty()) filters.add(RecordsFilter.SelectedTags(newSelectedTags))

        val newFilteredTags = update(filters.getFilteredTags())

        filters.removeAll { filter -> filter is RecordsFilter.FilteredTags }
        if (newFilteredTags.isNotEmpty()) filters.add(RecordsFilter.FilteredTags(newFilteredTags))
    }

    private fun removeFilter(type: RecordFilterViewData.Type) {
        val filterClass = recordsFilterViewDataMapper.mapToClass(type)
        filters.removeAll { filterClass.isInstance(it) }

        // Switch back to activity if category removed.
        val currentSelectionType = (filterSelectionState as? RecordsFilterSelectionState.Visible)?.type
        if (currentSelectionType == RecordFilterViewData.Type.CATEGORY) {
            filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.ACTIVITY)
        }

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    private suspend fun getTypesCache(): List<RecordType> {
        return types.takeUnless { it.isEmpty() }
            ?: run { recordTypeInteractor.getAll().also { types = it } }
    }

    private suspend fun getCategoriesCache(): List<Category> {
        return categories ?: run {
            categoryInteractor.getAll().also { categories = it }
        }
    }

    private suspend fun getRecordTypeCategoriesCache(): List<RecordTypeCategory> {
        return recordTypeCategories ?: run {
            recordTypeCategoryInteractor.getAll().also { recordTypeCategories = it }
        }
    }

    private suspend fun getTagsCache(): List<RecordTag> {
        return recordTags ?: run {
            recordTagInteractor.getAll().also { recordTags = it }
        }
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
        return viewDataInteractor.getFiltersViewData(
            extra = extra,
            selectionState = filterSelectionState,
            filters = filters
        )
    }

    private fun updateRecords() {
        recordsLoadJob?.cancel()
        recordsLoadJob = viewModelScope.launch {
            recordsViewData.set(
                RecordsFilterSelectedRecordsViewData(
                    selectedRecordsCount = recordsViewData.value?.selectedRecordsCount.orEmpty(),
                    recordsViewData = listOf(LoaderViewData()),
                    filteredRecordsTypeId = recordsViewData.value?.filteredRecordsTypeId,
                )
            )
            val data = loadRecordsViewData()
            recordsViewData.set(data)
            changedFilters.set(
                RecordsFilterResultParams(
                    tag = extra.tag,
                    filters = filters,
                    filteredRecordsTypeId = data.filteredRecordsTypeId
                )
            )
        }
    }

    private suspend fun loadRecordsViewData(): RecordsFilterSelectedRecordsViewData {
        return viewDataInteractor.getRecordsViewData(
            extra = extra,
            filters = filters,
            recordTypes = getTypesCache().associateBy(RecordType::id),
            recordTags = getTagsCache(),
        )
    }

    private fun updateFilterSelectionViewData() = viewModelScope.launch {
        val data = loadFilterSelectionViewData()
        filterSelectionContent.set(data)
    }

    private suspend fun loadFilterSelectionViewData(): List<ViewHolderType> {
        val type = (filterSelectionState as? RecordsFilterSelectionState.Visible)
            ?.type ?: return emptyList()

        return when (type) {
            RecordFilterViewData.Type.ACTIVITY,
            RecordFilterViewData.Type.CATEGORY -> {
                viewDataInteractor.getActivityFilterSelectionViewData(
                    filters = filters,
                    types = getTypesCache(),
                    recordTypeCategories = getRecordTypeCategoriesCache(),
                    categories = getCategoriesCache(),
                )
            }
            RecordFilterViewData.Type.COMMENT -> {
                viewDataInteractor.getCommentFilterSelectionViewData(
                    filters = filters,
                )
            }
            RecordFilterViewData.Type.SELECTED_TAGS,
            RecordFilterViewData.Type.FILTERED_TAGS -> {
                viewDataInteractor.getTagsFilterSelectionViewData(
                    type = type,
                    filters = filters,
                    types = getTypesCache(),
                    recordTypeCategories = getRecordTypeCategoriesCache(),
                    recordTags = getTagsCache(),
                )
            }
            RecordFilterViewData.Type.DATE -> {
                viewDataInteractor.getDateFilterSelectionViewData(
                    filters = filters,
                    defaultRange = defaultRange,
                )
            }
            RecordFilterViewData.Type.MANUALLY_FILTERED -> {
                viewDataInteractor.getManualFilterSelectionViewData(
                    filters = filters,
                    recordTypes = getTypesCache().associateBy(RecordType::id),
                    recordTags = getTagsCache(),
                )
            }
        }
    }

    companion object {
        private const val TIME_STARTED_TAG = "records_filter_range_selection_time_started_tag"
        private const val TIME_ENDED_TAG = "records_filter_range_selection_time_ended_tag"
    }
}
