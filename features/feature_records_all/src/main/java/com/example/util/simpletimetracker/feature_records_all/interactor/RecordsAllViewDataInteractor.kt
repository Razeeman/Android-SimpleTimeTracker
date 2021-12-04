package com.example.util.simpletimetracker.feature_records_all.interactor

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.isNotFiltered
import com.example.util.simpletimetracker.core.interactor.TypesFilterInteractor
import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_records_all.mapper.RecordsAllViewDataMapper
import com.example.util.simpletimetracker.feature_records_all.model.RecordsAllSortOrder
import com.example.util.simpletimetracker.feature_records_all.viewData.RecordsAllDateViewData
import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

class RecordsAllViewDataInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val typesFilterInteractor: TypesFilterInteractor,
    private val recordsAllViewDataMapper: RecordsAllViewDataMapper,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper
) {

    suspend fun getViewData(
        filter: TypesFilterParams,
        sortOrder: RecordsAllSortOrder,
        rangeStart: Long,
        rangeEnd: Long
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val recordTypes = recordTypeInteractor.getAll().map { it.id to it }.toMap()
        val recordTags = recordTagInteractor.getAll()
        val typesSelected = typesFilterInteractor.getTypeIds(filter)
        val records = recordInteractor.getByType(typesSelected)
            .filter { it.isNotFiltered(filter) }
            .let {
                if (rangeStart != 0L && rangeEnd != 0L) {
                    rangeMapper.getRecordsFromRange(it, rangeStart, rangeEnd)
                } else {
                    it
                }
            }

        return withContext(Dispatchers.Default) {
            records
                .mapNotNull { record ->
                    Triple(
                        record.timeStarted,
                        record.timeEnded - record.timeStarted,
                        recordsAllViewDataMapper.map(
                            record = record,
                            recordType = recordTypes[record.typeId] ?: return@mapNotNull null,
                            recordTags = recordTags.filter { it.id in record.tagIds },
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            useProportionalMinutes = useProportionalMinutes
                        )
                    )
                }
                .sortedByDescending { (timeStarted, duration, _) ->
                    when (sortOrder) {
                        RecordsAllSortOrder.TIME_STARTED -> timeStarted
                        RecordsAllSortOrder.DURATION -> duration
                    }
                }
                .map { (timeStarted, _, record) -> timeStarted to record }
                .let { viewData ->
                    if (sortOrder == RecordsAllSortOrder.TIME_STARTED) {
                        addDateViewData(viewData)
                    } else {
                        viewData.map { it.second }
                    }
                }
                .ifEmpty {
                    listOf(recordsAllViewDataMapper.mapToEmpty())
                }
        }
    }

    private fun addDateViewData(viewData: List<Pair<Long, ViewHolderType>>): List<ViewHolderType> {
        val calendar = Calendar.getInstance()
        val newViewData = mutableListOf<ViewHolderType>()
        var previousTimeStarted = 0L

        viewData.forEach { (timeStarted, recordViewData) ->
            synchronized(timeMapper) {
                if (!timeMapper.sameDay(timeStarted, previousTimeStarted, calendar)) {
                    timeMapper.formatDateYear(timeStarted)
                        .let(::RecordsAllDateViewData)
                        .let(newViewData::add)
                }
            }
            previousTimeStarted = timeStarted
            newViewData.add(recordViewData)
        }

        return newViewData
    }
}