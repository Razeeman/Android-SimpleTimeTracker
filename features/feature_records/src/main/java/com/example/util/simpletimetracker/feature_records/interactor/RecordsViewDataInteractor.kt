package com.example.util.simpletimetracker.feature_records.interactor

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_records.mapper.RecordsViewDataMapper
import java.util.Calendar
import javax.inject.Inject

class RecordsViewDataInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordsViewDataMapper: RecordsViewDataMapper
) {

    suspend fun getViewData(shift: Int): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val recordTypes = recordTypeInteractor.getAll().map { it.id to it }.toMap()
        val recordTags = recordTagInteractor.getAll().map { it.id to it }.toMap()
        val (rangeStart, rangeEnd) = getRange(shift)
        val records = if (rangeStart != 0L && rangeEnd != 0L) {
            recordInteractor.getFromRange(rangeStart, rangeEnd)
        } else {
            recordInteractor.getAll()
        }

        return records
            .mapNotNull { record ->
                record.timeStarted to recordsViewDataMapper.map(
                    record = record,
                    recordType = recordTypes[record.typeId] ?: return@mapNotNull null,
                    recordTag = recordTags[record.tagId],
                    rangeStart = rangeStart,
                    rangeEnd = rangeEnd,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    useProportionalMinutes = useProportionalMinutes
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
                        untrackedRecord.timeStarted to recordsViewDataMapper.mapToUntracked(
                            record = untrackedRecord,
                            rangeStart = rangeStart,
                            rangeEnd = rangeEnd,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            useProportionalMinutes = useProportionalMinutes
                        )
                    }
                    .let { untrackedRecords -> trackedRecords + untrackedRecords }
            }
            .sortedByDescending { (timeStarted, _) -> timeStarted }
            .map { (_, records) -> records }
            .let {
                if (it.isEmpty()) {
                    listOf(recordsViewDataMapper.mapToEmpty())
                } else {
                    it + recordsViewDataMapper.mapToHint()
                }
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