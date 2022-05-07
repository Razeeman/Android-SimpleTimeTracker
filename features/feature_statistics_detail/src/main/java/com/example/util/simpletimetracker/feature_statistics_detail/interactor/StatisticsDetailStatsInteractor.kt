package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.mapper.StatisticsMapper
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.statisticsTag.StatisticsTagViewData
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class StatisticsDetailStatsInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val timeMapper: TimeMapper,
    private val statisticsMapper: StatisticsMapper,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val rangeMapper: RangeMapper,
    private val resourceRepo: ResourceRepo,
) {

    suspend fun getStatsViewData(
        records: List<Record>,
        compareRecords: List<Record>,
        showComparison: Boolean,
        rangeLength: RangeLength,
        rangePosition: Int,
    ): StatisticsDetailStatsViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val types = recordTypeInteractor.getAll()
        val tags = recordTagInteractor.getAll()

        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift
        )

        return mapStatsData(
            records = if (range.first == 0L && range.second == 0L) {
                records
            } else {
                rangeMapper.getRecordsFromRange(
                    records = records,
                    rangeStart = range.first,
                    rangeEnd = range.second
                )
            },
            compareRecords = if (range.first == 0L && range.second == 0L) {
                compareRecords
            } else {
                rangeMapper.getRecordsFromRange(
                    records = compareRecords,
                    rangeStart = range.first,
                    rangeEnd = range.second
                )
            },
            showComparison = showComparison,
            types = types,
            tags = tags,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            useProportionalMinutes = useProportionalMinutes
        )
    }

    fun getEmptyStatsViewData(): StatisticsDetailStatsViewData {
        return mapToStatsViewData(
            totalDuration = "",
            compareTotalDuration = "",
            timesTracked = null,
            compareTimesTracked = "",
            timesTrackedIcon = null,
            shortestRecord = "",
            compareShortestRecord = "",
            averageRecord = "",
            compareAverageRecord = "",
            longestRecord = "",
            compareLongestRecord = "",
            firstRecord = "",
            compareFirstRecord = "",
            lastRecord = "",
            compareLastRecord = "",
            tagSplitData = emptyList()
        )
    }

    private fun mapStatsData(
        records: List<Record>,
        compareRecords: List<Record>,
        showComparison: Boolean,
        types: List<RecordType>,
        tags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
    ): StatisticsDetailStatsViewData {
        val recordsSorted = records.sortedBy { it.timeStarted }
        val durations = records.map(::mapToDuration)

        val compareRecordsSorted = compareRecords.sortedBy { it.timeStarted }
        val compareDurations = compareRecords.map(::mapToDuration)

        val emptyValue by lazy {
            resourceRepo.getString(R.string.statistics_detail_empty)
        }
        val recordsAllIcon = StatisticsDetailCardViewData.Icon(
            iconDrawable = R.drawable.statistics_detail_records_all,
            iconColor = if (isDarkTheme) {
                R.color.colorInactiveDark
            } else {
                R.color.colorInactive
            }.let(resourceRepo::getColor)
        )
        val tagSplitData = mapTags(
            records = records,
            typesMap = types.map { it.id to it }.toMap(),
            tagsMap = tags.map { it.id to it }.toMap(),
            isDarkTheme = isDarkTheme,
            useProportionalMinutes = useProportionalMinutes
        )

        fun formatInterval(value: Long?): String {
            value ?: return emptyValue
            return timeMapper.formatInterval(value, useProportionalMinutes)
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
            shortestRecord = durations.minOrNull()
                .let(::formatInterval),
            compareShortestRecord = compareDurations.minOrNull()
                .let(::formatInterval)
                .let(::processComparisonString),
            averageRecord = getAverage(durations)
                .let(::formatInterval),
            compareAverageRecord = getAverage(compareDurations)
                .let(::formatInterval)
                .let(::processComparisonString),
            longestRecord = durations.maxOrNull()
                .let(::formatInterval),
            compareLongestRecord = compareDurations.maxOrNull()
                .let(::formatInterval)
                .let(::processComparisonString),
            firstRecord = recordsSorted.firstOrNull()?.timeStarted
                .let(::formatDateTimeYear),
            compareFirstRecord = compareRecordsSorted.firstOrNull()?.timeStarted
                .let(::formatDateTimeYear)
                .let(::processComparisonString),
            lastRecord = recordsSorted.lastOrNull()?.timeEnded
                .let(::formatDateTimeYear),
            compareLastRecord = compareRecordsSorted.lastOrNull()?.timeEnded
                .let(::formatDateTimeYear)
                .let(::processComparisonString),
            tagSplitData = tagSplitData
        )
    }

    private fun mapToStatsViewData(
        totalDuration: String,
        compareTotalDuration: String,
        timesTracked: Int?,
        compareTimesTracked: String,
        timesTrackedIcon: StatisticsDetailCardViewData.Icon?,
        shortestRecord: String,
        compareShortestRecord: String,
        averageRecord: String,
        compareAverageRecord: String,
        longestRecord: String,
        compareLongestRecord: String,
        firstRecord: String,
        compareFirstRecord: String,
        lastRecord: String,
        compareLastRecord: String,
        tagSplitData: List<ViewHolderType>,
    ): StatisticsDetailStatsViewData {
        return StatisticsDetailStatsViewData(
            totalDuration = listOf(
                StatisticsDetailCardViewData(
                    title = totalDuration,
                    secondTitle = compareTotalDuration,
                    subtitle = resourceRepo.getString(R.string.statistics_detail_total_duration)
                )
            ),
            timesTracked = listOf(
                StatisticsDetailCardViewData(
                    title = timesTracked?.toString() ?: "",
                    secondTitle = compareTimesTracked,
                    subtitle = resourceRepo.getQuantityString(
                        R.plurals.statistics_detail_times_tracked, timesTracked.orZero()
                    ),
                    icon = timesTrackedIcon
                )
            ),
            averageRecord = listOf(
                StatisticsDetailCardViewData(
                    title = shortestRecord,
                    secondTitle = compareShortestRecord,
                    subtitle = resourceRepo.getString(R.string.statistics_detail_shortest_record)
                ),
                StatisticsDetailCardViewData(
                    title = averageRecord,
                    secondTitle = compareAverageRecord,
                    subtitle = resourceRepo.getString(R.string.statistics_detail_average_record)
                ),
                StatisticsDetailCardViewData(
                    title = longestRecord,
                    secondTitle = compareLongestRecord,
                    subtitle = resourceRepo.getString(R.string.statistics_detail_longest_record)
                )
            ),
            datesTracked = listOf(
                StatisticsDetailCardViewData(
                    title = firstRecord,
                    secondTitle = compareFirstRecord,
                    subtitle = resourceRepo.getString(R.string.statistics_detail_first_record)
                ),
                StatisticsDetailCardViewData(
                    title = lastRecord,
                    secondTitle = compareLastRecord,
                    subtitle = resourceRepo.getString(R.string.statistics_detail_last_record)
                )
            ),
            tagSplitData = tagSplitData
        )
    }

    private fun mapToDuration(record: Record): Long {
        return record.let { it.timeEnded - it.timeStarted }
    }

    private fun mapTags(
        records: List<Record>,
        typesMap: Map<Long, RecordType>,
        tagsMap: Map<Long, RecordTag>,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
    ): List<ViewHolderType> {
        val tags: MutableMap<Long, MutableList<Record>> = mutableMapOf()

        records.forEach { record ->
            record.tagIds.forEach { tagId ->
                tags.getOrPut(tagId, { mutableListOf() }).add(record)
            }
            if (record.tagIds.isEmpty()) {
                tags.getOrPut(0, { mutableListOf() }).add(record)
            }
        }

        val durations = tags
            .takeUnless { it.isEmpty() }
            ?.mapValues { (_, records) -> records.let(statisticsMapper::mapToDuration) }
            ?: return emptyList()
        val tagsSize = tags.size
        val sumDuration = durations.map { (_, duration) -> duration }.sum()
        val hint = resourceRepo.getString(R.string.statistics_detail_tag_split_hint)
            .let(::HintViewData).let(::listOf)

        return hint + durations
            .mapNotNull { (tagId, duration) ->
                val tag = tagsMap[tagId]
                val type = typesMap[tag?.typeId]

                mapTag(
                    tag = tag,
                    recordType = type,
                    duration = duration,
                    sumDuration = sumDuration,
                    isDarkTheme = isDarkTheme,
                    statisticsSize = tagsSize,
                    useProportionalMinutes = useProportionalMinutes,
                ) to duration
            }
            .sortedByDescending { (_, duration) -> duration }
            .map { (statistics, _) -> statistics }
    }

    private fun mapTag(
        tag: RecordTag?,
        recordType: RecordType?,
        duration: Long,
        sumDuration: Long,
        isDarkTheme: Boolean,
        statisticsSize: Int,
        useProportionalMinutes: Boolean,
    ): StatisticsTagViewData {
        val durationPercent = statisticsMapper.getDurationPercentString(
            sumDuration = sumDuration,
            duration = duration,
            statisticsSize = statisticsSize
        )

        return StatisticsTagViewData(
            id = tag?.id.orZero(),
            name = tag?.name
                ?: R.string.change_record_untagged.let(resourceRepo::getString),
            duration = duration
                .let { timeMapper.formatInterval(it, useProportionalMinutes) },
            percent = durationPercent,
            // Take icon and color from recordType if it is a typed tag,
            // show empty icon and tag color for untyped tags,
            // show unknown icon and untracked color if tagId == 0, meaning it is untagged.
            icon = recordType?.icon
                ?.let(iconMapper::mapIcon)
                ?: tag?.run { RecordTypeIcon.Image(0) }
                ?: RecordTypeIcon.Image(R.drawable.unknown),
            color = recordType?.color?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                ?: tag?.color?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                ?: colorMapper.toUntrackedColor(isDarkTheme)
        )
    }
}