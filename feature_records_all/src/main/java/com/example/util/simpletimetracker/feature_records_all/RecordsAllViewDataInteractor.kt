package com.example.util.simpletimetracker.feature_records_all

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecordsAllViewDataInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordsAllViewDataMapper: RecordsAllViewDataMapper
) {

    suspend fun getViewData(sortOrder: RecordsAllSortOrder, typeId: Long): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val recordTypes = recordTypeInteractor.getAll()
            .map { it.id to it }
            .toMap()
        val records = recordInteractor.getAll()
            .filter { it.typeId == typeId }

        return withContext(Dispatchers.Default) {
            records
                .mapNotNull { record ->
                    recordTypes[record.typeId]?.let { type -> record to type }
                }
                .map { (record, recordType) ->
                    Triple(
                        record.timeStarted,
                        record.timeEnded - record.timeStarted,
                        recordsAllViewDataMapper.map(record, recordType, isDarkTheme)
                    )
                }
                .sortedByDescending { (timeStarted, duration, _) ->
                    when (sortOrder) {
                        RecordsAllSortOrder.TIME_STARTED -> timeStarted
                        RecordsAllSortOrder.DURATION -> duration
                    }
                }
                .map { (_, _, records) -> records }
                .ifEmpty {
                    listOf(recordsAllViewDataMapper.mapToEmpty())
                }
        }
    }
}