package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardDoubleViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailClickablePopup
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailClickableTracked
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsDetailStatsInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val resourceRepo: ResourceRepo,
) {

    suspend fun getStatsViewData(
        records: List<RecordBase>,
        compareRecords: List<RecordBase>,
        showComparison: Boolean,
        rangeLength: RangeLength,
        rangePosition: Int,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val durationFormat = prefsInteractor.getDurationFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val types = recordTypeInteractor.getAll()

        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )

        return@withContext mapStatsData(
            records = if (range.isUndefined) {
                records
            } else {
                rangeMapper.getRecordsFromRange(records, range)
                    .map { rangeMapper.clampRecordToRange(it, range) }
            },
            compareRecords = if (range.isUndefined) {
                compareRecords
            } else {
                rangeMapper.getRecordsFromRange(compareRecords, range)
                    .map { rangeMapper.clampRecordToRange(it, range) }
            },
            showComparison = showComparison,
            types = types,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
        )
    }

    fun getEmptyStatsViewData(): List<ViewHolderType> {
        return mapToStatsViewData(
            totalDuration = "",
            compareTotalDuration = "",
            timesTracked = null,
            compareTimesTracked = "",
            timesTrackedIcon = null,
            shortestRecord = "",
            compareShortestRecord = "",
            shortestRecordDate = null,
            averageRecord = "",
            compareAverageRecord = "",
            longestRecord = "",
            compareLongestRecord = "",
            longestRecordDate = null,
            firstRecord = "",
            compareFirstRecord = "",
            lastRecord = "",
            compareLastRecord = "",
            firstRecordClickMessage = null,
            lastRecordClickMessage = null,
        )
    }

    private fun mapStatsData(
        records: List<RecordBase>,
        compareRecords: List<RecordBase>,
        showComparison: Boolean,
        types: List<RecordType>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
    ): List<ViewHolderType> {
        val typesMap = types.associateBy { it.id }
        val recordsSorted = records.sortedBy { it.timeStarted }
        val durations = records.map(RecordBase::duration)

        val compareRecordsSorted = compareRecords.sortedBy { it.timeStarted }
        val compareDurations = compareRecords.map(RecordBase::duration)

        val shortestRecord = records.minByOrNull(RecordBase::duration)
        val longestRecord = records.maxByOrNull(RecordBase::duration)

        val firstRecordData = recordsSorted.firstOrNull()?.timeStarted
        val lastRecordData = recordsSorted.lastOrNull()?.timeEnded
        val compareFirstRecordData = compareRecordsSorted.firstOrNull()?.timeStarted
        val compareLastRecordData = compareRecordsSorted.lastOrNull()?.timeEnded
        val recordSpanData = if (firstRecordData != null && lastRecordData != null) {
            lastRecordData - firstRecordData
        } else {
            null
        }

        val emptyValue by lazy {
            resourceRepo.getString(R.string.statistics_detail_empty)
        }
        val recordsAllIcon = StatisticsDetailCardInternalViewData.Icon(
            iconDrawable = R.drawable.arrow_right,
            iconColor = resourceRepo.getThemedAttr(R.attr.appInactiveColor, isDarkTheme),
        )

        fun formatInterval(value: Long?): String {
            value ?: return emptyValue
            return timeMapper.formatInterval(
                interval = value,
                forceSeconds = showSeconds,
                durationFormat = durationFormat,
            )
        }

        fun formatDateTimeYear(value: Long?): String {
            value ?: return emptyValue
            return timeMapper.formatDateTimeYear(value, useMilitaryTime)
        }

        fun getAverage(values: List<Long>): Long? {
            return if (values.isNotEmpty()) {
                values.sum() / values.size
            } else {
                null
            }
        }

        fun processComparisonString(value: String): String {
            return value
                .takeIf { showComparison }
                ?.let { "($it)" }
                .orEmpty()
        }

        fun processLengthHint(value: RecordBase): String {
            val result = StringBuilder()
            value.typeIds
                .mapNotNull(typesMap::get)
                .map(RecordType::name)
                .takeUnless { it.isEmpty() }
                ?.joinToString()
                ?.let {
                    result.append(it)
                    result.append("\n")
                }
            value.timeStarted
                .let(::formatDateTimeYear)
                .let { result.append(it) }

            return result.toString()
        }

        fun getTimeSinceMessage(timestamp: Long, span: Long?): String {
            val result = StringBuilder()
            result.append(resourceRepo.getString(R.string.statistics_detail_time_since))
            result.append("\n")
            val interval = System.currentTimeMillis() - timestamp
            val timeSince = formatInterval(interval)
            val timeSinceInDays = timeMapper.formatInterval(
                interval = interval,
                forceSeconds = showSeconds,
                durationFormat = DurationFormat.DAYS,
            )
            result.append(timeSince)
            if (timeSince != timeSinceInDays) {
                result.append("\n")
                result.append("($timeSinceInDays)")
            }
            if (span != null) {
                result.append("\n\n")
                result.append(resourceRepo.getString(R.string.statistics_detail_record_span))
                result.append("\n")
                val spanData = formatInterval(span)
                val spanDataInDays = timeMapper.formatInterval(
                    interval = span,
                    forceSeconds = showSeconds,
                    durationFormat = DurationFormat.DAYS,
                )
                result.append(spanData)
                if (spanData != spanDataInDays) {
                    result.append("\n")
                    result.append("($spanDataInDays)")
                }
            }

            return result.toString()
        }

        return mapToStatsViewData(
            totalDuration = durations.sum()
                .let(::formatInterval),
            compareTotalDuration = compareDurations.sum()
                .let(::formatInterval)
                .let(::processComparisonString),
            timesTracked = records.size,
            compareTimesTracked = compareRecords.size.toString()
                .let(::processComparisonString),
            timesTrackedIcon = recordsAllIcon,
            shortestRecord = shortestRecord?.duration
                .let(::formatInterval),
            compareShortestRecord = compareDurations.minOrNull()
                .let(::formatInterval)
                .let(::processComparisonString),
            shortestRecordDate = shortestRecord
                ?.let(::processLengthHint),
            averageRecord = getAverage(durations)
                .let(::formatInterval),
            compareAverageRecord = getAverage(compareDurations)
                .let(::formatInterval)
                .let(::processComparisonString),
            longestRecord = longestRecord?.duration
                .let(::formatInterval),
            compareLongestRecord = compareDurations.maxOrNull()
                .let(::formatInterval)
                .let(::processComparisonString),
            longestRecordDate = longestRecord
                ?.let(::processLengthHint),
            firstRecord = firstRecordData
                .let(::formatDateTimeYear),
            compareFirstRecord = compareFirstRecordData
                .let(::formatDateTimeYear)
                .let(::processComparisonString),
            lastRecord = lastRecordData
                .let(::formatDateTimeYear),
            compareLastRecord = compareLastRecordData
                .let(::formatDateTimeYear)
                .let(::processComparisonString),
            firstRecordClickMessage = firstRecordData?.let {
                getTimeSinceMessage(it, recordSpanData)
            },
            lastRecordClickMessage = lastRecordData?.let {
                getTimeSinceMessage(it, recordSpanData)
            },
        )
    }

    private fun mapToStatsViewData(
        totalDuration: String,
        compareTotalDuration: String,
        timesTracked: Int?,
        compareTimesTracked: String,
        timesTrackedIcon: StatisticsDetailCardInternalViewData.Icon?,
        shortestRecord: String,
        compareShortestRecord: String,
        shortestRecordDate: String?,
        averageRecord: String,
        compareAverageRecord: String,
        longestRecord: String,
        compareLongestRecord: String,
        longestRecordDate: String?,
        firstRecord: String,
        compareFirstRecord: String,
        lastRecord: String,
        compareLastRecord: String,
        firstRecordClickMessage: String?,
        lastRecordClickMessage: String?,
    ): List<ViewHolderType> {
        val totalDuration = listOf(
            StatisticsDetailCardInternalViewData(
                value = totalDuration,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = compareTotalDuration,
                description = resourceRepo.getString(R.string.statistics_detail_total_duration),
                accented = true,
                titleTextSizeSp = 22,
            ),
        )
        val timesTracked = listOf(
            StatisticsDetailCardInternalViewData(
                value = timesTracked?.toString() ?: "",
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = compareTimesTracked,
                description = resourceRepo.getQuantityString(
                    R.plurals.statistics_detail_times_tracked, timesTracked.orZero(),
                ),
                icon = timesTrackedIcon,
                clickable = StatisticsDetailClickableTracked,
                accented = true,
                titleTextSizeSp = 22,
            ),
        )
        val averageRecord = listOf(
            StatisticsDetailCardInternalViewData(
                value = shortestRecord,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = compareShortestRecord,
                description = resourceRepo.getString(R.string.statistics_detail_shortest_record),
                clickable = shortestRecordDate?.let { StatisticsDetailClickablePopup(it) },
            ),
            StatisticsDetailCardInternalViewData(
                value = averageRecord,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = compareAverageRecord,
                description = resourceRepo.getString(R.string.statistics_detail_average_record),
            ),
            StatisticsDetailCardInternalViewData(
                value = longestRecord,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = compareLongestRecord,
                description = resourceRepo.getString(R.string.statistics_detail_longest_record),
                clickable = longestRecordDate?.let { StatisticsDetailClickablePopup(it) },
            ),
        )
        val datesTracked = listOf(
            StatisticsDetailCardInternalViewData(
                value = firstRecord,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = compareFirstRecord,
                description = resourceRepo.getString(R.string.statistics_detail_first_record),
                clickable = firstRecordClickMessage?.let { StatisticsDetailClickablePopup(it) },
            ),
            StatisticsDetailCardInternalViewData(
                value = lastRecord,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = compareLastRecord,
                description = resourceRepo.getString(R.string.statistics_detail_last_record),
                clickable = lastRecordClickMessage?.let { StatisticsDetailClickablePopup(it) },
            ),
        )

        return listOf(
            StatisticsDetailCardDoubleViewData(
                block = StatisticsDetailBlock.Total,
                first = totalDuration,
                second = timesTracked,
            ),
            StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.Average,
                title = resourceRepo.getString(R.string.statistics_detail_record_length),
                marginTopDp = 4,
                data = averageRecord,
            ),
            StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.Dates,
                title = resourceRepo.getString(R.string.statistics_detail_record_time),
                marginTopDp = 4,
                data = datesTracked,
            ),
        )
    }
}