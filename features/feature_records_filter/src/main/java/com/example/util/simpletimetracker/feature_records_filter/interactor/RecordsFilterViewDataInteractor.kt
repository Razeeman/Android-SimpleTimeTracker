package com.example.util.simpletimetracker.feature_records_filter.interactor

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.DateDividerViewDataMapper
import com.example.util.simpletimetracker.core.mapper.DayOfWeekViewDataMapper
import com.example.util.simpletimetracker.core.mapper.MultitaskRecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.getAllTypeIds
import com.example.util.simpletimetracker.domain.extension.getCategoryIds
import com.example.util.simpletimetracker.domain.extension.getCategoryItems
import com.example.util.simpletimetracker.domain.extension.getCommentItems
import com.example.util.simpletimetracker.domain.extension.getComments
import com.example.util.simpletimetracker.domain.extension.getDate
import com.example.util.simpletimetracker.domain.extension.getDaysOfWeek
import com.example.util.simpletimetracker.domain.extension.getDuration
import com.example.util.simpletimetracker.domain.extension.getFilteredTags
import com.example.util.simpletimetracker.domain.extension.getManuallyFilteredRecordIds
import com.example.util.simpletimetracker.domain.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.extension.getTaggedIds
import com.example.util.simpletimetracker.domain.extension.getTimeOfDay
import com.example.util.simpletimetracker.domain.extension.hasCategoryFilter
import com.example.util.simpletimetracker.domain.extension.hasManuallyFiltered
import com.example.util.simpletimetracker.domain.extension.hasMultitaskFilter
import com.example.util.simpletimetracker.domain.extension.hasUncategorizedItem
import com.example.util.simpletimetracker.domain.extension.hasUntaggedItem
import com.example.util.simpletimetracker.domain.extension.hasUntrackedFilter
import com.example.util.simpletimetracker.domain.interactor.FilterSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.MultitaskRecord
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordTypeToTag
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_records_filter.R
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterButtonViewData
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterCommentViewData
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterRangeViewData
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterViewDataMapper
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterCommentType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectionState
import com.example.util.simpletimetracker.feature_records_filter.viewData.RecordsFilterSelectionButtonType
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RecordsFilterViewDataInteractor @Inject constructor(
    private val recordFilterInteractor: RecordFilterInteractor,
    private val filterSelectableTagsInteractor: FilterSelectableTagsInteractor,
    private val recordInteractor: RecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val mapper: RecordsFilterViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val recordViewDataMapper: RecordViewDataMapper,
    private val multitaskRecordViewDataMapper: MultitaskRecordViewDataMapper,
    private val getRunningRecordViewDataMediator: GetRunningRecordViewDataMediator,
    private val dateDividerViewDataMapper: DateDividerViewDataMapper,
    private val dayOfWeekViewDataMapper: DayOfWeekViewDataMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val rangeViewDataMapper: RangeViewDataMapper,
) {

    fun getDefaultDateRange(): Range {
        val calendar = Calendar.getInstance()
        val timeStarted = calendar.apply { setToStartOfDay() }.timeInMillis

        return Range(
            timeStarted = timeStarted,
            timeEnded = calendar.apply {
                timeInMillis = timeStarted
                add(Calendar.DATE, 1)
            }.timeInMillis,
        )
    }

    fun getDefaultDurationRange(): Range {
        return Range(
            timeStarted = 0L,
            timeEnded = TimeUnit.HOURS.toMillis(24),
        )
    }

    fun getDefaultTimeOfDayRange(): Range {
        return Range(
            timeStarted = 0L,
            timeEnded = TimeUnit.DAYS.toMillis(1) - TimeUnit.MINUTES.toMillis(1),
        )
    }

    suspend fun getDateTimeDialogParams(
        tag: String,
        timestamp: Long,
    ): DateTimeDialogParams {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

        return DateTimeDialogParams(
            tag = tag,
            timestamp = timestamp,
            type = DateTimeDialogType.DATETIME(initialTab = DateTimeDialogType.Tab.DATE),
            useMilitaryTime = useMilitaryTime,
            firstDayOfWeek = firstDayOfWeek,
        )
    }

    suspend fun getRecordsViewData(
        extra: RecordsFilterParams,
        filters: List<RecordsFilter>,
        recordTypes: Map<Long, RecordType>,
        recordTags: List<RecordTag>,
        goals: Map<Long, List<RecordTypeGoal>>,
    ): RecordsFilterSelectedRecordsViewData = withContext(Dispatchers.Default) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
        val finalFilters = filters
            .takeUnless {
                // If date isn't available and no other filters -
                // show empty records even if date is present.
                !extra.dateSelectionAvailable && filters.none { it !is RecordsFilter.Date }
            }
            .orEmpty()
        val records = recordFilterInteractor.getByFilter(finalFilters)
            .let { if (extra.addRunningRecords) it else it.filterIsInstance<Record>() }
        val manuallyFilteredRecords = filters
            .getManuallyFilteredRecordIds()
            .mapNotNull { recordInteractor.get(it) } // TODO do better
            .mapNotNull { record ->
                val mapped = recordViewDataMapper.mapFilteredRecord(
                    record = record,
                    recordTypes = recordTypes,
                    allRecordTags = recordTags,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                    isFiltered = true,
                ) ?: return@mapNotNull null
                record.timeStarted to mapped
            }

        var count: Int
        val viewData = records
            .mapNotNull { record ->
                ensureActive()
                val viewData = when (record) {
                    is Record -> if (record.typeId != UNTRACKED_ITEM_ID) {
                        recordViewDataMapper.map(
                            record = record,
                            recordType = recordTypes[record.typeId] ?: return@mapNotNull null,
                            recordTags = recordTags.filter { it.id in record.tagIds },
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            useProportionalMinutes = useProportionalMinutes,
                            showSeconds = showSeconds,
                        )
                    } else {
                        recordViewDataMapper.mapToUntracked(
                            timeStarted = record.timeStarted,
                            timeEnded = record.timeEnded,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            useProportionalMinutes = useProportionalMinutes,
                            showSeconds = showSeconds,
                        )
                    }
                    is RunningRecord -> getRunningRecordViewDataMediator.execute(
                        type = recordTypes[record.id] ?: return@mapNotNull null,
                        tags = recordTags.filter { it.id in record.tagIds },
                        goals = goals[record.id].orEmpty(),
                        record = record,
                        nowIconVisible = true,
                        goalsVisible = false,
                        totalDurationVisible = false,
                        isDarkTheme = isDarkTheme,
                        useMilitaryTime = useMilitaryTime,
                        useProportionalMinutes = useProportionalMinutes,
                        showSeconds = showSeconds,
                    )
                    is MultitaskRecord -> multitaskRecordViewDataMapper.map(
                        multitaskRecord = record,
                        recordTypes = recordTypes,
                        recordTags = recordTags,
                        isDarkTheme = isDarkTheme,
                        useMilitaryTime = useMilitaryTime,
                        useProportionalMinutes = useProportionalMinutes,
                        showSeconds = showSeconds,
                    )
                }
                record.timeStarted to viewData
            }
            .also { count = it.size }
            .plus(manuallyFilteredRecords)
            .sortedByDescending { (timeStarted, _) -> timeStarted }
            .let(dateDividerViewDataMapper::addDateViewData)
            .ifEmpty { listOf(recordViewDataMapper.mapToEmpty()) }
        val filterSelected = count != 0 || finalFilters.isNotEmpty()

        return@withContext RecordsFilterSelectedRecordsViewData(
            isLoading = false,
            selectedRecordsCount = mapper.mapRecordsCount(
                extra = extra,
                count = count,
                filterSelected = filterSelected,
            ),
            showListButtonIsVisible = filterSelected,
            recordsViewData = viewData,
        )
    }

    suspend fun getFiltersViewData(
        extra: RecordsFilterParams,
        selectionState: RecordsFilterSelectionState,
        filters: List<RecordsFilter>,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val hasUntracked = filters.hasUntrackedFilter()
        val hasMultitask = filters.hasMultitaskFilter()

        val availableFilters = listOfNotNull(
            when {
                hasUntracked -> RecordFilterType.Untracked
                hasMultitask -> RecordFilterType.Multitask
                filters.hasCategoryFilter() -> RecordFilterType.Category
                else -> RecordFilterType.Activity
            },
            RecordFilterType.Comment.takeUnless { hasUntracked || hasMultitask },
            RecordFilterType.SelectedTags.takeUnless { hasUntracked || hasMultitask },
            RecordFilterType.FilteredTags.takeUnless { hasUntracked || hasMultitask },
            RecordFilterType.Date.takeIf { extra.dateSelectionAvailable },
            RecordFilterType.DaysOfWeek,
            RecordFilterType.TimeOfDay,
            RecordFilterType.Duration,
            RecordFilterType.ManuallyFiltered.takeIf {
                filters.hasManuallyFiltered() && !hasUntracked && !hasMultitask
            },
        )

        return@withContext availableFilters.mapIndexed { index, type ->
            val clazz = mapper.mapToClass(type)
            // Only one filter type.
            val filter = filters.filterIsInstance(clazz).firstOrNull()
            val enabled = filter != null
            val selected = (selectionState as? RecordsFilterSelectionState.Visible)
                ?.type == type

            FilterViewData(
                id = index.toLong(),
                type = type,
                name = if (filter != null) {
                    mapper.mapActiveFilterName(
                        filter = filter,
                        useMilitaryTime = useMilitaryTime,
                        startOfDayShift = startOfDayShift,
                        firstDayOfWeek = firstDayOfWeek,
                    )
                } else {
                    mapper.mapInactiveFilterName(type)
                },
                color = if (enabled) {
                    colorMapper.toActiveColor(isDarkTheme)
                } else {
                    colorMapper.toInactiveColor(isDarkTheme)
                },
                removeBtnVisible = enabled,
                selected = selected,
            )
        }
    }

    suspend fun getActivityFilterSelectionViewData(
        extra: RecordsFilterParams,
        filters: List<RecordsFilter>,
        types: List<RecordType>,
        recordTypeCategories: List<RecordTypeCategory>,
        categories: List<Category>,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val selectedCategoryItems: List<RecordsFilter.CategoryItem> = filters.getCategoryItems()
        val selectedCategoryIds: List<Long> = filters.getCategoryIds()
        val allSelectedTypeIds: List<Long> = filters.getAllTypeIds(types, recordTypeCategories)

        val typesViewData = types.map { type ->
            recordTypeViewDataMapper.mapFiltered(
                recordType = type,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
                isFiltered = type.id !in allSelectedTypeIds,
                isChecked = null,
                isComplete = false,
            )
        }

        val typesSelectionButtons = mapper.mapToSelectionButtons(
            type = RecordsFilterSelectionButtonType.Type.Activities,
            isDarkTheme = isDarkTheme,
        )

        val categoriesViewData = categories
            .map { category ->
                categoryViewDataMapper.mapCategory(
                    category = category,
                    isDarkTheme = isDarkTheme,
                    isFiltered = category.id !in selectedCategoryIds,
                )
            }
            .takeUnless { it.isEmpty() }
            ?.plus(
                categoryViewDataMapper.mapToUncategorizedItem(
                    isFiltered = !selectedCategoryItems.hasUncategorizedItem(),
                    isDarkTheme = isDarkTheme,
                ),
            )
            .orEmpty()

        val categoriesSelectionButtons = mapper.mapToSelectionButtons(
            type = RecordsFilterSelectionButtonType.Type.Categories,
            isDarkTheme = isDarkTheme,
        )

        if (categoriesViewData.isNotEmpty()) {
            HintViewData(resourceRepo.getString(R.string.category_hint)).let(result::add)
            categoriesSelectionButtons.let(result::addAll)
            categoriesViewData.let(result::addAll)
            DividerViewData(1).let(result::add)
        }

        if (typesViewData.isNotEmpty()) {
            HintViewData(resourceRepo.getString(R.string.activity_hint)).let(result::add)
            typesSelectionButtons.let(result::addAll)
            typesViewData.let(result::addAll)
        } else {
            HintViewData(resourceRepo.getString(R.string.record_types_empty)).let(result::add)
        }

        if (
            extra.untrackedSelectionAvailable ||
            extra.multitaskSelectionAvailable
        ) {
            DividerViewData(2).let(result::add)
        }

        if (extra.untrackedSelectionAvailable) {
            categoryViewDataMapper.mapToTagUntrackedItem(
                isFiltered = !filters.hasUntrackedFilter(),
                isDarkTheme = isDarkTheme,
            ).let(result::add)
        }

        if (extra.multitaskSelectionAvailable) {
            categoryViewDataMapper.mapToMultitaskItem(
                isFiltered = !filters.hasMultitaskFilter(),
                isDarkTheme = isDarkTheme,
            ).let(result::add)
        }

        return@withContext result
    }

    suspend fun getCommentFilterSelectionViewData(
        filters: List<RecordsFilter>,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val result: MutableList<ViewHolderType> = mutableListOf()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val commentFilters = listOf(
            RecordFilterCommentType.NoComment,
            RecordFilterCommentType.AnyComment,
        )

        result += EmptySpaceViewData(
            id = 1,
            width = EmptySpaceViewData.ViewDimension.MatchParent,
            height = EmptySpaceViewData.ViewDimension.ExactSizeDp(6),
        )
        result += commentFilters.map {
            mapper.mapCommentFilter(
                type = it,
                filters = filters,
                isDarkTheme = isDarkTheme,
            )
        }
        result += DividerViewData(1)

        val comment = filters
            .getCommentItems()
            .getComments()
            .firstOrNull()
        result += RecordsFilterCommentViewData(
            id = 1L, // Only one at the time.
            text = comment.orEmpty(),
        )

        return@withContext result
    }

    suspend fun getTagsFilterSelectionViewData(
        type: RecordFilterType,
        filters: List<RecordsFilter>,
        types: List<RecordType>,
        recordTypeCategories: List<RecordTypeCategory>,
        recordTags: List<RecordTag>,
        recordTypesToTags: List<RecordTypeToTag>,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val isDarkTheme = prefsInteractor.getDarkMode()
        val typesMap = types.associateBy(RecordType::id)
        val selectedTypes = filters
            .getAllTypeIds(types, recordTypeCategories)
            .takeUnless { it.isEmpty() }
            ?: types.map(RecordType::id)
        val selectedTags: List<RecordsFilter.TagItem> = when (type) {
            RecordFilterType.SelectedTags -> filters.getSelectedTags()
            RecordFilterType.FilteredTags -> filters.getFilteredTags()
            else -> emptyList()
        }
        val selectedTaggedIds: List<Long> = selectedTags.getTaggedIds()
        val selectableTagIds = filterSelectableTagsInteractor.execute(
            tagIds = recordTags.map { it.id },
            typesToTags = recordTypesToTags,
            typeIds = selectedTypes,
        )

        val recordTagsViewData = recordTags
            .filter { it.id in selectableTagIds }
            .map { tag ->
                categoryViewDataMapper.mapRecordTag(
                    tag = tag,
                    type = typesMap[tag.iconColorSource],
                    isDarkTheme = isDarkTheme,
                    isFiltered = tag.id !in selectedTaggedIds,
                )
            }
            .takeUnless { it.isEmpty() }
            ?.plus(
                categoryViewDataMapper.mapToUntaggedItem(
                    isFiltered = !selectedTags.hasUntaggedItem(),
                    isDarkTheme = isDarkTheme,
                ),
            )
            .orEmpty()

        val selectionButtons = mapper.mapToSelectionButtons(
            type = RecordsFilterSelectionButtonType.Type.Tags,
            isDarkTheme = isDarkTheme,
        )

        if (recordTagsViewData.isNotEmpty()) {
            HintViewData(resourceRepo.getString(R.string.record_tag_hint)).let(result::add)
            selectionButtons.let(result::addAll)
            recordTagsViewData.let(result::addAll)
        } else {
            HintViewData(resourceRepo.getString(R.string.change_record_categories_empty)).let(result::add)
        }

        return@withContext result
    }

    suspend fun getDateFilterSelectionViewData(
        filters: List<RecordsFilter>,
        defaultRange: Range,
        extra: RecordsFilterParams,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val result: MutableList<ViewHolderType> = mutableListOf()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val filter = filters.getDate()
        val filterRange = filter?.range
        val lastDays = if (filterRange is RangeLength.Last) {
            filterRange.days
        } else {
            extra.defaultLastDaysNumber
        }
        val range = filter
            ?.takeUnless { it.range is RangeLength.All }
            ?.let { recordFilterInteractor.getRange(it) }
            ?: defaultRange

        result += EmptySpaceViewData(
            id = 1,
            width = EmptySpaceViewData.ViewDimension.MatchParent,
            height = EmptySpaceViewData.ViewDimension.ExactSizeDp(4),
        )
        result += listOf(
            RangeLength.Day,
            RangeLength.Week,
            RangeLength.Month,
            RangeLength.Year,
            RangeLength.All,
            RangeLength.Last(lastDays),
        ).mapIndexed { index, rangeLength ->
            mapper.mapDateRangeFilter(
                rangeLength = rangeLength,
                filter = filter,
                isDarkTheme = isDarkTheme,
                startOfDayShift = startOfDayShift,
                firstDayOfWeek = firstDayOfWeek,
                index = index,
            )
        }
        result += DividerViewData(1)
        result += HintViewData(text = resourceRepo.getString(R.string.range_custom))
        result += RecordsFilterRangeViewData(
            id = 1L, // Only one at the time.
            timeStarted = timeMapper.formatDateTimeYear(
                time = range.timeStarted,
                useMilitaryTime = useMilitaryTime,
            ),
            timeStartedHint = resourceRepo.getString(R.string.change_record_date_time_start),
            timeEnded = timeMapper.formatDateTimeYear(
                time = range.timeEnded,
                useMilitaryTime = useMilitaryTime,
            ),
            timeEndedHint = resourceRepo.getString(R.string.change_record_date_time_end),
            gravity = RecordsFilterRangeViewData.Gravity.CENTER,
            textColor = mapper.mapTextFieldColor(
                isSelected = filter?.range is RangeLength.Custom,
                isDarkTheme = isDarkTheme,
            ),
        )

        return@withContext result
    }

    suspend fun getManualFilterSelectionViewData(
        filters: List<RecordsFilter>,
        recordTypes: Map<Long, RecordType>,
        recordTags: List<RecordTag>,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
        val button = RecordsFilterButtonViewData(
            type = RecordsFilterButtonViewData.Type.INVERT_SELECTION,
            text = resourceRepo.getString(R.string.records_filter_invert_selection),
        )

        return@withContext button.let(::listOf) + filters
            .getManuallyFilteredRecordIds()
            .mapNotNull { recordInteractor.get(it) } // TODO do better
            .mapNotNull { record ->
                val mapped = recordViewDataMapper.mapFilteredRecord(
                    record = record,
                    recordTypes = recordTypes,
                    allRecordTags = recordTags,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                    isFiltered = false,
                ) ?: return@mapNotNull null
                record.timeStarted to mapped
            }
            .sortedByDescending { (timeStarted, _) -> timeStarted }
            .let(dateDividerViewDataMapper::addDateViewData)
    }

    suspend fun getDaysOfWeekFilterSelectionViewData(
        filters: List<RecordsFilter>,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val result: MutableList<ViewHolderType> = mutableListOf()
        val selectedDays = filters.getDaysOfWeek()
        val isDarkTheme = prefsInteractor.getDarkMode()

        result += EmptySpaceViewData(
            id = 1,
            width = EmptySpaceViewData.ViewDimension.MatchParent,
            height = EmptySpaceViewData.ViewDimension.ExactSizeDp(6),
        )
        result += dayOfWeekViewDataMapper.mapViewData(
            selectedDaysOfWeek = selectedDays,
            isDarkTheme = isDarkTheme,
            width = DayOfWeekViewData.Width.WrapContent,
            paddingHorizontalDp = 16,
        )

        return@withContext result
    }

    suspend fun getTimeOfDayFilterSelectionViewData(
        filters: List<RecordsFilter>,
        defaultRange: Range,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val result: MutableList<ViewHolderType> = mutableListOf()
        val filter = filters.getTimeOfDay()
        val range = filter ?: defaultRange
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val startOfDay = timeMapper.getStartOfDayTimeStamp()
        val isDarkTheme = prefsInteractor.getDarkMode()

        result += EmptySpaceViewData(
            id = 1,
            width = EmptySpaceViewData.ViewDimension.MatchParent,
            height = EmptySpaceViewData.ViewDimension.ExactSizeDp(4),
        )
        result += RecordsFilterRangeViewData(
            id = 1L, // Only one at the time.
            timeStarted = timeMapper.formatTime(
                time = range.timeStarted + startOfDay,
                useMilitaryTime = useMilitaryTime,
                showSeconds = false,
            ),
            timeStartedHint = resourceRepo.getString(R.string.change_record_date_time_start),
            timeEnded = timeMapper.formatTime(
                time = range.timeEnded + startOfDay,
                useMilitaryTime = useMilitaryTime,
                showSeconds = false,
            ),
            timeEndedHint = resourceRepo.getString(R.string.change_record_date_time_end),
            gravity = RecordsFilterRangeViewData.Gravity.CENTER,
            textColor = mapper.mapTextFieldColor(
                isSelected = filter != null,
                isDarkTheme = isDarkTheme,
            ),
        )

        return@withContext result
    }

    suspend fun getDurationFilterSelectionViewData(
        filters: List<RecordsFilter>,
        defaultRange: Range,
    ): List<ViewHolderType> {
        val result: MutableList<ViewHolderType> = mutableListOf()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val filter = filters.getDuration()
        val range = filter ?: defaultRange

        result += EmptySpaceViewData(
            id = 1,
            width = EmptySpaceViewData.ViewDimension.MatchParent,
            height = EmptySpaceViewData.ViewDimension.ExactSizeDp(4),
        )
        result += RecordsFilterRangeViewData(
            id = 1L, // Only one at the time.
            timeStarted = timeMapper.formatDuration(range.timeStarted / 1000),
            timeStartedHint = resourceRepo.getString(R.string.records_filter_duration_min),
            timeEnded = timeMapper.formatDuration(range.timeEnded / 1000),
            timeEndedHint = resourceRepo.getString(R.string.records_filter_duration_max),
            gravity = RecordsFilterRangeViewData.Gravity.CENTER,
            textColor = mapper.mapTextFieldColor(
                isSelected = filter != null,
                isDarkTheme = isDarkTheme,
            ),
        )

        return result
    }
}