package com.example.util.simpletimetracker.feature_records_all.interactor

import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.mapper.DateDividerViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_records_all.model.RecordsAllSortOrder
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecordsAllViewDataInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordFilterInteractor: RecordFilterInteractor,
    private val recordViewDataMapper: RecordViewDataMapper,
    private val dateDividerViewDataMapper: DateDividerViewDataMapper,
) {

    suspend fun getViewData(
        filter: List<RecordsFilter>,
        sortOrder: RecordsAllSortOrder,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
        val recordTypes = recordTypeInteractor.getAll().associateBy { it.id }
        val recordTags = recordTagInteractor.getAll()

        // Show empty records if no filters other than date.
        val records = filter
            .takeUnless { it.none { filter -> filter !is RecordsFilter.Date } }
            .orEmpty()
            .let { recordFilterInteractor.getByFilter(it) }

        return@withContext records
            .mapNotNull { record ->
                Triple(
                    record.timeStarted,
                    record.timeEnded - record.timeStarted,
                    recordViewDataMapper.map(
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
                    dateDividerViewDataMapper.addDateViewData(viewData)
                } else {
                    viewData.map { it.second }
                }
            }
            .ifEmpty { listOf(recordViewDataMapper.mapToEmpty()) }
    }
}