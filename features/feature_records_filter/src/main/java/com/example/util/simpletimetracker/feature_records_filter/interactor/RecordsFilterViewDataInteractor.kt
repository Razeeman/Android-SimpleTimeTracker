package com.example.util.simpletimetracker.feature_records_filter.interactor

import androidx.core.text.buildSpannedString
import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.DateDividerViewDataMapper
import com.example.util.simpletimetracker.core.mapper.DayOfWeekViewDataMapper
import com.example.util.simpletimetracker.core.mapper.MultitaskRecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RunningRecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.category.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.extension.addBetweenEach
import com.example.util.simpletimetracker.domain.extension.orEmpty
import com.example.util.simpletimetracker.domain.extension.plus
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.extension.getAllTypeIds
import com.example.util.simpletimetracker.domain.record.extension.getCategoryIds
import com.example.util.simpletimetracker.domain.record.extension.getCategoryItems
import com.example.util.simpletimetracker.domain.record.extension.getCommentItems
import com.example.util.simpletimetracker.domain.record.extension.getComments
import com.example.util.simpletimetracker.domain.record.extension.getDate
import com.example.util.simpletimetracker.domain.record.extension.getDaysOfWeek
import com.example.util.simpletimetracker.domain.record.extension.getDuration
import com.example.util.simpletimetracker.domain.record.extension.getFilteredCategoryIds
import com.example.util.simpletimetracker.domain.record.extension.getFilteredCategoryItems
import com.example.util.simpletimetracker.domain.record.extension.getFilteredTags
import com.example.util.simpletimetracker.domain.record.extension.getFilteredTypeIds
import com.example.util.simpletimetracker.domain.record.extension.getManuallyFilteredItems
import com.example.util.simpletimetracker.domain.record.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.record.extension.getTaggedIds
import com.example.util.simpletimetracker.domain.record.extension.getTimeOfDay
import com.example.util.simpletimetracker.domain.record.extension.hasDuplicationsFilter
import com.example.util.simpletimetracker.domain.record.extension.hasFilteredActivityFilter
import com.example.util.simpletimetracker.domain.record.extension.hasFilteredCategoryFilter
import com.example.util.simpletimetracker.domain.record.extension.hasManuallyFiltered
import com.example.util.simpletimetracker.domain.record.extension.hasSelectedActivityFilter
import com.example.util.simpletimetracker.domain.record.extension.hasSelectedCategoryFilter
import com.example.util.simpletimetracker.domain.record.extension.hasUncategorizedItem
import com.example.util.simpletimetracker.domain.record.extension.hasUntaggedItem
import com.example.util.simpletimetracker.domain.record.extension.hasUntrackedFilter
import com.example.util.simpletimetracker.domain.record.extension.toManuallyFilteredItem
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.FavouriteRecordsFilter
import com.example.util.simpletimetracker.domain.record.model.MultitaskRecord
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordTag.interactor.FilterSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTypeToTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.recordsFilter.interactor.FavouriteRecordsFilterInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.commentField.CommentFieldViewData
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSuggestion.RecordTypeSuggestionViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordsFilter.FavouriteRecordsFilterViewData
import com.example.util.simpletimetracker.feature_records_filter.R
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterButtonViewData
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterRangeViewData
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterFavouriteViewDataMapper
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterViewDataMapper
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterActivitiesType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterCommentType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterDuplicationsType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterSelectionType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectionState
import com.example.util.simpletimetracker.feature_records_filter.viewData.CategoryFilteredType
import com.example.util.simpletimetracker.feature_records_filter.viewData.RecordTypeFilteredType
import com.example.util.simpletimetracker.feature_records_filter.viewData.RecordsFilterSelectionButtonType
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
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
    private val runningRecordInteractor: RunningRecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val mapper: RecordsFilterViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val recordViewDataMapper: RecordViewDataMapper,
    private val runningRecordViewDataMapper: RunningRecordViewDataMapper,
    private val multitaskRecordViewDataMapper: MultitaskRecordViewDataMapper,
    private val recordsFilterFavouriteViewDataMapper: RecordsFilterFavouriteViewDataMapper,
    private val getRunningRecordViewDataMediator: GetRunningRecordViewDataMediator,
    private val favouriteRecordsFilterInteractor: FavouriteRecordsFilterInteractor,
    private val dateDividerViewDataMapper: DateDividerViewDataMapper,
    private val dayOfWeekViewDataMapper: DayOfWeekViewDataMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
) {

    fun filterAvailableFilters(
        extra: RecordsFilterParams,
        filters: List<RecordsFilter>,
    ): List<RecordsFilter> {
        return filters.filter {
            when (it) {
                is RecordsFilter.Date -> extra.flags.dateSelectionAvailable
                is RecordsFilter.Untracked -> extra.flags.untrackedSelectionAvailable
                is RecordsFilter.Multitask -> extra.flags.multitaskSelectionAvailable
                is RecordsFilter.Duplications -> extra.flags.duplicationsSelectionAvailable
                else -> true
            }
        }
    }

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
        val durationFormat = prefsInteractor.getDurationFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val manuallyFilteredItems = filters.getManuallyFilteredItems()
            .keys.toList()
        val finalFilters = filters
            .takeUnless {
                // If date isn't available and no other filters -
                // show empty records even if date is present.
                !extra.flags.dateSelectionAvailable &&
                    filters.none { it !is RecordsFilter.Date }
            }
            .orEmpty()
            .toMutableList()
            .apply {
                // Filtered records are marked separately here, so need records with them.
                // But of only manual filter selected - leave filter, otherwise records would be empty.
                if (filters.any { it !is RecordsFilter.ManuallyFiltered }) {
                    removeAll { it is RecordsFilter.ManuallyFiltered }
                }
            }

        val records = recordFilterInteractor.getByFilter(finalFilters)
            .let { if (extra.flags.addRunningRecords) it else it.filterIsInstance<Record>() }

        var count: Int
        var filtered = 0
        val viewData = records
            .mapNotNull { record ->
                ensureActive()
                val isManuallyFiltered = record.toManuallyFilteredItem() in manuallyFilteredItems
                val viewData = when (record) {
                    is Record -> if (record.typeId != UNTRACKED_ITEM_ID) {
                        recordViewDataMapper.map(
                            record = record,
                            recordTypes = recordTypes,
                            recordTags = recordTags,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            durationFormat = durationFormat,
                            showSeconds = showSeconds,
                        )?.let {
                            if (isManuallyFiltered) {
                                filtered += 1
                                recordViewDataMapper.mapFiltered(
                                    viewData = it,
                                    isDarkTheme = isDarkTheme,
                                    isFiltered = true,
                                )
                            } else {
                                it
                            }
                        } ?: return@mapNotNull null
                    } else {
                        recordViewDataMapper.mapToUntracked(
                            timeStarted = record.timeStarted,
                            timeEnded = record.timeEnded,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            durationFormat = durationFormat,
                            showSeconds = showSeconds,
                        ).let {
                            if (isManuallyFiltered) {
                                filtered += 1
                                recordViewDataMapper.mapFiltered(
                                    viewData = it,
                                    isDarkTheme = isDarkTheme,
                                    isFiltered = true,
                                )
                            } else {
                                it
                            }
                        }
                    }
                    is RunningRecord -> getRunningRecordViewDataMediator.execute(
                        type = recordTypes[record.id] ?: return@mapNotNull null,
                        tags = recordTags,
                        goals = goals[record.id].orEmpty(),
                        record = record,
                        nowIconVisible = true,
                        goalsVisible = false,
                        totalDurationVisible = false,
                        isDarkTheme = isDarkTheme,
                        useMilitaryTime = useMilitaryTime,
                        durationFormat = durationFormat,
                        showSeconds = showSeconds,
                    ).let {
                        if (isManuallyFiltered) {
                            filtered += 1
                            runningRecordViewDataMapper.mapFiltered(
                                viewData = it,
                                isDarkTheme = isDarkTheme,
                                isFiltered = true,
                            )
                        } else {
                            it
                        }
                    }
                    is MultitaskRecord -> multitaskRecordViewDataMapper.map(
                        multitaskRecord = record,
                        recordTypes = recordTypes,
                        recordTags = recordTags,
                        isDarkTheme = isDarkTheme,
                        useMilitaryTime = useMilitaryTime,
                        durationFormat = durationFormat,
                        showSeconds = showSeconds,
                    ).let {
                        if (isManuallyFiltered) {
                            filtered += 1
                            multitaskRecordViewDataMapper.mapFiltered(
                                viewData = it,
                                isDarkTheme = isDarkTheme,
                                isFiltered = true,
                            )
                        } else {
                            it
                        }
                    }
                }
                record.timeStarted to viewData
            }
            .also { count = it.size - filtered }
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
            recordsViewData = RecordsFilterSelectedRecordsViewData.RecordsViewData.Content(viewData),
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

        val availableFilters = listOfNotNull(
            when {
                hasUntracked -> RecordFilterType.Untracked
                filters.hasSelectedActivityFilter() -> RecordFilterType.Activity
                filters.hasSelectedCategoryFilter() -> RecordFilterType.Category
                filters.hasFilteredActivityFilter() -> RecordFilterType.Activity
                filters.hasFilteredCategoryFilter() -> RecordFilterType.Category
                else -> RecordFilterType.Activity
            },
            RecordFilterType.Tags.takeUnless { hasUntracked },
            RecordFilterType.Comment.takeUnless { hasUntracked },
            RecordFilterType.Date.takeIf { extra.flags.dateSelectionAvailable },
            RecordFilterType.DaysOfWeek,
            RecordFilterType.TimeOfDay,
            RecordFilterType.Duration,
            RecordFilterType.Multitask.takeIf { extra.flags.multitaskSelectionAvailable && !hasUntracked },
            RecordFilterType.Duplications.takeIf { extra.flags.duplicationsSelectionAvailable },
            RecordFilterType.ManuallyFiltered.takeIf { filters.hasManuallyFiltered() },
            RecordFilterType.Favourite.takeIf { extra.flags.favouriteSelectionAvailable },
        ).sortedBy {
            mapper.mapSortOrder(it)
        }

        return@withContext availableFilters.mapIndexed { index, type ->
            val clazz = mapper.mapToClass(type)
            // Only one filter type.
            val filter = clazz?.let { filters.filterIsInstance(it) }?.firstOrNull()
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
        type: RecordFilterActivitiesType,
        extra: RecordsFilterParams,
        filters: List<RecordsFilter>,
        isArchivedShown: Boolean,
        types: List<RecordType>,
        recordTypeCategories: List<RecordTypeCategory>,
        categories: List<Category>,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val isDarkTheme = prefsInteractor.getDarkMode()
        val numberOfCards = prefsInteractor.getNumberOfCards()

        result += EmptySpaceViewData(
            id = 1,
            width = EmptySpaceViewData.ViewDimension.MatchParent,
            height = EmptySpaceViewData.ViewDimension.ExactSizeDp(6),
        )
        result += listOf(
            RecordFilterActivitiesType.Activities,
            RecordFilterActivitiesType.Categories,
        ).map {
            mapper.mapActivitiesSelectionTypeFilter(
                type = it,
                filters = filters,
                currentType = type,
                isDarkTheme = isDarkTheme,
            )
        }
        result += DividerViewData(1)

        when (type) {
            is RecordFilterActivitiesType.Activities -> {
                if (types.isNotEmpty()) {
                    val typesViewData = mapActivitiesViewData(
                        types = types,
                        isArchivedShown = isArchivedShown,
                        showArchived = types.any { it.hidden },
                        numberOfCards = numberOfCards,
                        isDarkTheme = isDarkTheme,
                        allSelectedTypeIds = filters.getAllTypeIds(types, recordTypeCategories),
                    )
                    val typesSelectionButtons = mapper.mapToSelectionButtons(
                        type = RecordsFilterSelectionButtonType.Type.Activities,
                    )
                    result += typesSelectionButtons
                    result += typesViewData
                    val filteredTypeIds = filters.getFilteredTypeIds()
                    if (filteredTypeIds.isNotEmpty()) {
                        result += DividerViewData(3)
                        result += HintViewData(resourceRepo.getString(R.string.records_filter_exclude))
                        result += mapActivitiesViewData(
                            types = types.filter { it.id in filteredTypeIds },
                            isArchivedShown = true,
                            showArchived = false,
                            numberOfCards = numberOfCards,
                            isDarkTheme = isDarkTheme,
                            allSelectedTypeIds = filteredTypeIds,
                        ).map {
                            RecordTypeSuggestionViewData(
                                data = it,
                                type = RecordTypeFilteredType,
                            )
                        }
                    }
                } else {
                    result += HintViewData(resourceRepo.getString(R.string.record_types_empty))
                }
            }
            is RecordFilterActivitiesType.Categories -> {
                if (categories.isNotEmpty()) {
                    val categoriesViewData = mapCategoriesViewData(
                        categories = categories,
                        isDarkTheme = isDarkTheme,
                        selectedCategoryIds = filters.getCategoryIds(),
                        selectedCategoryItems = filters.getCategoryItems(),
                        addUncategorizedItem = true,
                    )
                    val categoriesSelectionButtons = mapper.mapToSelectionButtons(
                        type = RecordsFilterSelectionButtonType.Type.Categories,
                    )
                    result += categoriesSelectionButtons
                    result += categoriesViewData
                    val filteredCategoryIds = filters.getFilteredCategoryIds()
                    val filteredCategoryItems = filters.getFilteredCategoryItems()
                    if (filteredCategoryItems.isNotEmpty()) {
                        result += DividerViewData(3)
                        result += HintViewData(resourceRepo.getString(R.string.records_filter_exclude))
                        result += mapCategoriesViewData(
                            categories = categories.filter { it.id in filteredCategoryIds },
                            isDarkTheme = isDarkTheme,
                            selectedCategoryIds = filteredCategoryIds,
                            selectedCategoryItems = filteredCategoryItems,
                            addUncategorizedItem = filteredCategoryItems.hasUncategorizedItem(),
                        ).map {
                            it.copy(type = CategoryFilteredType)
                        }
                    }
                } else {
                    result += HintViewData(resourceRepo.getString(R.string.change_record_type_categories_empty))
                }
            }
        }

        if (extra.flags.untrackedSelectionAvailable) {
            DividerViewData(2).let(result::add)
            categoryViewDataMapper.mapToTagUntrackedItem(
                isFiltered = !filters.hasUntrackedFilter(),
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
        result += CommentFieldViewData(
            id = 1L, // Only one at the time.
            text = comment.orEmpty(),
            marginTopDp = -2,
            marginHorizontal = resourceRepo.getDimenInDp(R.dimen.edit_screen_margin_horizontal),
            hint = resourceRepo.getString(R.string.change_record_comment_hint),
            valueType = CommentFieldViewData.ValueType.TextMultiLine,
        )

        return@withContext result
    }

    suspend fun getDuplicationsFilterSelectionViewData(
        filters: List<RecordsFilter>,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val result: MutableList<ViewHolderType> = mutableListOf()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val duplicationsFilters = listOf(
            RecordFilterDuplicationsType.SameActivity,
            RecordFilterDuplicationsType.SameTimes,
        )
        val button = RecordsFilterButtonViewData(
            type = RecordsFilterButtonViewData.Type.FILTER_DUPLICATES,
            text = resourceRepo.getString(R.string.records_filter_duplications_manually_fitler),
            backgroundColor = colorMapper.toActiveColor(isDarkTheme),
            isEnabled = true,
        )

        result += EmptySpaceViewData(
            id = 1,
            width = EmptySpaceViewData.ViewDimension.MatchParent,
            height = EmptySpaceViewData.ViewDimension.ExactSizeDp(6),
        )
        result += duplicationsFilters.map {
            mapper.mapDuplicationsFilter(
                type = it,
                filters = filters,
                isDarkTheme = isDarkTheme,
            )
        }
        if (filters.hasDuplicationsFilter()) {
            result += DividerViewData(1)
            result += button
        }

        return@withContext result
    }

    suspend fun getTagsFilterSelectionViewData(
        type: RecordFilterSelectionType,
        filters: List<RecordsFilter>,
        isArchivedShown: Boolean,
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
            is RecordFilterSelectionType.Select -> filters.getSelectedTags()
            is RecordFilterSelectionType.Filter -> filters.getFilteredTags()
        }
        val selectedTaggedIds: List<Long> = selectedTags.getTaggedIds()
        val selectableTagIds = filterSelectableTagsInteractor.execute(
            tagIds = recordTags.map { it.id },
            typesToTags = recordTypesToTags,
            typeIds = selectedTypes,
        )
        val selectableTags = recordTags.filter { it.id in selectableTagIds }
        val showArchived = selectableTags.any { it.archived }

        val recordTagsViewData = mapTagsViewData(
            isArchivedShown = isArchivedShown,
            selectedTagIds = selectedTaggedIds,
            selectableTags = selectableTags,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
            showArchived = showArchived,
            hasUntaggedItem = selectedTags.hasUntaggedItem(),
        )

        if (recordTagsViewData.isEmpty()) {
            return@withContext listOf(HintViewData(resourceRepo.getString(R.string.change_record_categories_empty)))
        }

        result += EmptySpaceViewData(
            id = 1,
            width = EmptySpaceViewData.ViewDimension.MatchParent,
            height = EmptySpaceViewData.ViewDimension.ExactSizeDp(6),
        )
        result += listOf(
            RecordFilterSelectionType.Select,
            RecordFilterSelectionType.Filter,
        ).map {
            mapper.mapTagSelectionTypeFilter(
                type = it,
                filters = filters,
                currentType = type,
                isDarkTheme = isDarkTheme,
            )
        }
        result += DividerViewData(1)
        result += mapper.mapToSelectionButtons(
            type = RecordsFilterSelectionButtonType.Type.Tags,
        )
        result += recordTagsViewData

        return@withContext result
    }

    suspend fun getDateFilterSelectionViewData(
        filters: List<RecordsFilter>,
        currentRange: Range,
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
        val customRange = (filterRange as? RangeLength.Custom)?.range.orEmpty()

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
            RangeLength.Custom(customRange),
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
                time = currentRange.timeStarted,
                useMilitaryTime = useMilitaryTime,
            ),
            timeStartedHint = resourceRepo.getString(R.string.change_record_date_time_start),
            timeEnded = timeMapper.formatDateTimeYear(
                time = currentRange.timeEnded,
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

    // TODO add loader when a lot of records filtered.
    suspend fun getManualFilterSelectionViewData(
        filters: List<RecordsFilter>,
        recordTypes: Map<Long, RecordType>,
        recordTags: List<RecordTag>,
        goals: Map<Long, List<RecordTypeGoal>>,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val durationFormat = prefsInteractor.getDurationFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val manuallyFilteredItems = filters.getManuallyFilteredItems()
            .keys.toList()
        val button = RecordsFilterButtonViewData(
            type = RecordsFilterButtonViewData.Type.INVERT_SELECTION,
            text = resourceRepo.getString(R.string.records_filter_invert_selection),
            backgroundColor = colorMapper.toActiveColor(isDarkTheme),
            isEnabled = true,
        )

        val manuallyFilteredTracked = manuallyFilteredItems
            .filterIsInstance<RecordsFilter.ManuallyFilteredItem.Tracked>()
        val manuallyFilteredRunning = manuallyFilteredItems
            .filterIsInstance<RecordsFilter.ManuallyFilteredItem.Running>()
        val manuallyFilteredMultitask = manuallyFilteredItems
            .filterIsInstance<RecordsFilter.ManuallyFilteredItem.Multitask>()

        val manuallyFilteredRecords = if (manuallyFilteredTracked.size > 10) {
            recordInteractor.getAll()
        } else {
            manuallyFilteredTracked.map { it.id }
                .mapNotNull { recordInteractor.get(it) }
        }.associateBy { it.id }
        val manuallyFilteredRunningRecords = if (manuallyFilteredRunning.isNotEmpty()) {
            runningRecordInteractor.getAll()
        } else {
            emptyList()
        }.associateBy { it.id }
        val manuallyFilteredMultitaskRecords = if (manuallyFilteredMultitask.size > 10) {
            recordInteractor.getAll()
        } else {
            manuallyFilteredMultitask.map { it.ids }.flatten().distinct()
                .mapNotNull { recordInteractor.get(it) }
        }.associateBy { it.id }

        return@withContext button.let(::listOf) + manuallyFilteredItems
            .mapNotNull { item ->
                when (item) {
                    is RecordsFilter.ManuallyFilteredItem.Tracked -> {
                        val record = manuallyFilteredRecords[item.id]
                            ?: return@mapNotNull null
                        val mapped = recordViewDataMapper.map(
                            record = record,
                            recordTypes = recordTypes,
                            recordTags = recordTags,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            durationFormat = durationFormat,
                            showSeconds = showSeconds,
                        )?.let {
                            recordViewDataMapper.mapFiltered(
                                viewData = it,
                                isDarkTheme = isDarkTheme,
                                isFiltered = false,
                            )
                        } ?: return@mapNotNull null
                        record.timeStarted to mapped
                    }
                    is RecordsFilter.ManuallyFilteredItem.Untracked -> {
                        val mapped = recordViewDataMapper.mapToUntracked(
                            timeStarted = item.range.timeStarted,
                            timeEnded = item.range.timeEnded,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            durationFormat = durationFormat,
                            showSeconds = showSeconds,
                        )
                        item.range.timeStarted to mapped
                    }
                    is RecordsFilter.ManuallyFilteredItem.Running -> {
                        val record = manuallyFilteredRunningRecords[item.id]
                            ?: return@mapNotNull null
                        val mapped = getRunningRecordViewDataMediator.execute(
                            type = recordTypes[record.id] ?: return@mapNotNull null,
                            tags = recordTags,
                            goals = goals[record.id].orEmpty(),
                            record = record,
                            nowIconVisible = true,
                            goalsVisible = false,
                            totalDurationVisible = false,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            durationFormat = durationFormat,
                            showSeconds = showSeconds,
                        )
                        record.timeStarted to mapped
                    }
                    is RecordsFilter.ManuallyFilteredItem.Multitask -> {
                        val records = item.ids
                            .mapNotNull { manuallyFilteredMultitaskRecords[it] }
                            .takeUnless { it.isEmpty() }
                            ?: return@mapNotNull null
                        val record = MultitaskRecord(records)
                        val mapped = multitaskRecordViewDataMapper.map(
                            multitaskRecord = record,
                            recordTypes = recordTypes,
                            recordTags = recordTags,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            durationFormat = durationFormat,
                            showSeconds = showSeconds,
                        )
                        record.timeStarted to mapped
                    }
                }
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
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

        result += EmptySpaceViewData(
            id = 1,
            width = EmptySpaceViewData.ViewDimension.MatchParent,
            height = EmptySpaceViewData.ViewDimension.ExactSizeDp(6),
        )
        result += dayOfWeekViewDataMapper.mapViewData(
            selectedDaysOfWeek = selectedDays,
            isDarkTheme = isDarkTheme,
            firstDayOfWeek = firstDayOfWeek,
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

    // TODO FILTER add to backup?
    // TODO FILTER handle activity/category/tag removal?
    // TODO FILTER selectable filter on top, change text "Filter" to star icon.
    // TODO FILTER add icons to activities/tags.
    // TODO FILTER order activities/categories/tags.
    suspend fun getFavouriteFiltersSelectionViewData(
        filters: List<RecordsFilter>,
        extra: RecordsFilterParams,
        isDeleteEnabled: Boolean,
        recordTypes: Map<Long, RecordType>,
        categories: Map<Long, Category>,
        recordTags: Map<Long, RecordTag>,
    ): List<ViewHolderType> {
        val result: MutableList<ViewHolderType> = mutableListOf()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val currentAvailableFilters = filterAvailableFilters(extra, filters)
        val isSaveEnabled = currentAvailableFilters.isNotEmpty()
        val filters = favouriteRecordsFilterInteractor.getAll()

        fun mapActiveColor(isActive: Boolean): Int {
            return if (isActive) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            }
        }

        fun mapFilterText(filter: FavouriteRecordsFilter): CharSequence {
            return filter.filter.sortedBy {
                mapper.mapToViewData(it).let(mapper::mapSortOrder)
            }.map {
                recordsFilterFavouriteViewDataMapper.mapFilterName(
                    filter = it,
                    useMilitaryTime = useMilitaryTime,
                    startOfDayShift = startOfDayShift,
                    firstDayOfWeek = firstDayOfWeek,
                    isDarkTheme = isDarkTheme,
                    recordTypes = recordTypes,
                    categories = categories,
                    recordTags = recordTags,
                )
            }.addBetweenEach {
                "\n"
            }.let { texts ->
                buildSpannedString { texts.forEach(::append) }
            }
        }

        fun mapView(
            isAvailable: Boolean,
            filter: FavouriteRecordsFilter,
        ): ViewHolderType {
            return FavouriteRecordsFilterViewData(
                id = filter.id,
                text = mapFilterText(filter),
                backgroundColor = mapActiveColor(isAvailable),
                isEnabled = isAvailable || isDeleteEnabled,
                isDeleteVisible = isDeleteEnabled,
            )
        }

        result += RecordsFilterButtonViewData(
            type = RecordsFilterButtonViewData.Type.SAVE_FAVOURITE,
            text = resourceRepo.getString(R.string.records_filter_save_filter),
            backgroundColor = mapActiveColor(isSaveEnabled),
            isEnabled = isSaveEnabled,
        )
        if (filters.isNotEmpty()) {
            result += RecordsFilterButtonViewData(
                type = RecordsFilterButtonViewData.Type.DELETE_FAVOURITE,
                text = resourceRepo.getString(R.string.archive_dialog_delete),
                backgroundColor = mapActiveColor(isDeleteEnabled),
                isEnabled = true,
            )
        }
        val (availableFilters, unavailableFilters) = filters
            .sortedByDescending { it.id } // Newest on top.
            .partition { filter ->
                filter.filter.size ==
                    filterAvailableFilters(extra, filter.filter).size
            }
        result += availableFilters.map {
            mapView(
                isAvailable = true,
                filter = it,
            )
        }
        if (unavailableFilters.isNotEmpty()) {
            result += DividerViewData(
                id = "favourite_filters_unavailable_divider".hashCode().toLong(),
            )
            result += HintViewData(
                text = resourceRepo.getString(R.string.records_filter_filter_not_available),
            )
            result += unavailableFilters.map {
                mapView(
                    isAvailable = false,
                    filter = it,
                )
            }
        }

        return result
    }

    private fun mapActivitiesViewData(
        types: List<RecordType>,
        isArchivedShown: Boolean,
        showArchived: Boolean,
        numberOfCards: Int,
        isDarkTheme: Boolean,
        allSelectedTypeIds: List<Long>,
    ): List<RecordTypeViewData> {
        return types.mapNotNull { type ->
            if (!isArchivedShown && type.hidden) {
                return@mapNotNull null
            }
            recordTypeViewDataMapper.mapFiltered(
                recordType = type,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
                isFiltered = type.id !in allSelectedTypeIds,
                checkState = GoalCheckmarkView.CheckState.HIDDEN,
                isComplete = false,
            )
        }.plus(
            if (showArchived) {
                recordTypeViewDataMapper.mapToArchivedItem(
                    isEnabled = isArchivedShown,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                )
            } else {
                null
            },
        )
    }

    private fun mapCategoriesViewData(
        categories: List<Category>,
        isDarkTheme: Boolean,
        selectedCategoryIds: List<Long>,
        selectedCategoryItems: List<RecordsFilter.CategoryItem>,
        addUncategorizedItem: Boolean,
    ): List<CategoryViewData.Category> {
        return categories
            .map { category ->
                categoryViewDataMapper.mapCategory(
                    category = category,
                    isDarkTheme = isDarkTheme,
                    isFiltered = category.id !in selectedCategoryIds,
                )
            }
            .plus(
                if (addUncategorizedItem) {
                    categoryViewDataMapper.mapToUncategorizedItem(
                        isFiltered = !selectedCategoryItems.hasUncategorizedItem(),
                        isDarkTheme = isDarkTheme,
                    )
                } else {
                    null
                },
            )
    }

    private fun mapTagsViewData(
        isArchivedShown: Boolean,
        selectedTagIds: List<Long>,
        selectableTags: List<RecordTag>,
        typesMap: Map<Long, RecordType>,
        isDarkTheme: Boolean,
        showArchived: Boolean,
        hasUntaggedItem: Boolean,
    ): List<CategoryViewData> {
        return selectableTags
            .mapNotNull { tag ->
                if (!isArchivedShown && tag.archived) {
                    return@mapNotNull null
                }
                categoryViewDataMapper.mapRecordTag(
                    tag = tag,
                    types = typesMap,
                    isDarkTheme = isDarkTheme,
                    isFiltered = tag.id !in selectedTagIds,
                )
            }
            .takeUnless { selectableTags.isEmpty() }
            ?.plus(
                categoryViewDataMapper.mapToUntaggedItem(
                    isFiltered = !hasUntaggedItem,
                    isDarkTheme = isDarkTheme,
                ),
            )
            ?.plus(
                if (showArchived) {
                    categoryViewDataMapper.mapToTagArchiveItem(
                        isEnabled = isArchivedShown,
                        isDarkTheme = isDarkTheme,
                    )
                } else {
                    null
                },
            )
            .orEmpty()
    }
}