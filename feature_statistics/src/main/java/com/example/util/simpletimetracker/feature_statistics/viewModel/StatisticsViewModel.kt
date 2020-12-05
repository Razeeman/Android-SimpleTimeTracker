package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.domain.model.StatisticsCategory
import com.example.util.simpletimetracker.feature_statistics.extra.StatisticsExtra
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsViewDataMapper
import com.example.util.simpletimetracker.feature_statistics.viewData.RangeLength
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.StatisticsDetailParams
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private val router: Router,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val statisticsInteractor: StatisticsInteractor,
    private val statisticsCategoryInteractor: StatisticsCategoryInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsViewDataMapper: StatisticsViewDataMapper
) : ViewModel() {

    var extra: StatisticsExtra? = null

    val statistics: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    private var rangeLength: RangeLength? = null
    private var isVisible: Boolean = false

    fun onVisible() {
        isVisible = true
        updateStatistics()
    }

    fun onHidden() {
        isVisible = false
    }

    fun onNewRange(newRangeLength: RangeLength) {
        rangeLength = newRangeLength
        if (isVisible) updateStatistics()
    }

    fun onFilterClick() {
        router.navigate(Screen.CHART_FILTER_DIALOG)
    }

    fun onItemClick(item: StatisticsViewData, sharedElements: Map<Any, String>) {
        // TODO untracked and category detailed statistics
        if (item.id == -1L || item !is StatisticsViewData.StatisticsActivityViewData) return

        router.navigate(
            screen = Screen.STATISTICS_DETAIL,
            data = StatisticsDetailParams(item.id),
            sharedElements = sharedElements
        )
    }

    fun onFilterApplied() {
        updateStatistics()
    }

    private fun getRange(): Pair<Long, Long> {
        val shift = extra?.shift.orZero()
        val rangeStart: Long
        val rangeEnd: Long
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (rangeLength) {
            RangeLength.DAY -> {
                calendar.add(Calendar.DATE, shift)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.DATE, 1) }.timeInMillis
            }
            RangeLength.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.add(Calendar.DATE, shift * 7)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.DATE, 7) }.timeInMillis
            }
            RangeLength.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.MONTH, shift)
                rangeStart = calendar.timeInMillis
                rangeEnd = calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis
            }
            RangeLength.ALL -> {
                rangeStart = 0L
                rangeEnd = 0L
            }
            else -> {
                rangeStart = 0L
                rangeEnd = 0L
            }
        }

        return rangeStart to rangeEnd
    }

    private fun updateStatistics() = viewModelScope.launch {
        val data = loadStatisticsViewData()
        (statistics as MutableLiveData).value = data
    }

    private suspend fun loadStatisticsViewData(): List<ViewHolderType> {
        val filterType = prefsInteractor.getChartFilterType()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val (start, end) = getRange()
        val showDuration = start.orZero() != 0L && end.orZero() != 0L

        val list: List<ViewHolderType>
        val chart: ViewHolderType

        when (filterType) {
            ChartFilterType.ACTIVITY -> {
                val types = recordTypeInteractor.getAll()
                val typesFiltered = prefsInteractor.getFilteredTypes()
                val statistics = getStatistics(typesFiltered)

                list = statisticsViewDataMapper.mapActivities(
                    statistics, types, typesFiltered, showDuration, isDarkTheme
                )
                chart = statisticsViewDataMapper.mapActivitiesToChart(
                    statistics, types, typesFiltered, isDarkTheme
                )
            }
            ChartFilterType.CATEGORY -> {
                val categories = categoryInteractor.getAll()
                val categoriesFiltered = prefsInteractor.getFilteredCategories()
                val statistics = getStatisticsCategory()

                list = statisticsViewDataMapper.mapCategory(
                    statistics, categories, categoriesFiltered, showDuration, isDarkTheme
                )
                chart = statisticsViewDataMapper.mapCategoriesToChart(
                    statistics, categories, categoriesFiltered, isDarkTheme
                )
            }
        }

        if (list.isEmpty()) return listOf(statisticsViewDataMapper.mapToEmpty())
        return listOf(chart) + list
    }

    private suspend fun getStatistics(typesFiltered: List<Long>): List<Statistics> {
        val (start, end) = getRange()

        return if (start.orZero() != 0L && end.orZero() != 0L) {
            statisticsInteractor.getFromRange(
                start = start.orZero(),
                end = end.orZero(),
                addUntracked = !typesFiltered.contains(-1L)
            )
        } else {
            statisticsInteractor.getAll()
        }
    }

    private suspend fun getStatisticsCategory(): List<StatisticsCategory> {
        val (start, end) = getRange()

        return if (start.orZero() != 0L && end.orZero() != 0L) {
            statisticsCategoryInteractor.getFromRange(
                start = start.orZero(),
                end = end.orZero()
            )
        } else {
            statisticsCategoryInteractor.getAll()
        }
    }
}
