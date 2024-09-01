package com.example.util.simpletimetracker.feature_records_filter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toModel
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.MULTITASK_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.getDate
import com.example.util.simpletimetracker.domain.extension.getDuration
import com.example.util.simpletimetracker.domain.extension.getTimeOfDay
import com.example.util.simpletimetracker.domain.extension.hasManuallyFiltered
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordTypeToTag
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.RecordFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_base_adapter.selectionButton.SelectionButtonViewData
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterButtonViewData
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterRangeViewData
import com.example.util.simpletimetracker.feature_records_filter.interactor.RecordsFilterUpdateInteractor
import com.example.util.simpletimetracker.feature_records_filter.interactor.RecordsFilterViewDataInteractor
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterViewDataMapper
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectionState
import com.example.util.simpletimetracker.feature_records_filter.model.type
import com.example.util.simpletimetracker.feature_records_filter.viewData.RecordsFilterSelectionButtonType
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParam
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class RecordsFilterViewModel @Inject constructor(
    private val viewDataInteractor: RecordsFilterViewDataInteractor,
    private val recordsFilterViewDataMapper: RecordsFilterViewDataMapper,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeToTagInteractor: RecordTypeToTagInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val router: Router,
    private val recordsFilterUpdateInteractor: RecordsFilterUpdateInteractor,
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

    private var filters: List<RecordsFilter> = emptyList()
    private var filterSelectionState: RecordsFilterSelectionState = RecordsFilterSelectionState.Hidden
    private val defaultRange: Range by lazy { viewDataInteractor.getDefaultDateRange() }
    private val defaultDurationRange: Range by lazy { viewDataInteractor.getDefaultDurationRange() }
    private val defaultTimeOfDayRange: Range by lazy { viewDataInteractor.getDefaultTimeOfDayRange() }
    private var filtersLoadJob: Job? = null
    private var filtersSelectionLoadJob: Job? = null
    private var recordsLoadJob: Job? = null

    // Cache
    private var types: List<RecordType> = emptyList()
    private var recordTypeCategories: List<RecordTypeCategory>? = null
    private var categories: List<Category>? = null
    private var recordTags: List<RecordTag>? = null
    private var recordTypeToTag: List<RecordTypeToTag>? = null
    private var goals: List<RecordTypeGoal>? = null

    fun init(extra: RecordsFilterParams) {
        this.extra = extra
        filters = extra.filters.map(RecordsFilterParam::toModel).toMutableList()

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
        updateViewDataOnFiltersChanged()
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
        updateViewDataOnFiltersChanged()
    }

    fun onSelectionButtonClick(item: SelectionButtonViewData) = viewModelScope.launch {
        val data = item.type
            as? RecordsFilterSelectionButtonType
            ?: return@launch
        val subtype = data.subtype
        when (data.type) {
            is RecordsFilterSelectionButtonType.Type.Activities -> {
                filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.ACTIVITY)
                filters = recordsFilterUpdateInteractor.onTypesSelectionButtonClick(
                    currentFilters = filters,
                    subtype = subtype,
                    recordTypes = getTypesCache(),
                )
                checkTagFilterConsistency()
            }
            is RecordsFilterSelectionButtonType.Type.Categories -> {
                filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.CATEGORY)
                filters = recordsFilterUpdateInteractor.onCategoriesSelectionButtonClick(
                    currentFilters = filters,
                    subtype = subtype,
                    categories = getCategoriesCache(),
                )
                checkTagFilterConsistency()
            }
            is RecordsFilterSelectionButtonType.Type.Tags -> {
                filters = recordsFilterUpdateInteractor.onTagsSelectionButtonClick(
                    currentFilters = filters,
                    subtype = subtype,
                    currentState = filterSelectionState.type ?: return@launch,
                    tags = getTagsCache(),
                )
            }
        }
        updateViewDataOnFiltersChanged()
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
        updateViewDataOnFiltersChanged()
    }

    fun onCommentChange(text: String) {
        handleCommentChange(text)
        updateViewDataOnFiltersChanged()
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
        val requestedTags = listOf(
            DURATION_FROM_TAG,
            DURATION_TO_TAG,
        )
        if (tag !in requestedTags) return

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

        filters = recordsFilterUpdateInteractor.onDurationSet(
            currentFilters = filters,
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
        )

        updateViewDataOnFiltersChanged()
    }

    fun onRecordClick(
        item: RecordViewData,
        @Suppress("UNUSED_PARAMETER") sharedElements: Pair<Any, String>,
    ) {
        if (item is RecordViewData.Untracked) return // TODO manually filter untracked records?
        handleRecordClick(item.getUniqueId())
        updateViewDataOnFiltersChanged()
    }

    fun onInnerFilterButtonClick(viewData: RecordsFilterButtonViewData) {
        when (viewData.type) {
            RecordsFilterButtonViewData.Type.INVERT_SELECTION -> {
                handleInvertSelection()
            }
        }
        updateViewDataOnFiltersChanged()
    }

    fun onDayOfWeekClick(viewData: DayOfWeekViewData) {
        handleDayOfWeekClick(viewData.dayOfWeek)
        updateViewDataOnFiltersChanged()
    }

    fun onShowRecordsListClick() {
        filterSelectionState = RecordsFilterSelectionState.Hidden
        updateFilters()
        updateFilterSelectionVisibility()
    }

    private suspend fun handleTypeClick(id: Long) {
        filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.ACTIVITY)
        filters = recordsFilterUpdateInteractor.handleTypeClick(
            id = id,
            currentFilters = filters,
            recordTypes = getTypesCache(),
            recordTypeCategories = getRecordTypeCategoriesCache(),
        )
        checkTagFilterConsistency()
    }

    private suspend fun handleCategoryClick(id: Long) {
        filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.CATEGORY)
        filters = recordsFilterUpdateInteractor.handleCategoryClick(
            id = id,
            currentFilters = filters,
        )
        checkTagFilterConsistency()
    }

    private fun handleUntrackedClick() {
        filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.UNTRACKED)
        filters = recordsFilterUpdateInteractor.handleUntrackedClick(
            currentFilters = filters,
        )
    }

    private fun handleMultitaskClick() {
        filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.MULTITASK)
        filters = recordsFilterUpdateInteractor.handleMultitaskClick(
            currentFilters = filters,
        )
    }

    private fun handleCommentFilterClick(item: RecordFilterViewData) {
        filters = recordsFilterUpdateInteractor.handleCommentFilterClick(
            currentFilters = filters,
            item = item,
        )
    }

    private fun handleCommentChange(text: String) {
        filters = recordsFilterUpdateInteractor.handleCommentChange(
            currentFilters = filters,
            text = text,
        )
    }

    private fun handleTagClick(item: CategoryViewData.Record) {
        filters = recordsFilterUpdateInteractor.handleTagClick(
            currentState = filterSelectionState.type ?: return,
            currentFilters = filters,
            item = item,
        )
    }

    private fun handleRecordClick(id: Long) {
        filters = recordsFilterUpdateInteractor.handleRecordClick(
            currentFilters = filters,
            id = id,
        )
        checkManualFilterVisibility()
    }

    private suspend fun checkTagFilterConsistency() {
        filters = recordsFilterUpdateInteractor.checkTagFilterConsistency(
            currentFilters = filters,
            recordTypes = getTypesCache(),
            recordTypeCategories = getRecordTypeCategoriesCache(),
            recordTags = getTagsCache(),
            typesToTags = getRecordTypeToTagCache(),
        )
    }

    private fun removeFilter(type: RecordFilterViewData.Type) {
        filters = recordsFilterUpdateInteractor.removeFilter(
            currentFilters = filters,
            type = type,
        )

        // Switch back to activity if category removed.
        val currentSelectionType = (filterSelectionState as? RecordsFilterSelectionState.Visible)?.type
        if (currentSelectionType == RecordFilterViewData.Type.CATEGORY) {
            filterSelectionState = RecordsFilterSelectionState.Visible(RecordFilterViewData.Type.ACTIVITY)
        }
        checkManualFilterVisibility()
        updateViewDataOnFiltersChanged()
    }

    private fun handleInvertSelection() {
        filters = recordsFilterUpdateInteractor.handleInvertSelection(
            currentFilters = filters,
            recordsViewData = recordsViewData.value,
        )
        checkManualFilterVisibility()
    }

    private fun handleDayOfWeekClick(dayOfWeek: DayOfWeek) {
        filters = recordsFilterUpdateInteractor.handleDayOfWeekClick(
            currentFilters = filters,
            dayOfWeek = dayOfWeek,
        )
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
                value = DurationDialogParams.Value.Duration(
                    duration = range.timeStarted / 1000,
                ),
                hideDisableButton = true,
            )
            RecordsFilterRangeViewData.FieldType.TIME_ENDED -> DurationDialogParams(
                tag = DURATION_TO_TAG,
                value = DurationDialogParams.Value.Duration(
                    duration = range.timeEnded / 1000,
                ),
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

        filters = recordsFilterUpdateInteractor.handleDateSet(
            currentFilters = filters,
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
        )

        updateViewDataOnFiltersChanged()
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

        filters = recordsFilterUpdateInteractor.handleTimeOfDaySet(
            currentFilters = filters,
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
        )

        updateViewDataOnFiltersChanged()
    }

    private fun updateViewDataOnFiltersChanged() {
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

    private suspend fun getRecordTypeToTagCache(): List<RecordTypeToTag> {
        return recordTypeToTag ?: run {
            recordTypeToTagInteractor.getAll().also { recordTypeToTag = it }
        }
    }

    private suspend fun getGoalsCache(): List<RecordTypeGoal> {
        return goals ?: run {
            recordTypeGoalInteractor.getAllTypeGoals().also { goals = it }
        }
    }

    private fun updateFilterSelectionVisibility() {
        val data = loadFilterSelectionVisibility()
        filterSelectionVisibility.set(data)
    }

    private fun loadFilterSelectionVisibility(): Boolean {
        return filterSelectionState is RecordsFilterSelectionState.Visible
    }

    private fun updateFilters() {
        filtersLoadJob?.cancel()
        filtersLoadJob = viewModelScope.launch {
            changedFilters.set(
                RecordsFilterResultParams(
                    tag = extra.tag,
                    filters = filters,
                ),
            )
            val data = loadFiltersViewData()
            filtersViewData.set(data)
        }
    }

    private suspend fun loadFiltersViewData(): List<ViewHolderType> {
        return viewDataInteractor.getFiltersViewData(
            extra = extra,
            selectionState = filterSelectionState,
            filters = filters,
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
            goals = getGoalsCache().groupBy { it.idData.value },
        )
    }

    private fun updateFilterSelectionViewData() {
        filtersSelectionLoadJob?.cancel()
        filtersSelectionLoadJob = viewModelScope.launch {
            val data = loadFilterSelectionViewData()
            filterSelectionContent.set(data)
        }
    }

    private suspend fun loadFilterSelectionViewData(): List<ViewHolderType> {
        val type = filterSelectionState.type ?: return emptyList()

        return when (type) {
            RecordFilterViewData.Type.UNTRACKED,
            RecordFilterViewData.Type.MULTITASK,
            RecordFilterViewData.Type.ACTIVITY,
            RecordFilterViewData.Type.CATEGORY,
            -> {
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
            RecordFilterViewData.Type.FILTERED_TAGS,
            -> {
                viewDataInteractor.getTagsFilterSelectionViewData(
                    type = type,
                    filters = filters,
                    types = getTypesCache(),
                    recordTypeCategories = getRecordTypeCategoriesCache(),
                    recordTags = getTagsCache(),
                    recordTypesToTags = getRecordTypeToTagCache(),
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
