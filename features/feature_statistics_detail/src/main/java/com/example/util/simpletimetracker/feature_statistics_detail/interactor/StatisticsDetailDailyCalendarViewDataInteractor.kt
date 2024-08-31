package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.view.dayCalendar.DayCalendarViewData
import com.example.util.simpletimetracker.domain.extension.getCategoryItems
import com.example.util.simpletimetracker.domain.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.extension.hasUncategorizedItem
import com.example.util.simpletimetracker.domain.extension.hasUntaggedItem
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsTagInteractor
import com.example.util.simpletimetracker.domain.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailDailyCalendarViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDayCalendarViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

class StatisticsDetailDailyCalendarViewDataInteractor @Inject constructor(
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val colorMapper: ColorMapper,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsCategoryInteractor: StatisticsCategoryInteractor,
    private val statisticsTagInteractor: StatisticsTagInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
    private val statisticsDetailPreviewInteractor: StatisticsDetailPreviewInteractor,
    private val mapper: StatisticsDetailDailyCalendarViewDataMapper,
) {

    fun getEmptyChartViewData(
        rangeLength: RangeLength,
    ): StatisticsDetailDayCalendarViewData? {
        if (rangeLength != RangeLength.Day) return null
        return mapper.mapToEmpty()
    }

    suspend fun getViewData(
        records: List<RecordBase>,
        compareRecords: List<RecordBase>,
        filter: List<RecordsFilter>,
        compare: List<RecordsFilter>, // TODO
        rangeLength: RangeLength,
        rangePosition: Int,
    ): StatisticsDetailDayCalendarViewData? = withContext(Dispatchers.Default) {
        if (rangeLength != RangeLength.Day) return@withContext null

        val calendar = Calendar.getInstance()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val recordsFromRange = rangeMapper.getRecordsFromRange(records, range)
            .map { rangeMapper.clampRecordToRange(it, range) }
        val previewType =
            statisticsDetailPreviewInteractor.getPreviewType(filter)
        val data = getData(
            previewType = previewType,
            records = recordsFromRange,
            filter = filter,
            isDarkTheme = isDarkTheme,
        ).map {
            mapper.mapToCalendarPoint(
                holder = it,
                calendar = calendar,
                startOfDayShift = startOfDayShift,
            )
        }

        return@withContext StatisticsDetailDayCalendarViewData(
            data = DayCalendarViewData(data),
        )
    }

    private suspend fun getData(
        previewType: StatisticsDetailPreviewInteractor.PreviewType,
        records: List<RecordBase>,
        filter: List<RecordsFilter>,
        isDarkTheme: Boolean,
    ): List<RecordHolder> {
        val untrackedColor = colorMapper.toUntrackedColor(isDarkTheme)

        return when (previewType) {
            is StatisticsDetailPreviewInteractor.PreviewType.Untracked,
            is StatisticsDetailPreviewInteractor.PreviewType.Multitask,
            -> getUndefinedData(
                records = records,
                untrackedColor = untrackedColor,
            )
            is StatisticsDetailPreviewInteractor.PreviewType.Activities,
            is StatisticsDetailPreviewInteractor.PreviewType.ActivitiesFromRecords,
            -> getActivitiesData(
                records = records,
                untrackedColor = untrackedColor,
                isDarkTheme = isDarkTheme,
            )
            is StatisticsDetailPreviewInteractor.PreviewType.Categories,
            -> getCategoriesData(
                records = records,
                filter = filter,
                untrackedColor = untrackedColor,
                isDarkTheme = isDarkTheme,
            )
            is StatisticsDetailPreviewInteractor.PreviewType.SelectedTags,
            -> getTagsData(
                records = records,
                filter = filter,
                untrackedColor = untrackedColor,
                isDarkTheme = isDarkTheme,
            )
        }
    }

    private fun getUndefinedData(
        records: List<RecordBase>,
        untrackedColor: Int,
    ): List<RecordHolder> {
        return records.map { record ->
            mapper.mapRecordHolder(record, untrackedColor)
        }
    }

    private suspend fun getActivitiesData(
        records: List<RecordBase>,
        untrackedColor: Int,
        isDarkTheme: Boolean,
    ): List<RecordHolder> {
        val types = recordTypeInteractor.getAll()
            .associateBy { it.id }

        return records.map { record ->
            val typeId = record.typeIds.firstOrNull().orZero()
            val color = types[typeId]?.color
                ?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                ?: untrackedColor
            mapper.mapRecordHolder(record, color)
        }
    }

    private suspend fun getCategoriesData(
        records: List<RecordBase>,
        filter: List<RecordsFilter>,
        untrackedColor: Int,
        isDarkTheme: Boolean,
    ): List<RecordHolder> {
        val categories = categoryInteractor.getAll()
            .associateBy(Category::id)
        val categoriesData = statisticsCategoryInteractor.getCategoryRecords(
            records,
        ).flatMap { (categoryId, records) ->
            records.map { record ->
                val color = categories[categoryId]?.color
                    ?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                    ?: untrackedColor
                mapper.mapRecordHolder(record, color)
            }
        }
        val uncategorizedRecords = if (
            filter.getCategoryItems().hasUncategorizedItem()
        ) {
            statisticsCategoryInteractor.getUncategorized(records)
        } else {
            emptyList()
        }
        val uncategorizedData = uncategorizedRecords.map { record ->
            mapper.mapRecordHolder(record, untrackedColor)
        }

        return categoriesData + uncategorizedData
    }

    private suspend fun getTagsData(
        records: List<RecordBase>,
        filter: List<RecordsFilter>,
        untrackedColor: Int,
        isDarkTheme: Boolean,
    ): List<RecordHolder> {
        val tags = recordTagInteractor.getAll()
            .associateBy(RecordTag::id)
        val types = recordTypeInteractor.getAll()
            .associateBy(RecordType::id)
        val tagsData = statisticsTagInteractor.getTagRecords(
            records,
        ).flatMap { (tagId, records) ->
            records.map { record ->
                val color = tags[tagId]
                    ?.let { recordTagViewDataMapper.mapColor(it, types) }
                    ?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                    ?: untrackedColor
                mapper.mapRecordHolder(record, color)
            }
        }
        val untaggedRecords = if (
            filter.getSelectedTags().hasUntaggedItem()
        ) {
            statisticsTagInteractor.getUntagged(records)
        } else {
            emptyList()
        }
        val untaggedData = untaggedRecords.map { record ->
            mapper.mapRecordHolder(record, untrackedColor)
        }

        return tagsData + untaggedData

    }

    data class RecordHolder(
        val timeStartedTimestamp: Long,
        val timeEndedTimestamp: Long,
        @ColorInt val color: Int,
    )
}