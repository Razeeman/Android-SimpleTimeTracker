package com.example.util.simpletimetracker.feature_records_filter.interactor

import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.RecordFilterViewData
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterViewDataMapper
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
) {

    suspend fun getViewData(
        filters: List<RecordsFilter>,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
        val recordTypes = recordTypeInteractor.getAll().associateBy { it.id }
        val recordTags = recordTagInteractor.getAll()
        val records = recordFilterInteractor.getByFilter(filters)

        return withContext(Dispatchers.Default) {
            records
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
                .ifEmpty {
                    listOf(mapper.mapToEmpty())
                }
        }
    }

    suspend fun getFiltersViewData(
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

            RecordFilterViewData(
                type = type,
                name = if (filter != null) {
                    mapper.mapActiveFilterName(filter)
                } else {
                    mapper.mapInactiveFilterName(type)
                },
                color = if (enabled) {
                    colorMapper.toActiveColor(isDarkTheme)
                } else {
                    colorMapper.toInactiveColor(isDarkTheme)
                },
                enabled = enabled,
            )
        }
    }
}