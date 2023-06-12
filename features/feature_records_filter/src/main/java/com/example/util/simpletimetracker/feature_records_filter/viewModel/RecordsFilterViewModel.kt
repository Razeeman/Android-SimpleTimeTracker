package com.example.util.simpletimetracker.feature_records_filter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toModel
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.MULTITASK_ITEM_ID
import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.getAllTypeIds
import com.example.util.simpletimetracker.domain.extension.getCategoryItems
import com.example.util.simpletimetracker.domain.extension.getCommentItems
import com.example.util.simpletimetracker.domain.extension.getDate
import com.example.util.simpletimetracker.domain.extension.getDaysOfWeek
import com.example.util.simpletimetracker.domain.extension.getDuration
import com.example.util.simpletimetracker.domain.extension.getFilteredTags
import com.example.util.simpletimetracker.domain.extension.getManuallyFilteredRecordIds
import com.example.util.simpletimetracker.domain.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.extension.getTimeOfDay
import com.example.util.simpletimetracker.domain.extension.getTypeIds
import com.example.util.simpletimetracker.domain.extension.getTypeIdsFromCategories
import com.example.util.simpletimetracker.domain.extension.hasManuallyFiltered
import com.example.util.simpletimetracker.domain.extension.hasMultitaskFilter
import com.example.util.simpletimetracker.domain.extension.hasUntrackedFilter
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.DayOfWeek
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
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterButtonViewData
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterDayOfWeekViewData
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterRangeViewData
import com.example.util.simpletimetracker.feature_records_filter.interactor.RecordsFilterViewDataInteractor
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterViewDataMapper
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectionState
import com.example.util.simpletimetracker.feature_records_filter.model.type
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParam
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
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
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
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
                initial.value = RecordsFilterSelectedRecordsViewData.Loading
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
    private val defaultDurationRange: Range by lazy { viewDataInteractor.getDefaultDurationRange() }
    private val defaultTimeOfDayRange: Range by lazy { viewDataInteractor.getDefaultTimeOfDayRange() }
    private var recordsLoadJob: Job? = null

    // Cache
    private var types: List<RecordType> = emptyList()
    private var recordTypeCategories: List<RecordTypeCategory>? = null
    private var categories: List<Category>? = null
    private var recordTags: List<RecordTag>? = null

    fun init(extra: RecordsFilterParams) {
        this.extra = extra

        recordsFilterViewDataMapper.mapInitialFilter(extra, filters)
            ?.let(RecordsFilterSelectionState::Visible)
            ?.let(this::filterSelectionState::set)
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
            is CategoryViewData.Category -> {
                handleCategoryClick(item.id)
            }
            is CategoryViewData.Record -> when (item.id) {
                UNTRACKED_ITEM_ID -> handleUntrackedClick()
                MULTITASK_ITEM_ID -> handleMultitaskClick()
                else -> handleTagClick(item)
            }
        }

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    fun onInnerFilterClick(item: RecordFilterViewData) {
        when (item.type) {
            RecordFilterViewData.Type.COMMENT -> {
                handleCommentFilterClick(item)
            }
            else -> {
                // Do nothing.
            }
        }

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    fun onCommentChange(text: String) {
        handleCommentChange(text)

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        when (tag) {
            TIME_STARTED_TAG, TIME_ENDED_TAG -> handleDateSet(timestamp, tag)
            TIME_OF_DAY_FROM_TAG, TIME_OF_DAY_TO_TAG -> handleTimeOfDaySet(timestamp, tag)
        }
    }

    fun onRangeTimeClick(fieldType: RecordsFilterRangeViewData.FieldType) {
        viewModelScope.launch {
            when (filterSelectionState.type) {
                RecordFilterViewData.Type.DATE -> handleDateFieldClick(fieldType)
                RecordFilterViewData.Type.DURATION -> handleDurationFieldClick(fieldType)
                RecordFilterViewData.Type.TIME_OF_DAY -> handleTimeOfDayFieldClick(fieldType)
                else -> return@launch
            }
        }
    }

    fun onDurationSet(duration: Long, tag: String?) {
        val durationInMillis = duration * 1000
        var (rangeStart, rangeEnd) = filters.getDuration() ?: defaultDurationRange

        when (tag) {
            DURATION_FROM_TAG -> {
                if (durationInMillis != rangeStart) {
                    rangeStart = durationInMillis
                    if (durationInMillis > rangeEnd) rangeEnd = durationInMillis
                }
            }
            DURATION_TO_TAG -> {
                if (durationInMillis != rangeEnd) {
                    rangeEnd = durationInMillis
                    if (durationInMillis < rangeStart) rangeStart = durationInMillis
                }
            }
        }

        filters.removeAll { it is RecordsFilter.Duration }
        filters.add(RecordsFilter.Duration(Range(rangeStart, rangeEnd)))

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    fun onRecordClick(
        item: RecordViewData,
        @Suppress("UNUSED_PARAMETER") sharedElements: Pair<Any, String>
    ) {
        if (item is RecordViewData.Untracked) return // TODO manually filter untracked records?

        handleRecordClick(item.getUniqueId())

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    fun onInnerFilterButtonClick(viewData: RecordsFilterButtonViewData) {
        when (viewData.type) {
            RecordsFilterButtonViewData.Type.INVERT_SELECTION -> {
                handleInvertSelection()
            }
        }

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    fun onDayOfWeekClick(viewData: RecordsFilterDayOfWeekViewData) {
        handleDayOfWeekClick(viewData.dayOfWeek)

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    private suspend fun handleTypeClick(id: Long) {
        val currentIds = filters.getTypeIds().toMutableList()
        val currentIdsFromCategories = filters.getTypeIdsFromCategories(
            recordTypes = getTypesCache(),
            recordTypeCategories = getRecordTypeCategoriesCache()
        )

        filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.ACTIVITY)

        // Switch from categories to types in these categories.
        if (currentIdsFromCategories.isNotEmpty()) {
            currentIds.addAll(currentIdsFromCategories)
        }

        val newIds = currentIds.toMutableList().apply { addOrRemove(id) }

        filters.removeAll { it is RecordsFilter.Activity }
        filters.removeAll { it is RecordsFilter.Category }
        filters.removeAll { it is RecordsFilter.Untracked }
        filters.removeAll { it is RecordsFilter.Multitask }
        if (newIds.isNotEmpty()) filters.add(RecordsFilter.Activity(newIds))
    }

    private suspend fun handleCategoryClick(id: Long) {
        val currentItems = filters.getCategoryItems()

        filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.CATEGORY)

        val newItems = if (id == UNCATEGORIZED_ITEM_ID) {
            RecordsFilter.CategoryItem.Uncategorized
        } else {
            RecordsFilter.CategoryItem.Categorized(id)
        }.let { currentItems.toMutableList().apply { addOrRemove(it) } }

        filters.removeAll { it is RecordsFilter.Activity }
        filters.removeAll { it is RecordsFilter.Category }
        filters.removeAll { it is RecordsFilter.Untracked }
        filters.removeAll { it is RecordsFilter.Multitask }
        if (newItems.isNotEmpty()) filters.add(RecordsFilter.Category(newItems))

        checkTagFilterConsistency()
    }

    private fun handleUntrackedClick() {
        val hasUntrackedFilter = filters.hasUntrackedFilter()

        filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.UNTRACKED)

        if (!hasUntrackedFilter) {
            val filtersAvailableWithUntrackedFilter = listOf(
                RecordsFilter.Date::class.java,
                RecordsFilter.DaysOfWeek::class.java,
                RecordsFilter.TimeOfDay::class.java,
                RecordsFilter.Duration::class.java,
            )
            filters.removeAll {
                it::class.java !in filtersAvailableWithUntrackedFilter
            }

            filters.add(RecordsFilter.Untracked)
        } else {
            filters.removeAll { it is RecordsFilter.Untracked }
        }
    }

    private fun handleMultitaskClick() {
        val hasMultitaskFilter = filters.hasMultitaskFilter()

        filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.MULTITASK)

        if (!hasMultitaskFilter) {
            val filtersAvailableWithMultitaskFilter = listOf(
                RecordsFilter.Date::class.java,
                RecordsFilter.DaysOfWeek::class.java,
                RecordsFilter.TimeOfDay::class.java,
                RecordsFilter.Duration::class.java,
            )
            filters.removeAll {
                it::class.java !in filtersAvailableWithMultitaskFilter
            }

            filters.add(RecordsFilter.Multitask)
        } else {
            filters.removeAll { it is RecordsFilter.Multitask }
        }
    }

    private fun handleCommentFilterClick(item: RecordFilterViewData) {
        val currentItems = filters.getCommentItems()

        val clickedItem = when (item.id) {
            RecordFilterViewData.CommentType.NO_COMMENT.ordinal.toLong() -> {
                RecordsFilter.CommentItem.NoComment
            }
            RecordFilterViewData.CommentType.ANY_COMMENT.ordinal.toLong() -> {
                RecordsFilter.CommentItem.AnyComment
            }
            else -> return
        }
        val newItems = currentItems.toMutableList().apply {
            if (clickedItem !in this) clear()
            addOrRemove(clickedItem)
        }

        filters.removeAll { it is RecordsFilter.Comment }
        if (newItems.isNotEmpty()) filters.add(RecordsFilter.Comment(newItems))
    }

    private fun handleCommentChange(text: String) {
        filters.removeAll { it is RecordsFilter.Comment }
        if (text.isNotEmpty()) {
            val newItems = RecordsFilter.CommentItem.Comment(text).let(::listOf)
            filters.add(RecordsFilter.Comment(newItems))
        }
    }

    private fun handleTagClick(item: CategoryViewData.Record) {
        val currentState = filterSelectionState.type ?: return

        val currentTags = when (currentState) {
            RecordFilterViewData.Type.SELECTED_TAGS -> filters.getSelectedTags()
            RecordFilterViewData.Type.FILTERED_TAGS -> filters.getFilteredTags()
            else -> return
        }

        val newTags = when (item) {
            is CategoryViewData.Record.Tagged -> RecordsFilter.TagItem.Tagged(item.id)
            is CategoryViewData.Record.Untagged -> RecordsFilter.TagItem.Untagged
        }.let { currentTags.toMutableList().apply { addOrRemove(it) } }

        filters.removeAll { it is RecordsFilter.Untracked }
        filters.removeAll { it is RecordsFilter.Multitask }
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
        checkManualFilterVisibility()
    }

    private suspend fun checkTagFilterConsistency() {
        // Update tags according to selected activities
        val newTypeIds: List<Long> = filters.getAllTypeIds(
            recordTypes = getTypesCache(),
            recordTypeCategories = getRecordTypeCategoriesCache()
        )

        suspend fun update(tags: List<RecordsFilter.TagItem>): List<RecordsFilter.TagItem> {
            return tags.filter {
                when (it) {
                    is RecordsFilter.TagItem.Tagged -> {
                        it.tagId in getTagsCache()
                            .filter { tag -> tag.typeId in newTypeIds || tag.typeId == 0L }
                            .map { tag -> tag.id }
                    }
                    is RecordsFilter.TagItem.Untagged -> {
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
        checkManualFilterVisibility()

        updateFilters()
        updateFilterSelectionViewData()
        updateRecords()
    }

    private fun handleInvertSelection() {
        val filteredIds = filters.getManuallyFilteredRecordIds()
            .toMutableList()
        val selectedIds = recordsViewData.value
            ?.recordsViewData
            .orEmpty()
            .filterIsInstance<RecordViewData.Tracked>()
            .filter { it.id !in filteredIds }
            .map { it.id }

        filters.removeAll { it is RecordsFilter.ManuallyFiltered }
        if (selectedIds.isNotEmpty()) filters.add(RecordsFilter.ManuallyFiltered(selectedIds))
        checkManualFilterVisibility()
    }

    private fun handleDayOfWeekClick(dayOfWeek: DayOfWeek) {
        val newDays = filters.getDaysOfWeek()
            .toMutableList()
            .apply { addOrRemove(dayOfWeek) }

        filters.removeAll { it is RecordsFilter.DaysOfWeek }
        if (newDays.isNotEmpty()) filters.add(RecordsFilter.DaysOfWeek(newDays))
    }

    private fun checkManualFilterVisibility() {
        if (
            !filters.hasManuallyFiltered() &&
            filterSelectionState.type == RecordFilterViewData.Type.MANUALLY_FILTERED
        ) {
            filterSelectionState = RecordsFilterSelectionState.Hidden
            updateFilterSelectionVisibility()
        }
    }

    private suspend fun handleDateFieldClick(fieldType: RecordsFilterRangeViewData.FieldType) {
        val range = filters.getDate() ?: defaultRange

        when (fieldType) {
            RecordsFilterRangeViewData.FieldType.TIME_STARTED -> {
                viewDataInteractor.getDateTimeDialogParams(
                    tag = TIME_STARTED_TAG,
                    timestamp = range.timeStarted,
                )
            }
            RecordsFilterRangeViewData.FieldType.TIME_ENDED -> {
                viewDataInteractor.getDateTimeDialogParams(
                    tag = TIME_ENDED_TAG,
                    timestamp = range.timeEnded,
                )
            }
        }.let(router::navigate)
    }

    private fun handleDurationFieldClick(fieldType: RecordsFilterRangeViewData.FieldType) {
        val range = filters.getDuration() ?: defaultDurationRange

        when (fieldType) {
            RecordsFilterRangeViewData.FieldType.TIME_STARTED -> DurationDialogParams(
                tag = DURATION_FROM_TAG,
                duration = range.timeStarted / 1000,
                hideDisableButton = true,
            )
            RecordsFilterRangeViewData.FieldType.TIME_ENDED -> DurationDialogParams(
                tag = DURATION_TO_TAG,
                duration = range.timeEnded / 1000,
                hideDisableButton = true,
            )
        }.let(router::navigate)
    }

    private suspend fun handleTimeOfDayFieldClick(fieldType: RecordsFilterRangeViewData.FieldType) {
        val range = filters.getTimeOfDay() ?: defaultTimeOfDayRange
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val startOfDay = timeMapper.getStartOfDayTimeStamp()

        when (fieldType) {
            RecordsFilterRangeViewData.FieldType.TIME_STARTED -> DateTimeDialogParams(
                tag = TIME_OF_DAY_FROM_TAG,
                type = DateTimeDialogType.TIME,
                timestamp = startOfDay + range.timeStarted,
                useMilitaryTime = useMilitaryTime,
            )
            RecordsFilterRangeViewData.FieldType.TIME_ENDED -> DateTimeDialogParams(
                tag = TIME_OF_DAY_TO_TAG,
                type = DateTimeDialogType.TIME,
                timestamp = startOfDay + range.timeEnded,
                useMilitaryTime = useMilitaryTime,
            )
        }.let(router::navigate)
    }

    private fun handleDateSet(timestamp: Long, tag: String?) {
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

    private fun handleTimeOfDaySet(timestamp: Long, tag: String?) {
        var (rangeStart, rangeEnd) = filters.getTimeOfDay() ?: defaultTimeOfDayRange
        val startOfDay = timeMapper.getStartOfDayTimeStamp()
        val normalizedTimeStamp = (timestamp - startOfDay)
            .coerceIn(0..TimeUnit.DAYS.toMillis(1))

        when (tag) {
            TIME_OF_DAY_FROM_TAG -> rangeStart = normalizedTimeStamp
            TIME_OF_DAY_TO_TAG -> rangeEnd = normalizedTimeStamp
        }

        filters.removeAll { it is RecordsFilter.TimeOfDay }
        filters.add(RecordsFilter.TimeOfDay(Range(rangeStart, rangeEnd)))

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
        changedFilters.set(
            RecordsFilterResultParams(
                tag = extra.tag,
                filters = filters,
            )
        )
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
            recordsViewData.set(RecordsFilterSelectedRecordsViewData.Loading)
            val data = loadRecordsViewData()
            recordsViewData.set(data)
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
        val type = filterSelectionState.type ?: return emptyList()

        return when (type) {
            RecordFilterViewData.Type.UNTRACKED,
            RecordFilterViewData.Type.MULTITASK,
            RecordFilterViewData.Type.ACTIVITY,
            RecordFilterViewData.Type.CATEGORY -> {
                viewDataInteractor.getActivityFilterSelectionViewData(
                    extra = extra,
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
            RecordFilterViewData.Type.DAYS_OF_WEEK -> {
                viewDataInteractor.getDaysOfWeekFilterSelectionViewData(
                    filters = filters,
                )
            }
            RecordFilterViewData.Type.TIME_OF_DAY -> {
                viewDataInteractor.getTimeOfDayFilterSelectionViewData(
                    filters = filters,
                    defaultRange = defaultTimeOfDayRange,
                )
            }
            RecordFilterViewData.Type.DURATION -> {
                viewDataInteractor.getDurationFilterSelectionViewData(
                    filters = filters,
                    defaultRange = defaultDurationRange,
                )
            }
        }
    }

    companion object {
        private const val TIME_STARTED_TAG = "records_filter_range_selection_time_started_tag"
        private const val TIME_ENDED_TAG = "records_filter_range_selection_time_ended_tag"
        private const val DURATION_FROM_TAG = "records_filter_duration_selection_from_tag"
        private const val DURATION_TO_TAG = "records_filter_duration_selection_to_tag"
        private const val TIME_OF_DAY_FROM_TAG = "records_filter_time_of_day_selection_from_tag"
        private const val TIME_OF_DAY_TO_TAG = "records_filter_time_of_day_selection_to_tag"
    }
}
