package com.example.util.simpletimetracker.feature_records_filter.interactor

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.DateDividerViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
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
import com.example.util.simpletimetracker.domain.extension.hasUncategorizedItem
import com.example.util.simpletimetracker.domain.extension.hasUntaggedItem
import com.example.util.simpletimetracker.domain.extension.hasUntrackedFilter
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.RecordFilterViewData
import com.example.util.simpletimetracker.feature_records_filter.R
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterButtonViewData
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterCommentViewData
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterDayOfWeekViewData
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterRangeViewData
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterViewDataMapper
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectionState
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecordsFilterViewDataInteractor @Inject constructor(
    private val recordFilterInteractor: RecordFilterInteractor,
    private val recordInteractor: RecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val mapper: RecordsFilterViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val recordViewDataMapper: RecordViewDataMapper,
    private val getRunningRecordViewDataMediator: GetRunningRecordViewDataMediator,
    private val dateDividerViewDataMapper: DateDividerViewDataMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
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
            firstDayOfWeek = firstDayOfWeek
        )
    }

    suspend fun getRecordsViewData(
        extra: RecordsFilterParams,
        filters: List<RecordsFilter>,
        recordTypes: Map<Long, RecordType>,
        recordTags: List<RecordTag>,
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
                val mapped = mapFilteredRecord(
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
            .map { record ->
                val type = recordTypes[record.typeId]
                val viewData = if (type != null) {
                    when (record) {
                        is Record -> recordViewDataMapper.map(
                            record = record,
                            recordType = type,
                            recordTags = recordTags.filter { it.id in record.tagIds },
                            timeStarted = record.timeStarted,
                            timeEnded = record.timeEnded,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            useProportionalMinutes = useProportionalMinutes,
                            showSeconds = showSeconds,
                        )
                        is RunningRecord -> getRunningRecordViewDataMediator.execute(
                            type = type,
                            tags = recordTags.filter { it.id in record.tagIds },
                            record = record,
                            nowIconVisible = true,
                            goalsVisible = false,
                            totalDurationVisible = false,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            useProportionalMinutes = useProportionalMinutes,
                            showSeconds = showSeconds,
                        )
                    }
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
                record.timeStarted to viewData
            }
            .also { count = it.size }
            .plus(manuallyFilteredRecords)
            .sortedByDescending { (timeStarted, _) -> timeStarted }
            .let(dateDividerViewDataMapper::addDateViewData)
            .ifEmpty { listOf(recordViewDataMapper.mapToEmpty()) }

        return@withContext RecordsFilterSelectedRecordsViewData(
            isLoading = false,
            selectedRecordsCount = mapper.mapRecordsCount(
                extra = extra,
                count = count,
                filter = finalFilters,
            ),
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
        val hasUntracked = filters.hasUntrackedFilter()

        val availableFilters = listOfNotNull(
            when {
                hasUntracked -> RecordFilterViewData.Type.UNTRACKED
                filters.hasCategoryFilter() -> RecordFilterViewData.Type.CATEGORY
                else -> RecordFilterViewData.Type.ACTIVITY
            },
            RecordFilterViewData.Type.COMMENT.takeUnless { hasUntracked },
            RecordFilterViewData.Type.DATE.takeIf { extra.dateSelectionAvailable },
            RecordFilterViewData.Type.SELECTED_TAGS.takeUnless { hasUntracked },
            RecordFilterViewData.Type.FILTERED_TAGS.takeUnless { hasUntracked },
            RecordFilterViewData.Type.DAYS_OF_WEEK,
            RecordFilterViewData.Type.TIME_OF_DAY,
            RecordFilterViewData.Type.DURATION,
            RecordFilterViewData.Type.MANUALLY_FILTERED.takeIf {
                filters.hasManuallyFiltered() && !hasUntracked
            }
        )

        return@withContext availableFilters.mapIndexed { index, type ->
            val clazz = mapper.mapToClass(type)
            // Only one filter type.
            val filter = filters.filterIsInstance(clazz).firstOrNull()
            val enabled = filter != null
            val selected = (selectionState as? RecordsFilterSelectionState.Visible)
                ?.type == type

            RecordFilterViewData(
                id = index.toLong(),
                type = type,
                name = if (filter != null) {
                    mapper.mapActiveFilterName(filter, useMilitaryTime)
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
                isFiltered = type.id !in allSelectedTypeIds
            )
        }

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
                    isDarkTheme = isDarkTheme
                )
            )
            .orEmpty()

        if (categoriesViewData.isNotEmpty()) {
            HintViewData(resourceRepo.getString(R.string.category_hint)).let(result::add)
            categoriesViewData.let(result::addAll)
            DividerViewData(1).let(result::add)
        }

        if (typesViewData.isNotEmpty()) {
            HintViewData(resourceRepo.getString(R.string.activity_hint)).let(result::add)
            typesViewData.let(result::addAll)
        } else {
            HintViewData(resourceRepo.getString(R.string.record_types_empty)).let(result::add)
        }

        if (extra.untrackedSelectionAvailable) {
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
            RecordFilterViewData.CommentType.NO_COMMENT,
            RecordFilterViewData.CommentType.ANY_COMMENT,
        )

        commentFilters.forEach {
            mapper.mapCommentFilter(
                type = it,
                filters = filters,
                isDarkTheme = isDarkTheme
            ).let(result::add)
        }

        DividerViewData(1).let(result::add)

        val comment = filters
            .getCommentItems()
            .getComments()
            .firstOrNull()
        RecordsFilterCommentViewData(
            id = 1L, // Only one at the time.
            text = comment.orEmpty()
        ).let(result::add)

        return@withContext result
    }

    suspend fun getTagsFilterSelectionViewData(
        type: RecordFilterViewData.Type,
        filters: List<RecordsFilter>,
        types: List<RecordType>,
        recordTypeCategories: List<RecordTypeCategory>,
        recordTags: List<RecordTag>,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val isDarkTheme = prefsInteractor.getDarkMode()
        val typesMap = types.associateBy(RecordType::id)
        val selectedTypes = filters
            .getAllTypeIds(types, recordTypeCategories)
            .takeUnless { it.isEmpty() }
            ?: types.map(RecordType::id)
        val selectedTags: List<RecordsFilter.TagItem> = when (type) {
            RecordFilterViewData.Type.SELECTED_TAGS -> filters.getSelectedTags()
            RecordFilterViewData.Type.FILTERED_TAGS -> filters.getFilteredTags()
            else -> emptyList()
        }
        val selectedTaggedIds: List<Long> = selectedTags.getTaggedIds()

        val recordTagsViewData = recordTags
            .filter { it.typeId in selectedTypes || it.typeId == 0L }
            .sortedBy { tag ->
                val recordType = types.firstOrNull { it.id == tag.typeId } ?: 0
                types.indexOf(recordType)
            }
            .map { tag ->
                categoryViewDataMapper.mapRecordTag(
                    tag = tag,
                    type = typesMap[tag.typeId],
                    isDarkTheme = isDarkTheme,
                    isFiltered = tag.id !in selectedTaggedIds,
                )
            }
            .takeUnless { it.isEmpty() }
            ?.plus(
                categoryViewDataMapper.mapToUntaggedItem(
                    isFiltered = !selectedTags.hasUntaggedItem(),
                    isDarkTheme = isDarkTheme
                )
            )
            .orEmpty()

        if (recordTagsViewData.isNotEmpty()) {
            HintViewData(resourceRepo.getString(R.string.record_tag_hint)).let(result::add)
            recordTagsViewData.let(result::addAll)
        } else {
            HintViewData(resourceRepo.getString(R.string.change_record_categories_empty)).let(result::add)
        }

        return@withContext result
    }

    suspend fun getDateFilterSelectionViewData(
        filters: List<RecordsFilter>,
        defaultRange: Range,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val range = filters.getDate() ?: defaultRange

        return@withContext RecordsFilterRangeViewData(
            id = 1L, // Only one at the time.
            timeStarted = timeMapper.formatDateTimeYear(
                time = range.timeStarted,
                useMilitaryTime = useMilitaryTime,
            ),
            timeEnded = timeMapper.formatDateTimeYear(
                time = range.timeEnded,
                useMilitaryTime = useMilitaryTime,
            ),
            gravity = RecordsFilterRangeViewData.Gravity.CENTER_VERTICAL,
            separatorVisible = false,
        ).let(::listOf)
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
                val mapped = mapFilteredRecord(
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
        val selectedDays = filters.getDaysOfWeek()
        val isDarkTheme = prefsInteractor.getDarkMode()

        return@withContext DayOfWeek.values().map {
            val selected = it in selectedDays
            RecordsFilterDayOfWeekViewData(
                dayOfWeek = it,
                text = timeMapper.toShortDayOfWeekName(it),
                color = if (selected) {
                    colorMapper.toActiveColor(isDarkTheme)
                } else {
                    colorMapper.toInactiveColor(isDarkTheme)
                },
            )
        }
    }

    suspend fun getTimeOfDayFilterSelectionViewData(
        filters: List<RecordsFilter>,
        defaultRange: Range,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val range = filters.getTimeOfDay() ?: defaultRange
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val startOfDay = timeMapper.getStartOfDayTimeStamp()

        return@withContext RecordsFilterRangeViewData(
            id = 1L, // Only one at the time.
            timeStarted = timeMapper.formatTime(
                time = range.timeStarted + startOfDay,
                useMilitaryTime = useMilitaryTime,
                showSeconds = false
            ),
            timeEnded = timeMapper.formatTime(
                time = range.timeEnded + startOfDay,
                useMilitaryTime = useMilitaryTime,
                showSeconds = false
            ),
            gravity = RecordsFilterRangeViewData.Gravity.CENTER,
            separatorVisible = true,
        ).let(::listOf)
    }

    fun getDurationFilterSelectionViewData(
        filters: List<RecordsFilter>,
        defaultRange: Range,
    ): List<ViewHolderType> {
        val range = filters.getDuration() ?: defaultRange

        return RecordsFilterRangeViewData(
            id = 1L, // Only one at the time.
            timeStarted = timeMapper.formatDuration(range.timeStarted / 1000),
            timeEnded = timeMapper.formatDuration(range.timeEnded / 1000),
            gravity = RecordsFilterRangeViewData.Gravity.CENTER,
            separatorVisible = true,
        ).let(::listOf)
    }

    private fun mapFilteredRecord(
        record: Record,
        recordTypes: Map<Long, RecordType>,
        allRecordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
        isFiltered: Boolean,
    ): RecordViewData.Tracked? {
        return recordViewDataMapper.map(
            record = record,
            recordType = recordTypes[record.typeId] ?: return null,
            recordTags = allRecordTags.filter { it.id in record.tagIds },
            timeStarted = record.timeStarted,
            timeEnded = record.timeEnded,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        ).let {
            if (isFiltered) {
                it.copy(color = colorMapper.toFilteredColor(isDarkTheme))
            } else {
                it
            }
        }
    }
}