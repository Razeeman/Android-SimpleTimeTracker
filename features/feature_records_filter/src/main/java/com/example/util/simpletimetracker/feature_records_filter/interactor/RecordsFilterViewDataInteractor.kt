package com.example.util.simpletimetracker.feature_records_filter.interactor

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
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
import com.example.util.simpletimetracker.domain.extension.getComment
import com.example.util.simpletimetracker.domain.extension.getDate
import com.example.util.simpletimetracker.domain.extension.getFilteredTags
import com.example.util.simpletimetracker.domain.extension.getManuallyFilteredRecordIds
import com.example.util.simpletimetracker.domain.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.extension.getTaggedIds
import com.example.util.simpletimetracker.domain.extension.getUntaggedIds
import com.example.util.simpletimetracker.domain.extension.hasCategoryFilter
import com.example.util.simpletimetracker.domain.extension.hasManuallyFiltered
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.RecordFilterViewData
import com.example.util.simpletimetracker.feature_records_filter.R
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterCommentViewData
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterRangeViewData
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterViewDataMapper
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectionState
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import java.util.Calendar
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
    ): RecordsFilterSelectedRecordsViewData {
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
        val selectedTypeIds = records.map { it.typeId }.toSet()
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

        val (count, viewData) = withContext(Dispatchers.Default) {
            var count: Int
            val viewData = records
                .mapNotNull { record ->
                    record.timeStarted to recordViewDataMapper.map(
                        record = record,
                        recordType = recordTypes[record.typeId] ?: return@mapNotNull null,
                        recordTags = recordTags.filter { it.id in record.tagIds },
                        timeStarted = record.timeStarted,
                        timeEnded = record.timeEnded,
                        isDarkTheme = isDarkTheme,
                        useMilitaryTime = useMilitaryTime,
                        useProportionalMinutes = useProportionalMinutes,
                        showSeconds = showSeconds,
                    )
                }
                .also { count = it.size }
                .plus(manuallyFilteredRecords)
                .sortedByDescending { (timeStarted, _) -> timeStarted }
                .let(dateDividerViewDataMapper::addDateViewData)
                .ifEmpty { listOf(recordViewDataMapper.mapToEmpty()) }

            count to viewData
        }

        return RecordsFilterSelectedRecordsViewData(
            selectedRecordsCount = mapper.mapRecordsCount(
                extra = extra,
                count = count,
                filter = finalFilters,
            ),
            recordsViewData = viewData,
            filteredRecordsTypeId = selectedTypeIds.takeIf { it.size == 1 }?.firstOrNull(),
        )
    }

    suspend fun getFiltersViewData(
        extra: RecordsFilterParams,
        selectionState: RecordsFilterSelectionState,
        filters: List<RecordsFilter>,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        val availableFilters = listOfNotNull(
            if (filters.hasCategoryFilter()) {
                RecordFilterViewData.Type.CATEGORY
            } else {
                RecordFilterViewData.Type.ACTIVITY
            },
            RecordFilterViewData.Type.COMMENT,
            RecordFilterViewData.Type.DATE.takeIf {
                extra.dateSelectionAvailable
            },
            RecordFilterViewData.Type.SELECTED_TAGS,
            RecordFilterViewData.Type.FILTERED_TAGS,
            RecordFilterViewData.Type.MANUALLY_FILTERED.takeIf {
                filters.hasManuallyFiltered()
            }
        )

        return availableFilters.mapIndexed { index, type ->
            val clazz = mapper.mapToClass(type)
            // Only one filter type.
            val filter = filters.filterIsInstance(clazz).firstOrNull()
            val enabled = filter != null
            // TODO add string translations
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
        filters: List<RecordsFilter>,
        types: List<RecordType>,
        recordTypeCategories: List<RecordTypeCategory>,
        categories: List<Category>,
    ): List<ViewHolderType> {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val selectedCategoryIds: List<Long> = filters.getCategoryIds()
        val allSelectedTypeIds: List<Long> = filters.getAllTypeIds(recordTypeCategories)

        val typesViewData = types.map { type ->
            recordTypeViewDataMapper.mapFiltered(
                recordType = type,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
                isFiltered = type.id !in allSelectedTypeIds
            )
        }

        val categoriesViewData = categories.map { category ->
            categoryViewDataMapper.mapCategory(
                category = category,
                isDarkTheme = isDarkTheme,
                isFiltered = category.id !in selectedCategoryIds,
            )
        }

        if (categoriesViewData.isNotEmpty()) {
            HintViewData(resourceRepo.getString(R.string.category_hint)).let(result::add)
            categoriesViewData.let(result::addAll)
        }

        if (typesViewData.isNotEmpty()) {
            if (categoriesViewData.isNotEmpty()) {
                DividerViewData(1).let(result::add)
            }
            HintViewData(resourceRepo.getString(R.string.activity_hint)).let(result::add)
            typesViewData.let(result::addAll)
        } else {
            HintViewData(resourceRepo.getString(R.string.record_types_empty)).let(result::add)
        }

        return result
    }

    fun getCommentFilterSelectionViewData(
        filters: List<RecordsFilter>,
    ): List<ViewHolderType> {
        return RecordsFilterCommentViewData(
            id = 1L, // Only one at the time.
            text = filters.getComment().orEmpty()
        ).let(::listOf)
    }

    suspend fun getTagsFilterSelectionViewData(
        type: RecordFilterViewData.Type,
        filters: List<RecordsFilter>,
        types: List<RecordType>,
        recordTypeCategories: List<RecordTypeCategory>,
        recordTags: List<RecordTag>,
    ): List<ViewHolderType> {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val isDarkTheme = prefsInteractor.getDarkMode()
        val typesMap = types.associateBy(RecordType::id)
        val selectedTypes = filters
            .getAllTypeIds(recordTypeCategories)
            .takeUnless { it.isEmpty() }
            ?: types.map(RecordType::id)
        val selectedTags: List<RecordsFilter.Tag> = when (type) {
            RecordFilterViewData.Type.SELECTED_TAGS -> filters.getSelectedTags()
            RecordFilterViewData.Type.FILTERED_TAGS -> filters.getFilteredTags()
            else -> emptyList()
        }
        val selectedTaggedIds: List<Long> = selectedTags.getTaggedIds()
        val selectedUntaggedIds: List<Long> = selectedTags.getUntaggedIds()

        val recordTagsViewData = selectedTypes
            .map { typeId ->
                typeId to recordTags.filter { it.typeId == typeId }
            }
            .mapNotNull { (typeId, tags) ->
                typeId to mapUntaggedTags(
                    selectedIds = selectedUntaggedIds,
                    type = typesMap[typeId] ?: return@mapNotNull null,
                    typeId = typeId,
                    isDarkTheme = isDarkTheme,
                ) + mapTaggedTags(
                    selectedIds = selectedTaggedIds,
                    tags = tags,
                    typesMap = typesMap,
                    isDarkTheme = isDarkTheme
                )
            }
            .map { (_, tags) -> tags }
            .flatten()
            .toList()
            .takeUnless { it.isEmpty() }
            ?.plus(
                mapTaggedTags(
                    selectedIds = selectedTaggedIds,
                    tags = recordTags.filter { it.typeId == 0L },
                    typesMap = typesMap,
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

        return result
    }

    suspend fun getDateFilterSelectionViewData(
        filters: List<RecordsFilter>,
        defaultRange: Range,
    ): List<ViewHolderType> {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val range = filters.getDate() ?: defaultRange

        return RecordsFilterRangeViewData(
            id = 1L, // Only one at the time.
            timeStarted = timeMapper.formatDateTimeYear(
                time = range.timeStarted,
                useMilitaryTime = useMilitaryTime,
            ),
            timeEnded = timeMapper.formatDateTimeYear(
                time = range.timeEnded,
                useMilitaryTime = useMilitaryTime,
            ),
        ).let(::listOf)
    }

    suspend fun getManualFilterSelectionViewData(
        filters: List<RecordsFilter>,
        recordTypes: Map<Long, RecordType>,
        recordTags: List<RecordTag>,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()

        return filters
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

    private fun mapUntaggedTags(
        selectedIds: List<Long>,
        type: RecordType,
        typeId: Long,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        return categoryViewDataMapper.mapRecordTagUntagged(
            type = type,
            isDarkTheme = isDarkTheme,
            isFiltered = typeId !in selectedIds,
        ).let(::listOf)
    }

    private fun mapTaggedTags(
        selectedIds: List<Long>,
        tags: List<RecordTag>,
        typesMap: Map<Long, RecordType>,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        return tags.map { tag ->
            categoryViewDataMapper.mapRecordTag(
                tag = tag,
                type = typesMap[tag.typeId],
                isDarkTheme = isDarkTheme,
                isFiltered = tag.id !in selectedIds,
            )
        }
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