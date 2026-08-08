package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTagValueType
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailPreviewInteractor.PreviewType
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailTagValuesViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartMode
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartSplitSortMode
import com.example.util.simpletimetracker.domain.statistics.model.StatisticsDetailTagValueSettings
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailTagValuesCompositeViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsDetailTagValueInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val chartInteractor: StatisticsDetailChartInteractor,
    private val statisticsDetailPreviewInteractor: StatisticsDetailPreviewInteractor,
    private val statisticsDetailTagValuesViewDataMapper: StatisticsDetailTagValuesViewDataMapper,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val fillEmptyBarsWithPreviousValueInteractor: FillEmptyBarsWithPreviousValueInteractor,
) {

    // TODO compare?
    suspend fun getViewData(
        valuedTag: RecordTag,
        records: List<RecordBase>,
        currentChartGrouping: ChartGrouping,
        currentChartLength: ChartLength,
        settings: StatisticsDetailTagValueSettings,
        rangeLength: RangeLength,
        rangePosition: Int,
    ): StatisticsDetailTagValuesCompositeViewData = withContext(Dispatchers.Default) {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val durationFormat = prefsInteractor.getDurationFormat()
        val useMonthDayTimeFormat = prefsInteractor.getUseMonthDayTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val types = recordTypeInteractor.getAll()
        val typesMap = types.associateBy(RecordType::id)
        val typesOrder = types.map(RecordType::id)
        val chartMode = ChartMode.TAG_VALUE(
            tagId = valuedTag.id,
            multiplyDuration = settings.multiplyDuration,
        )

        val compositeData = chartInteractor.getChartRangeSelectionData(
            currentChartGrouping = currentChartGrouping,
            currentChartLength = currentChartLength,
            rangeLength = rangeLength,
            firstDayOfWeek = firstDayOfWeek,
        )
        val ranges = chartInteractor.getRanges(
            compositeData = compositeData,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            useMonthDayTimeFormat = useMonthDayTimeFormat,
        )
        val data = chartInteractor.getChartData(
            allRecords = records,
            ranges = ranges,
            typesOrder = typesOrder,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
            chartMode = chartMode,
            chartValueMode = settings.chartValueMode,
            multiplyDuration = settings.multiplyDuration,
            splitByActivity = false,
            splitSortMode = ChartSplitSortMode.ACTIVITY_ORDER,
        )
        val prevData = chartInteractor.getPrevData(
            rangeLength = rangeLength,
            compositeData = compositeData,
            rangePosition = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            useMonthDayTimeFormat = useMonthDayTimeFormat,
            records = records,
            typesOrder = typesOrder,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
            chartMode = chartMode,
            chartValueMode = settings.chartValueMode,
            multiplyDuration = settings.multiplyDuration,
            splitSortMode = ChartSplitSortMode.ACTIVITY_ORDER,
        )
        val preparedPrevData = if (settings.fillEmptyPeriods) {
            fillEmptyBarsWithPreviousValueInteractor.invoke(prevData, null)
        } else {
            prevData
        }
        val preparedData = if (settings.fillEmptyPeriods) {
            fillEmptyBarsWithPreviousValueInteractor.invoke(data, preparedPrevData)
        } else {
            data
        }

        val chartViewData = statisticsDetailTagValuesViewDataMapper.mapTagValueChartViewData(
            data = preparedData,
            prevData = preparedPrevData,
            rangeLength = rangeLength,
            availableChartGroupings = compositeData.availableChartGroupings,
            appliedChartGrouping = compositeData.appliedChartGrouping,
            availableChartLengths = compositeData.availableChartLengths,
            appliedChartLength = compositeData.appliedChartLength,
            chartMode = chartMode,
            chartValueMode = settings.chartValueMode,
            yAxisZoomed = settings.yAxisZoomed,
            valueSuffix = valuedTag.valueSuffix,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
            isDarkTheme = isDarkTheme,
        )

        return@withContext StatisticsDetailTagValuesCompositeViewData(
            viewData = chartViewData,
            appliedChartGrouping = compositeData.appliedChartGrouping,
            appliedChartLength = compositeData.appliedChartLength,
        )
    }

    suspend fun getSelectedTagWithValueId(
        filter: List<RecordsFilter>,
    ): RecordTag? = withContext(Dispatchers.Default) {
        val tags = recordTagInteractor.getAll()
        getSingleSelectedTagWithValue(filter, tags)
    }

    private fun getSingleSelectedTagWithValue(
        filter: List<RecordsFilter>,
        tags: List<RecordTag>,
    ): RecordTag? {
        val previewType = statisticsDetailPreviewInteractor.getPreviewType(filter)
        val selectedTags = filter.getSelectedTags().filterIsInstance<RecordsFilter.TagItem.Tagged>()
        val selectedTagId = selectedTags.firstOrNull()?.tagId
        val selectedTag = tags.firstOrNull { it.id == selectedTagId }
        val tagType = selectedTag?.valueType

        val needToShowTagValue = previewType is PreviewType.SelectedTags &&
            selectedTags.size == 1 &&
            tagType == RecordTagValueType.NUMERIC

        return if (needToShowTagValue) selectedTag else null
    }
}