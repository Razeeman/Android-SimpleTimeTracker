package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.StatisticsWidgetData
import com.example.util.simpletimetracker.domain.repo.PrefsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PrefsInteractor @Inject constructor(
    private val prefsRepo: PrefsRepo,
) {

    suspend fun getFilteredTypes(): List<Long> = withContext(Dispatchers.IO) {
        prefsRepo.recordTypesFilteredOnChart
            .mapNotNull(String::toLongOrNull)
    }

    suspend fun setFilteredTypes(typeIdsFiltered: List<Long>) = withContext(Dispatchers.IO) {
        prefsRepo.recordTypesFilteredOnChart = typeIdsFiltered
            .map(Long::toString).toSet()
    }

    suspend fun getFilteredCategories(): List<Long> = withContext(Dispatchers.IO) {
        prefsRepo.categoriesFilteredOnChart
            .mapNotNull(String::toLongOrNull)
    }

    suspend fun setFilteredCategories(categoryIdsFiltered: List<Long>) = withContext(Dispatchers.IO) {
        prefsRepo.categoriesFilteredOnChart = categoryIdsFiltered
            .map(Long::toString).toSet()
    }

    suspend fun getChartFilterType(): ChartFilterType = withContext(Dispatchers.IO) {
        when (prefsRepo.chartFilterType) {
            0 -> ChartFilterType.ACTIVITY
            1 -> ChartFilterType.CATEGORY
            else -> ChartFilterType.ACTIVITY
        }
    }

    suspend fun setChartFilterType(chartFilterType: ChartFilterType) = withContext(Dispatchers.IO) {
        prefsRepo.chartFilterType = when (chartFilterType) {
            ChartFilterType.ACTIVITY -> 0
            ChartFilterType.CATEGORY -> 1
        }
    }

    suspend fun getCardOrder(): CardOrder = withContext(Dispatchers.IO) {
        when (prefsRepo.cardOrder) {
            0 -> CardOrder.NAME
            1 -> CardOrder.COLOR
            2 -> CardOrder.MANUAL
            else -> CardOrder.NAME
        }
    }

    suspend fun setCardOrder(cardOrder: CardOrder) = withContext(Dispatchers.IO) {
        prefsRepo.cardOrder = when (cardOrder) {
            CardOrder.NAME -> 0
            CardOrder.COLOR -> 1
            CardOrder.MANUAL -> 2
        }
    }

    suspend fun getStatisticsRange(): RangeLength = withContext(Dispatchers.IO) {
        mapToRange(prefsRepo.statisticsRange, forDetail = false)
    }

    suspend fun setStatisticsRange(rangeLength: RangeLength) = withContext(Dispatchers.IO) {
        prefsRepo.statisticsRange = mapRange(rangeLength)

        if (rangeLength is RangeLength.Custom) {
            prefsRepo.statisticsRangeCustomStart = rangeLength.range.timeStarted
            prefsRepo.statisticsRangeCustomEnd = rangeLength.range.timeEnded
        }
    }

    suspend fun getStatisticsDetailRange(): RangeLength = withContext(Dispatchers.IO) {
        mapToRange(prefsRepo.statisticsDetailRange, forDetail = true)
    }

    suspend fun setStatisticsDetailRange(rangeLength: RangeLength) = withContext(Dispatchers.IO) {
        prefsRepo.statisticsDetailRange = mapRange(rangeLength)

        if (rangeLength is RangeLength.Custom) {
            prefsRepo.statisticsDetailRangeCustomStart = rangeLength.range.timeStarted
            prefsRepo.statisticsDetailRangeCustomEnd = rangeLength.range.timeEnded
        }
    }

    suspend fun getKeepStatisticsRange(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.keepStatisticsRange
    }

    suspend fun setKeepStatisticsRange(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.keepStatisticsRange = isEnabled
    }

    suspend fun getFirstDayOfWeek(): DayOfWeek = withContext(Dispatchers.IO) {
        // Same as in java Calendar
        when (prefsRepo.firstDayOfWeek) {
            1 -> DayOfWeek.SUNDAY
            2 -> DayOfWeek.MONDAY
            3 -> DayOfWeek.TUESDAY
            4 -> DayOfWeek.WEDNESDAY
            5 -> DayOfWeek.THURSDAY
            6 -> DayOfWeek.FRIDAY
            7 -> DayOfWeek.SATURDAY
            else -> DayOfWeek.SUNDAY
        }
    }

    suspend fun setFirstDayOfWeek(dayOfWeek: DayOfWeek) = withContext(Dispatchers.IO) {
        // Same as in java Calendar
        prefsRepo.firstDayOfWeek = when (dayOfWeek) {
            DayOfWeek.SUNDAY -> 1
            DayOfWeek.MONDAY -> 2
            DayOfWeek.TUESDAY -> 3
            DayOfWeek.WEDNESDAY -> 4
            DayOfWeek.THURSDAY -> 5
            DayOfWeek.FRIDAY -> 6
            DayOfWeek.SATURDAY -> 7
        }
    }

    suspend fun getStartOfDayShift(): Long = withContext(Dispatchers.IO) {
        prefsRepo.startOfDayShift
    }

    suspend fun setStartOfDayShift(startOfDay: Long) = withContext(Dispatchers.IO) {
        prefsRepo.startOfDayShift = startOfDay
    }

    suspend fun getShowUntrackedInRecords(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showUntrackedInRecords
    }

    suspend fun setShowUntrackedInRecords(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showUntrackedInRecords = isEnabled
    }

    suspend fun getShowRecordsCalendar(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showRecordsCalendar
    }

    suspend fun setShowRecordsCalendar(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showRecordsCalendar = isEnabled
    }

    suspend fun getReverseOrderInCalendar(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.reverseOrderInCalendar
    }

    suspend fun setReverseOrderInCalendar(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.reverseOrderInCalendar = isEnabled
    }

    suspend fun getShowActivityFilters(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showActivityFilters
    }

    suspend fun setShowActivityFilters(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showActivityFilters = isEnabled
    }

    suspend fun getAllowMultitasking(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.allowMultitasking
    }

    suspend fun setAllowMultitasking(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.allowMultitasking = isEnabled
    }

    suspend fun getShowNotifications(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showNotifications
    }

    suspend fun setShowNotifications(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showNotifications = isEnabled
    }

    suspend fun getInactivityReminderDuration(): Long = withContext(Dispatchers.IO) {
        prefsRepo.inactivityReminderDuration
    }

    suspend fun setInactivityReminderDuration(duration: Long) = withContext(Dispatchers.IO) {
        prefsRepo.inactivityReminderDuration = duration
    }

    suspend fun getIgnoreShortRecordsDuration(): Long = withContext(Dispatchers.IO) {
        prefsRepo.ignoreShortRecordsDuration
    }

    suspend fun setIgnoreShortRecordsDuration(duration: Long) = withContext(Dispatchers.IO) {
        prefsRepo.ignoreShortRecordsDuration = duration
    }

    suspend fun getDarkMode(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.darkMode
    }

    suspend fun setDarkMode(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.darkMode = isEnabled
    }

    suspend fun getNumberOfCards(): Int = withContext(Dispatchers.IO) {
        prefsRepo.numberOfCards
    }

    suspend fun setNumberOfCards(cardSize: Int) = withContext(Dispatchers.IO) {
        prefsRepo.numberOfCards = cardSize
    }

    suspend fun getUseMilitaryTimeFormat(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.useMilitaryTimeFormat
    }

    suspend fun setUseMilitaryTimeFormat(isUsed: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.useMilitaryTimeFormat = isUsed
    }

    suspend fun getUseProportionalMinutes(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.useProportionalMinutes
    }

    suspend fun setUseProportionalMinutes(isUsed: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.useProportionalMinutes = isUsed
    }

    suspend fun getKeepScreenOn(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.keepScreenOn
    }

    suspend fun setKeepScreenOn(keep: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.keepScreenOn = keep
    }

    suspend fun getShowRecordTagSelection(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showRecordTagSelection
    }

    suspend fun setShowRecordTagSelection(value: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showRecordTagSelection = value
    }

    suspend fun getRecordTagSelectionCloseAfterOne(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.recordTagSelectionCloseAfterOne
    }

    suspend fun setRecordTagSelectionCloseAfterOne(value: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.recordTagSelectionCloseAfterOne = value
    }

    suspend fun setWidget(widgetId: Int, recordType: Long) = withContext(Dispatchers.IO) {
        prefsRepo.setWidget(widgetId, recordType)
    }

    suspend fun getWidget(widgetId: Int): Long = withContext(Dispatchers.IO) {
        prefsRepo.getWidget(widgetId)
    }

    suspend fun removeWidget(widgetId: Int) = withContext(Dispatchers.IO) {
        prefsRepo.removeWidget(widgetId)
    }

    suspend fun setStatisticsWidget(widgetId: Int, data: StatisticsWidgetData) = withContext(Dispatchers.IO) {
        prefsRepo.setStatisticsWidget(widgetId, data)
    }

    suspend fun getStatisticsWidget(widgetId: Int): StatisticsWidgetData = withContext(Dispatchers.IO) {
        prefsRepo.getStatisticsWidget(widgetId)
    }

    suspend fun removeStatisticsWidget(widgetId: Int) = withContext(Dispatchers.IO) {
        prefsRepo.removeStatisticsWidget(widgetId)
    }

    suspend fun setCardOrderManual(cardsOrder: Map<Long, Long>) = withContext(Dispatchers.IO) {
        prefsRepo.setCardOrderManual(cardsOrder)
    }

    suspend fun getCardOrderManual(): Map<Long, Long> = withContext(Dispatchers.IO) {
        prefsRepo.getCardOrderManual()
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        prefsRepo.clear()
    }

    private fun mapToRange(value: Int, forDetail: Boolean): RangeLength {
        return when (value) {
            0 -> RangeLength.Day
            1 -> RangeLength.Week
            2 -> RangeLength.Month
            3 -> RangeLength.Year
            4 -> RangeLength.All
            5 -> {
                if (forDetail) {
                    Range(
                        timeStarted = prefsRepo.statisticsDetailRangeCustomStart,
                        timeEnded = prefsRepo.statisticsDetailRangeCustomEnd
                    )
                } else {
                    Range(
                        timeStarted = prefsRepo.statisticsRangeCustomStart,
                        timeEnded = prefsRepo.statisticsRangeCustomEnd
                    )
                }.let(RangeLength::Custom)
            }
            6 -> RangeLength.Last
            else -> RangeLength.Day
        }
    }

    private fun mapRange(rangeLength: RangeLength): Int {
        return when (rangeLength) {
            is RangeLength.Day -> 0
            is RangeLength.Week -> 1
            is RangeLength.Month -> 2
            is RangeLength.Year -> 3
            is RangeLength.All -> 4
            is RangeLength.Custom -> 5
            is RangeLength.Last -> 6
        }
    }
}