package com.example.util.simpletimetracker.feature_records.interactor

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_records.mapper.RecordViewDataMapper
import javax.inject.Inject

class RecordsViewDataInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordViewDataMapper: RecordViewDataMapper
) {

    suspend fun getViewData(rangeStart: Long, rangeEnd: Long): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val recordTypes = recordTypeInteractor.getAll()
            .map { it.id to it }
            .toMap()
        val records = if (rangeStart != 0L && rangeEnd != 0L) {
            recordInteractor.getFromRange(rangeStart, rangeEnd)
        } else {
            recordInteractor.getAll()
        }

        return records
            .mapNotNull { record ->
                recordTypes[record.typeId]?.let { type -> record to type }
            }
            .map { (record, recordType) ->
                record.timeStarted to
                    recordViewDataMapper.map(record, recordType, rangeStart, rangeEnd, isDarkTheme)
            }
            .let { trackedRecords ->
                if (!prefsInteractor.getShowUntrackedInRecords()) return@let trackedRecords

                recordInteractor.getUntrackedFromRange(rangeStart, rangeEnd)
                    .filter {
                        // Filter only untracked records that are longer than a minute
                        (it.timeEnded - it.timeStarted) >= UNTRACKED_RECORD_LENGTH_LIMIT
                    }
                    .map { untrackedRecord ->
                        untrackedRecord.timeStarted to
                            recordViewDataMapper.mapToUntracked(
                                untrackedRecord,
                                rangeStart,
                                rangeEnd,
                                isDarkTheme
                            )
                    }
                    .let { untrackedRecords -> trackedRecords + untrackedRecords }
            }
            .sortedByDescending { (timeStarted, _) -> timeStarted }
            .map { (_, records) -> records }
            .ifEmpty {
                return listOf(recordViewDataMapper.mapToEmpty())
            }
    }

    companion object {
        private const val UNTRACKED_RECORD_LENGTH_LIMIT: Long = 60 * 1000L // 1 min
    }
}