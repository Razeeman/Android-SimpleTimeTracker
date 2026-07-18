package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.interactor.StatisticsChartViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.StatisticsViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.base.OneShotValue
import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.statistics.interactor.StatisticsCategoryInteractor
import com.example.util.simpletimetracker.domain.statistics.interactor.StatisticsInteractor
import com.example.util.simpletimetracker.domain.statistics.interactor.StatisticsTagInteractor
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.statistics.model.Statistics
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsSelectableViewData
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailHintViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailPieChartViewData
import com.example.util.simpletimetracker.feature_views.barChart.BarChartView
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataDuration
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionGraph
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionMode
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDataDistributionGraphViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDataDistributionModeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDataDistributionViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsDetailDataDistributionInteractor @Inject constructor(
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val statisticsInteractor: StatisticsInteractor,
    private val statisticsTagInteractor: StatisticsTagInteractor,
    private val statisticsCategoryInteractor: StatisticsCategoryInteractor,
    private val statisticsMediator: StatisticsMediator,
    private val statisticsChartViewDataInteractor: StatisticsChartViewDataInteractor,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
    private val statisticsViewDataMapper: StatisticsViewDataMapper,
) {

    suspend fun getViewData(
        records: List<RecordBase>,
        rangeLength: RangeLength,
        rangePosition: Int,
        dataDistributionMode: DataDistributionMode,
        dataDistributionGraph: DataDistributionGraph,
        selectedItemId: Long?,
        animate: Boolean,
    ): StatisticsDetailDataDistributionViewData = withContext(Dispatchers.Default) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val durationFormat = prefsInteractor.getDurationFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val types = recordTypeInteractor.getAll()

        val typesMap = types.associateBy(RecordType::id)
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val splitData = mapDataDistribution(
            mode = dataDistributionMode,
            graph = dataDistributionGraph,
            selectedItemId = selectedItemId,
            records = if (range.isUndefined) {
                records
            } else {
                rangeMapper.getRecordsFromRange(records, range)
                    .map { rangeMapper.clampRecordToRange(it, range) }
            },
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
            animate = animate,
        )

        return@withContext StatisticsDetailDataDistributionViewData(
            splitData = splitData,
        )
    }

    private suspend fun mapDataDistribution(
        mode: DataDistributionMode,
        graph: DataDistributionGraph,
        selectedItemId: Long?,
        records: List<RecordBase>,
        typesMap: Map<Long, RecordType>,
        isDarkTheme: Boolean,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
        animate: Boolean,
    ): List<ViewHolderType> {
        if (records.isEmpty()) return emptyList()
        val result = mutableListOf<ViewHolderType>()

        val filterType = mapFilterType(
            mode = mode,
        )
        val dataHolders = statisticsMediator.getDataHolders(
            filterType = filterType,
            types = typesMap,
        )
        val statistics = getStatistics(
            mode = mode,
            allRecords = records,
        )
        val chart = mapChart(
            graph = graph,
            filterType = filterType,
            selectedItemId = selectedItemId,
            statistics = statistics,
            data = dataHolders,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
            animate = animate,
        )
        val items = mapItemsList(
            statistics = statistics,
            data = dataHolders,
            filterType = filterType,
            selectedItemId = selectedItemId,
            isDarkTheme = isDarkTheme,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
        )

        result += mapHint()
        result += chart
        result += mapModeControl(mode)
        result += mapGraphControl(graph)
        result += items

        return result
    }

    private suspend fun getStatistics(
        mode: DataDistributionMode,
        allRecords: List<RecordBase>,
    ): List<Statistics> {
        return when (mode) {
            DataDistributionMode.ACTIVITY -> {
                statisticsInteractor.getActivityRecordsFull(
                    allRecords = allRecords,
                )
            }
            DataDistributionMode.CATEGORY -> {
                statisticsCategoryInteractor.getCategoryRecords(
                    allRecords = allRecords,
                    addUncategorized = true,
                )
            }
            DataDistributionMode.TAG -> {
                statisticsTagInteractor.getTagRecords(
                    allRecords = allRecords,
                    addUncategorized = true,
                )
            }
        }.let {
            statisticsInteractor.getStatisticsData(
                allRecords = it,
                showSeconds = true, // All detailed statistics accounts for seconds.
            )
        }
    }

    private fun mapItemsList(
        shift: Int = 0,
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        filterType: ChartFilterType,
        selectedItemId: Long?,
        isDarkTheme: Boolean,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
    ): List<ViewHolderType> {
        return statisticsViewDataMapper.mapItemsList(
            shift = shift,
            statistics = statistics,
            data = data,
            filterType = filterType,
            filteredIds = emptyList(),
            showDuration = true,
            isDarkTheme = isDarkTheme,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
            hasTransitions = false,
        ).map {
            StatisticsSelectableViewData(
                data = it,
                isSelected = it.id == selectedItemId,
            )
        }
    }

    private suspend fun mapChart(
        graph: DataDistributionGraph,
        filterType: ChartFilterType,
        selectedItemId: Long?,
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        typesMap: Map<Long, RecordType>,
        isDarkTheme: Boolean,
        animate: Boolean,
    ): ViewHolderType {
        return when (graph) {
            DataDistributionGraph.PIE_CHART -> {
                statisticsChartViewDataInteractor.getChart(
                    filterType = filterType,
                    filteredIds = emptyList(),
                    statistics = statistics,
                    dataHolders = data,
                    types = typesMap,
                    isDarkTheme = isDarkTheme,
                ).let { chartData ->
                    val selectedPiePosition = chartData
                        .indexOfFirst { it.id == selectedItemId }
                        .takeUnless { it == -1 }
                    StatisticsDetailPieChartViewData(
                        block = StatisticsDetailBlock.DataDistributionPieChart,
                        data = chartData,
                        selectedPiePosition = selectedPiePosition,
                        animate = OneShotValue(animate),
                    )
                }
            }
            DataDistributionGraph.BAR_CHART -> {
                mapBarChartData(
                    statistics = statistics,
                    data = data,
                    selectedItemId = selectedItemId,
                    isDarkTheme = isDarkTheme,
                    animate = animate,
                ).let { chartData ->
                    StatisticsDetailBarChartViewData(
                        block = StatisticsDetailBlock.DataDistributionBarChart,
                        singleColor = null,
                        marginTopDp = 0,
                        data = chartData,
                    )
                }
            }
        }
    }

    private fun mapBarChartData(
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        selectedItemId: Long?,
        isDarkTheme: Boolean,
        animate: Boolean,
    ): StatisticsDetailChartViewData {
        val chartData = statistics
            .mapNotNull { statistic ->
                val chart = mapBarChartItem(
                    statistics = statistic,
                    dataHolder = data[statistic.id],
                    isDarkTheme = isDarkTheme,
                ) ?: return@mapNotNull null
                chart to statistic
            }
            .sortedBy { (_, statistic) -> statistic.data.duration }
        val selectedBarPosition = chartData
            .indexOfFirst { it.second.id == selectedItemId }
            .takeUnless { it == -1 }
        val (legendSuffix, isMinutes) = chartData
            .map { (statistics, _) -> statistics }
            .let { statisticsDetailViewDataMapper.mapLegendSuffix(it) }

        return StatisticsDetailChartViewData(
            visible = true,
            data = chartData.map { (chart, statistic) ->
                val value = chart.durations.map { (duration, color) ->
                    statisticsDetailViewDataMapper.formatInterval(duration, isMinutes) to color
                }
                BarChartView.ViewData(
                    id = statistic.id,
                    value = value,
                    legend = chart.legend,
                )
            },
            legendSuffix = legendSuffix,
            addLegendToSelectedBar = false,
            shouldDrawHorizontalLegends = false,
            showSelectedBarOnStart = false,
            selectedBarPosition = selectedBarPosition,
            goalValue = 0f,
            yAxisZoomed = false,
            useSingleColor = true,
            drawRoundCaps = true,
            animate = OneShotValue(animate),
        )
    }

    private fun mapBarChartItem(
        statistics: Statistics,
        dataHolder: StatisticsDataHolder?,
        isDarkTheme: Boolean,
    ): ChartBarDataDuration? {
        return when {
            statistics.id == UNTRACKED_ITEM_ID -> {
                ChartBarDataDuration(
                    rangeStart = 0, // Not needed.
                    legend = "",
                    durations = listOf(
                        statistics.data.duration to
                            colorMapper.toUntrackedColor(isDarkTheme),
                    ),
                )
            }
            statistics.id == UNCATEGORIZED_ITEM_ID -> {
                ChartBarDataDuration(
                    rangeStart = 0,
                    legend = "",
                    durations = listOf(
                        statistics.data.duration to
                            colorMapper.toUntrackedColor(isDarkTheme),
                    ),
                )
            }
            dataHolder != null -> {
                ChartBarDataDuration(
                    rangeStart = 0,
                    legend = "",
                    durations = listOf(
                        statistics.data.duration to
                            dataHolder.color.let { colorMapper.mapToColorInt(it, isDarkTheme) },
                    ),
                )
            }
            else -> {
                null
            }
        }
    }

    private fun mapHint(): ViewHolderType {
        return StatisticsDetailHintViewData(
            block = StatisticsDetailBlock.DataDistributionHint,
            text = resourceRepo.getString(R.string.statistics_detail_data_split_hint),
        )
    }

    private fun mapModeControl(
        mode: DataDistributionMode,
    ): ViewHolderType {
        val values = listOf(
            DataDistributionMode.ACTIVITY,
            DataDistributionMode.CATEGORY,
            DataDistributionMode.TAG,
        )

        return ButtonsRowItemViewData(
            block = StatisticsDetailBlock.DataDistributionMode,
            marginTopDp = 4,
            data = values.map {
                StatisticsDetailDataDistributionModeViewData(
                    mode = it,
                    name = when (it) {
                        DataDistributionMode.ACTIVITY -> R.string.activity_hint
                        DataDistributionMode.CATEGORY -> R.string.category_hint
                        DataDistributionMode.TAG -> R.string.record_tag_hint_short
                    }.let(resourceRepo::getString),
                    isSelected = it == mode,
                    textSizeSp = 12,
                )
            },
        )
    }

    private fun mapGraphControl(
        graph: DataDistributionGraph,
    ): ViewHolderType {
        val values = listOf(
            DataDistributionGraph.PIE_CHART,
            DataDistributionGraph.BAR_CHART,
        )
        return ButtonsRowItemViewData(
            block = StatisticsDetailBlock.DataDistributionGraph,
            marginTopDp = -10,
            data = values.map {
                StatisticsDetailDataDistributionGraphViewData(
                    graph = it,
                    name = when (it) {
                        DataDistributionGraph.PIE_CHART ->
                            R.string.statistics_detail_data_split_pie_chart
                        DataDistributionGraph.BAR_CHART ->
                            R.string.statistics_detail_data_split_bar_chart
                    }.let(resourceRepo::getString),
                    isSelected = it == graph,
                )
            },
        )
    }

    private fun mapFilterType(mode: DataDistributionMode): ChartFilterType {
        return when (mode) {
            DataDistributionMode.ACTIVITY -> ChartFilterType.ACTIVITY
            DataDistributionMode.CATEGORY -> ChartFilterType.CATEGORY
            DataDistributionMode.TAG -> ChartFilterType.RECORD_TAG
        }
    }
}