package com.example.util.simpletimetracker.feature_records_filter.interactor

import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.RecordFilterViewData
import com.example.util.simpletimetracker.feature_records_filter.R
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterCommentViewData
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterViewDataMapper
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectionState
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecordsFilterViewDataInteractor @Inject constructor(
    private val recordFilterInteractor: RecordFilterInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val mapper: RecordsFilterViewDataMapper,
    private val colorMapper: ColorMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val resourceRepo: ResourceRepo,
) {

    suspend fun getViewData(
        filters: List<RecordsFilter>,
    ): RecordsFilterSelectedRecordsViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
        val recordTypes = recordTypeInteractor.getAll().associateBy { it.id }
        val recordTags = recordTagInteractor.getAll()
        val records = if (filters.isEmpty()) {
            emptyList()
        } else {
            recordFilterInteractor.getByFilter(filters)
        }

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
            selectedRecordsCount = mapper.mapRecordsCount(count),
            recordsViewData = viewData,
        )
    }

    suspend fun getFiltersViewData(
        selectionState: RecordsFilterSelectionState,
        filters: List<RecordsFilter>,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

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
            val active = enabled || (selectionState as? RecordsFilterSelectionState.Visible)?.type == type

            RecordFilterViewData(
                type = type,
                name = if (filter != null) {
                    mapper.mapActiveFilterName(filter)
                } else {
                    mapper.mapInactiveFilterName(type)
                },
                color = if (active) {
                    colorMapper.toActiveColor(isDarkTheme)
                } else {
                    colorMapper.toInactiveColor(isDarkTheme)
                },
                removeBtnVisible = enabled,
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
}