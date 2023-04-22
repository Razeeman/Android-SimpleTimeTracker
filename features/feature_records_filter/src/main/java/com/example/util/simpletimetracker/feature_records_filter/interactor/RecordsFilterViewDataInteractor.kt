package com.example.util.simpletimetracker.feature_records_filter.interactor

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.RecordFilterViewData
import com.example.util.simpletimetracker.feature_records_filter.R
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterCommentViewData
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterRangeViewData
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterViewDataMapper
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectionState
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecordsFilterViewDataInteractor @Inject constructor(
    private val recordFilterInteractor: RecordFilterInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val mapper: RecordsFilterViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
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
        filters: List<RecordsFilter>,
    ): RecordsFilterSelectedRecordsViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
        val recordTypes = recordTypeInteractor.getAll().associateBy { it.id }
        val recordTags = recordTagInteractor.getAll()
        val records = recordFilterInteractor.getByFilter(filters)
        val selectedTypeIds = records.map { it.typeId }.toSet()

        val (count, viewData) = withContext(Dispatchers.Default) {
            var count: Int
            val viewData = records
                .mapNotNull { record ->
                    mapper.map(
                        record = record,
                        recordType = recordTypes[record.typeId] ?: return@mapNotNull null,
                        recordTags = recordTags.filter { it.id in record.tagIds },
                        isDarkTheme = isDarkTheme,
                        useMilitaryTime = useMilitaryTime,
                        useProportionalMinutes = useProportionalMinutes,
                        showSeconds = showSeconds,
                    )
                }
                .also {
                    count = it.size
                }
                .ifEmpty {
                    listOf(mapper.mapToEmpty())
                }

            count to viewData
        }

        return RecordsFilterSelectedRecordsViewData(
            selectedRecordsCount = mapper.mapRecordsCount(count, filters),
            recordsViewData = viewData,
            filteredRecordsTypeId = selectedTypeIds.takeIf { it.size == 1 }?.firstOrNull(),
        )
    }

    suspend fun getFiltersViewData(
        selectionState: RecordsFilterSelectionState,
        filters: List<RecordsFilter>,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        val availableFilters = listOf(
            RecordFilterViewData.Type.ACTIVITY,
            RecordFilterViewData.Type.COMMENT,
            RecordFilterViewData.Type.DATE,
            RecordFilterViewData.Type.SELECTED_TAGS,
            RecordFilterViewData.Type.FILTERED_TAGS,
        )

        return availableFilters.map { type ->
            // Only one filter type.
            val clazz = mapper.mapToClass(type)
            val filter = filters.filterIsInstance(clazz).firstOrNull()
            val enabled = filter != null
            // TODO add string translations
            val selected = (selectionState as? RecordsFilterSelectionState.Visible)
                ?.type == type

            RecordFilterViewData(
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
    ): List<ViewHolderType> {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val selectedTypes = filters
            .filterIsInstance<RecordsFilter.Activity>()
            .map { it.typeIds }
            .flatten()

        val typesViewData = types.map { type ->
            recordTypeViewDataMapper.mapFiltered(
                recordType = type,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
                isFiltered = type.id !in selectedTypes
            )
        }

        if (typesViewData.isNotEmpty()) {
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
        val comment = filters
            .filterIsInstance<RecordsFilter.Comment>()
            .firstOrNull()
            ?.comment
            .orEmpty()

        return RecordsFilterCommentViewData(
            id = 1L, // Only one at the time.
            text = comment
        ).let(::listOf)
    }

    suspend fun getTagsFilterSelectionViewData(
        type: RecordFilterViewData.Type,
        filters: List<RecordsFilter>,
        types: List<RecordType>,
        recordTags: List<RecordTag>,
    ): List<ViewHolderType> {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val isDarkTheme = prefsInteractor.getDarkMode()
        val typesMap = types.associateBy { it.id }
        val selectedTypes = filters
            .filterIsInstance<RecordsFilter.Activity>()
            .map { it.typeIds }
            .flatten()
            .takeUnless { it.isEmpty() }
            ?: types.map { it.id }
        val selectedTags: List<RecordsFilter.Tag> = when (type) {
            RecordFilterViewData.Type.SELECTED_TAGS -> {
                filters.filterIsInstance<RecordsFilter.SelectedTags>().map { it.tags }
            }
            RecordFilterViewData.Type.FILTERED_TAGS -> {
                filters.filterIsInstance<RecordsFilter.FilteredTags>().map { it.tags }
            }
            else -> {
                emptyList()
            }
        }.flatten()
        val selectedTaggedIds: List<Long> = selectedTags.filterIsInstance<RecordsFilter.Tag.Tagged>()
            .map { it.tagId }
        val selectedUntaggedIds: List<Long> = selectedTags.filterIsInstance<RecordsFilter.Tag.Untagged>()
            .map { it.typeId }

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

        val range = filters.filterIsInstance<RecordsFilter.Date>()
            .firstOrNull()
            ?.range
            ?: defaultRange

        return RecordsFilterRangeViewData(
            id = 1L, // Only one at the time.
            timeStarted = timeMapper.formatDateTime(
                time = range.timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = false,
            ),
            timeEnded = timeMapper.formatDateTime(
                time = range.timeEnded,
                useMilitaryTime = useMilitaryTime,
                showSeconds = false,
            ),
        ).let(::listOf)
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
}