package com.example.util.simpletimetracker.feature_records.interactor

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_records.mapper.RecordViewDataMapper
import java.util.Calendar
import javax.inject.Inject

class RecordsViewDataInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordViewDataMapper: RecordViewDataMapper
) {

    suspend fun getViewData(shift: Int): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val recordTypes = recordTypeInteractor.getAll().map { it.id to it }.toMap()
        val (rangeStart, rangeEnd) = getRange(shift)
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
                record.timeStarted to recordViewDataMapper.map(
                    record = record,
                    recordType = recordType,
                    rangeStart = rangeStart,
                    rangeEnd = rangeEnd,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime
                )
            }
            .let { trackedRecords ->
                if (!prefsInteractor.getShowUntrackedInRecords()) return@let trackedRecords

                recordInteractor.getUntrackedFromRange(rangeStart, rangeEnd)
                    .filter {
                        // Filter only untracked records that are longer than a minute
                        (it.timeEnded - it.timeStarted) >= UNTRACKED_RECORD_LENGTH_LIMIT
                    }
                    .map { untrackedRecord ->
                        untrackedRecord.timeStarted to recordViewDataMapper.mapToUntracked(
                            record = untrackedRecord,
                            rangeStart = rangeStart,
                            rangeEnd = rangeEnd,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime
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

    private fun getRange(shift: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DATE, shift)
        }
        val rangeStart = calendar.timeInMillis
        val rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis

        return rangeStart to rangeEnd
    }

    companion object {
        private const val UNTRACKED_RECORD_LENGTH_LIMIT: Long = 60 * 1000L // 1 min
    }
}