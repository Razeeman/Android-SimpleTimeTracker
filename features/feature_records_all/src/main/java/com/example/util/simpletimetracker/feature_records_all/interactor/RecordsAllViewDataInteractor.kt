package com.example.util.simpletimetracker.feature_records_all.interactor

import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.mapper.DateDividerViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.domain.model.RunningRecord
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
    private val getRunningRecordViewDataMediator: GetRunningRecordViewDataMediator,
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

                Triple(
                    record.timeStarted,
                    record.duration,
                    viewData,
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