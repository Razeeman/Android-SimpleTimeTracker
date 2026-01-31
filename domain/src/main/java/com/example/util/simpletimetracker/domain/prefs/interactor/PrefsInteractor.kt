package com.example.util.simpletimetracker.domain.prefs.interactor

import com.example.util.simpletimetracker.domain.base.CommentFilterType
import com.example.util.simpletimetracker.domain.base.ContainerOptionsModel
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.fileExport.ExportDateTimeFormat
import com.example.util.simpletimetracker.domain.darkMode.interactor.IsSystemInDarkModeInteractor
import com.example.util.simpletimetracker.domain.darkMode.model.DarkMode
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DaysInCalendar
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.fileExport.IsExportFormatAvailableInteractor
import com.example.util.simpletimetracker.domain.prefs.repo.PrefsRepo
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RepeatButtonType
import com.example.util.simpletimetracker.domain.recordTag.model.CardTagOrder
import com.example.util.simpletimetracker.domain.recordType.model.CardOrder
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.statistics.model.StatisticsStreaksType
import com.example.util.simpletimetracker.domain.widget.model.GridWidgetData
import com.example.util.simpletimetracker.domain.widget.model.StatisticsWidgetData
import com.example.util.simpletimetracker.domain.widget.model.QuickSettingsWidgetType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PrefsInteractor @Inject constructor(
    private val prefsRepo: PrefsRepo,
    private val isSystemInDarkModeInteractor: IsSystemInDarkModeInteractor,
    private val isExportFormatAvailableInteractor: IsExportFormatAvailableInteractor,
) {

    suspend fun getFilteredTypesOnList(): List<Long> = withContext(Dispatchers.IO) {
        prefsRepo.recordTypesFilteredOnList
            .mapNotNull(String::toLongOrNull)
    }

    suspend fun setFilteredTypesOnList(typeIdsFiltered: List<Long>) = withContext(Dispatchers.IO) {
        prefsRepo.recordTypesFilteredOnList = typeIdsFiltered
            .map(Long::toString).toSet()
    }

    suspend fun getFilteredCategoriesOnList(): List<Long> = withContext(Dispatchers.IO) {
        prefsRepo.categoriesFilteredOnList
            .mapNotNull(String::toLongOrNull)
    }

    suspend fun setFilteredCategoriesOnList(categoryIdsFiltered: List<Long>) = withContext(Dispatchers.IO) {
        prefsRepo.categoriesFilteredOnList = categoryIdsFiltered
            .map(Long::toString).toSet()
    }

    suspend fun getFilteredTagsOnList(): List<Long> = withContext(Dispatchers.IO) {
        prefsRepo.tagsFilteredOnList
            .mapNotNull(String::toLongOrNull)
    }

    suspend fun setFilteredTagsOnList(tagIdsFiltered: List<Long>) = withContext(Dispatchers.IO) {
        prefsRepo.tagsFilteredOnList = tagIdsFiltered
            .map(Long::toString).toSet()
    }

    suspend fun getListFilterType(): ChartFilterType = withContext(Dispatchers.IO) {
        mapToChartFilterType(prefsRepo.listFilterType)
    }

    suspend fun setListFilterType(chartFilterType: ChartFilterType) = withContext(Dispatchers.IO) {
        prefsRepo.listFilterType = mapFromChartFilterType(chartFilterType)
    }

    suspend fun getListFilteredIds(filterType: ChartFilterType): List<Long> = withContext(Dispatchers.IO) {
        when (filterType) {
            ChartFilterType.ACTIVITY -> getFilteredTypesOnList()
            ChartFilterType.CATEGORY -> getFilteredCategoriesOnList()
            ChartFilterType.RECORD_TAG -> getFilteredTagsOnList()
        }
    }

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

    suspend fun getFilteredTags(): List<Long> = withContext(Dispatchers.IO) {
        prefsRepo.tagsFilteredOnChart
            .mapNotNull(String::toLongOrNull)
    }

    suspend fun setFilteredTags(tagIdsFiltered: List<Long>) = withContext(Dispatchers.IO) {
        prefsRepo.tagsFilteredOnChart = tagIdsFiltered
            .map(Long::toString).toSet()
    }

    suspend fun getChartFilterType(): ChartFilterType = withContext(Dispatchers.IO) {
        mapToChartFilterType(prefsRepo.chartFilterType)
    }

    suspend fun setChartFilterType(chartFilterType: ChartFilterType) = withContext(Dispatchers.IO) {
        prefsRepo.chartFilterType = mapFromChartFilterType(chartFilterType)
    }

    suspend fun getChartFilteredIds(filterType: ChartFilterType): List<Long> = withContext(Dispatchers.IO) {
        when (filterType) {
            ChartFilterType.ACTIVITY -> getFilteredTypes()
            ChartFilterType.CATEGORY -> getFilteredCategories()
            ChartFilterType.RECORD_TAG -> getFilteredTags()
        }
    }

    suspend fun getCardOrder(): CardOrder = withContext(Dispatchers.IO) {
        mapToCardOrder(prefsRepo.cardOrder)
    }

    suspend fun setCardOrder(cardOrder: CardOrder) = withContext(Dispatchers.IO) {
        prefsRepo.cardOrder = mapFromCardOrder(cardOrder)
    }

    suspend fun hasCardOrder(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.hasCardOrder
    }

    suspend fun getCategoryOrder(): CardOrder = withContext(Dispatchers.IO) {
        mapToCardOrder(prefsRepo.categoryOrder)
    }

    suspend fun setCategoryOrder(cardOrder: CardOrder) = withContext(Dispatchers.IO) {
        prefsRepo.categoryOrder = mapFromCardOrder(cardOrder)
    }

    suspend fun getTagOrder(): CardTagOrder = withContext(Dispatchers.IO) {
        mapToCardTagOrder(prefsRepo.tagOrder)
    }

    suspend fun setTagOrder(cardOrder: CardTagOrder) = withContext(Dispatchers.IO) {
        prefsRepo.tagOrder = mapFromCardTagOrder(cardOrder)
    }

    suspend fun getStatisticsRange(): RangeLength = withContext(Dispatchers.IO) {
        mapToRange(
            value = prefsRepo.statisticsRange,
            customStart = prefsRepo.statisticsRangeCustomStart,
            customEnd = prefsRepo.statisticsRangeCustomEnd,
            lastDays = prefsRepo.statisticsRangeLastDays,
        )
    }

    suspend fun setStatisticsRange(rangeLength: RangeLength) = withContext(Dispatchers.IO) {
        prefsRepo.statisticsRange = mapRange(rangeLength)

        if (rangeLength is RangeLength.Custom) {
            prefsRepo.statisticsRangeCustomStart = rangeLength.range.timeStarted
            prefsRepo.statisticsRangeCustomEnd = rangeLength.range.timeEnded
        }
        if (rangeLength is RangeLength.Last) {
            prefsRepo.statisticsRangeLastDays = rangeLength.days
        }
    }

    suspend fun getStatisticsLastDays(): Int = withContext(Dispatchers.IO) {
        prefsRepo.statisticsRangeLastDays
    }

    suspend fun getStatisticsDetailRange(): RangeLength = withContext(Dispatchers.IO) {
        mapToRange(
            value = prefsRepo.statisticsDetailRange,
            customStart = prefsRepo.statisticsDetailRangeCustomStart,
            customEnd = prefsRepo.statisticsDetailRangeCustomEnd,
            lastDays = prefsRepo.statisticsDetailRangeLastDays,
        )
    }

    suspend fun setStatisticsDetailRange(rangeLength: RangeLength) = withContext(Dispatchers.IO) {
        prefsRepo.statisticsDetailRange = mapRange(rangeLength)

        if (rangeLength is RangeLength.Custom) {
            prefsRepo.statisticsDetailRangeCustomStart = rangeLength.range.timeStarted
            prefsRepo.statisticsDetailRangeCustomEnd = rangeLength.range.timeEnded
        }
        if (rangeLength is RangeLength.Last) {
            prefsRepo.statisticsDetailRangeLastDays = rangeLength.days
        }
    }

    suspend fun getStatisticsDetailLastDays(): Int = withContext(Dispatchers.IO) {
        prefsRepo.statisticsDetailRangeLastDays
    }

    suspend fun getFileExportRange(): RangeLength = withContext(Dispatchers.IO) {
        mapToRange(
            value = prefsRepo.fileExportRange,
            customStart = prefsRepo.fileExportRangeCustomStart,
            customEnd = prefsRepo.fileExportRangeCustomEnd,
            lastDays = prefsRepo.fileExportRangeLastDays,
        )
    }

    suspend fun setFileExportRange(rangeLength: RangeLength) = withContext(Dispatchers.IO) {
        prefsRepo.fileExportRange = mapRange(rangeLength)

        if (rangeLength is RangeLength.Custom) {
            prefsRepo.fileExportRangeCustomStart = rangeLength.range.timeStarted
            prefsRepo.fileExportRangeCustomEnd = rangeLength.range.timeEnded
        }
        if (rangeLength is RangeLength.Last) {
            prefsRepo.fileExportRangeLastDays = rangeLength.days
        }
    }

    suspend fun getFileExportLastDays(): Int = withContext(Dispatchers.IO) {
        prefsRepo.fileExportRangeLastDays
    }

    suspend fun getCsvExportDateTimeFormat(): ExportDateTimeFormat = withContext(Dispatchers.IO) {
        mapToExportDateTimeFormat(prefsRepo.csvExportDateTimeFormat)
            .takeIf(isExportFormatAvailableInteractor::execute)
            ?: ExportDateTimeFormat.Local
    }

    suspend fun setCsvExportDateTimeFormat(value: ExportDateTimeFormat) = withContext(Dispatchers.IO) {
        prefsRepo.csvExportDateTimeFormat = mapFromExportDateTimeFormat(value)
    }

    suspend fun getCsvExportCustomFileName(): String = withContext(Dispatchers.IO) {
        prefsRepo.csvExportCustomFileName
    }

    suspend fun setCsvExportCustomFileName(value: String) = withContext(Dispatchers.IO) {
        prefsRepo.csvExportCustomFileName = value
    }

    suspend fun getIcsExportCustomFileName(): String = withContext(Dispatchers.IO) {
        prefsRepo.icsExportCustomFileName
    }

    suspend fun setIcsExportCustomFileName(value: String) = withContext(Dispatchers.IO) {
        prefsRepo.icsExportCustomFileName = value
    }

    suspend fun getKeepStatisticsRange(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.keepStatisticsRange
    }

    suspend fun setKeepStatisticsRange(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.keepStatisticsRange = isEnabled
    }

    suspend fun getRetroactiveTrackingMode(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.retroactiveTrackingMode
    }

    suspend fun setRetroactiveTrackingMode(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.retroactiveTrackingMode = isEnabled
    }

    suspend fun getRetroactiveMultitaskingHintWasHidden(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.retroactiveMultitaskingHintWasHidden
    }

    suspend fun setRetroactiveMultitaskingHintWasHidden(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.retroactiveMultitaskingHintWasHidden = isEnabled
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

    suspend fun getShowUntrackedInStatistics(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showUntrackedInStatistics
    }

    suspend fun setShowUntrackedInStatistics(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showUntrackedInStatistics = isEnabled
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

    suspend fun getDaysInCalendar(): DaysInCalendar = withContext(Dispatchers.IO) {
        // Previously was saved with number of days,
        // now with mapped value,
        // 0 is default, not existent.
        when (prefsRepo.daysInCalendar) {
            1 -> DaysInCalendar.ONE
            3 -> DaysInCalendar.THREE
            5 -> DaysInCalendar.FIVE
            7 -> DaysInCalendar.SEVEN
            8 -> DaysInCalendar.WEEK
            else -> DaysInCalendar.ONE
        }
    }

    suspend fun setDaysInCalendar(days: DaysInCalendar) = withContext(Dispatchers.IO) {
        prefsRepo.daysInCalendar = when (days) {
            DaysInCalendar.ONE -> 1
            DaysInCalendar.THREE -> 3
            DaysInCalendar.FIVE -> 5
            DaysInCalendar.SEVEN -> 7
            DaysInCalendar.WEEK -> 8
        }
    }

    suspend fun getShowActivityFilters(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showActivityFilters
    }

    suspend fun setShowActivityFilters(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showActivityFilters = isEnabled
    }

    suspend fun getIsActivityFiltersCollapsed(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.isActivityFiltersCollapsed
    }

    suspend fun setIsActivityFiltersCollapsed(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.isActivityFiltersCollapsed = isEnabled
    }

    suspend fun getEnableRepeatButton(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.enableRepeatButton
    }

    suspend fun setEnableRepeatButton(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.enableRepeatButton = isEnabled
    }

    suspend fun getEnableSearchOnMain(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.enableSearchOnMain
    }

    suspend fun setEnableSearchOnMain(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.enableSearchOnMain = isEnabled
    }

    suspend fun getEnablePomodoroMode(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.enablePomodoroMode
    }

    suspend fun setEnablePomodoroMode(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.enablePomodoroMode = isEnabled
    }

    suspend fun getPomodoroModeStartedTimestampMs(): Long = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroModeStartedTimestamp
    }

    suspend fun setPomodoroModeStartedTimestampMs(timestampMs: Long) = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroModeStartedTimestamp = timestampMs
    }

    suspend fun getPomodoroModePausedTimestampMs(): Long = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroModePausedTimestamp
    }

    suspend fun setPomodoroModePausedTimestampMs(timestampMs: Long) = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroModePausedTimestamp = timestampMs
    }

    suspend fun getPomodoroFocusTime(): Long = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroFocusTime
    }

    suspend fun setPomodoroFocusTime(duration: Long) = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroFocusTime = duration
    }

    suspend fun getPomodoroBreakTime(): Long = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroBreakTime
    }

    suspend fun setPomodoroBreakTime(duration: Long) = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroBreakTime = duration
    }

    suspend fun getPomodoroLongBreakTime(): Long = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroLongBreakTime
    }

    suspend fun setPomodoroLongBreakTime(duration: Long) = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroLongBreakTime = duration
    }

    suspend fun getPomodoroPeriodsUntilLongBreak(): Long = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroPeriodsUntilLongBreak
    }

    suspend fun setPomodoroPeriodsUntilLongBreak(value: Long) = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroPeriodsUntilLongBreak = value
    }

    suspend fun getPomodoroShowMoreControls(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroShowMoreControls
    }

    suspend fun setPomodoroShowMoreControls(value: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.pomodoroShowMoreControls = value
    }

    suspend fun getAllowMultipleActivityFilters(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.allowMultipleActivityFilters
    }

    suspend fun setAllowMultipleActivityFilters(isAllowed: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.allowMultipleActivityFilters = isAllowed
    }

    suspend fun getShowCategoriesAsPredefinedFilters(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showCategoriesAsPredefinedFilters
    }

    suspend fun setShowCategoriesAsPredefinedFilters(isAllowed: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showCategoriesAsPredefinedFilters = isAllowed
    }

    suspend fun getSelectedPredefinedFilters(): List<Long> = withContext(Dispatchers.IO) {
        prefsRepo.selectedPredefinedFilters
            .mapNotNull(String::toLongOrNull)
    }

    suspend fun setSelectedPredefinedFilters(ids: List<Long>) = withContext(Dispatchers.IO) {
        prefsRepo.selectedPredefinedFilters = ids
            .map(Long::toString).toSet()
    }

    suspend fun getShowGoalsSeparately(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showGoalsSeparately
    }

    suspend fun setShowGoalsSeparately(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showGoalsSeparately = isEnabled
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

    suspend fun getShowNotificationsControls(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showNotificationsControls
    }

    suspend fun setShowNotificationsControls(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showNotificationsControls = isEnabled
    }

    suspend fun getShowNotificationEvenWithNoTimers(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showNotificationEvenWithNoTimers
    }

    suspend fun setShowNotificationEvenWithNoTimers(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showNotificationEvenWithNoTimers = isEnabled
    }

    suspend fun getInactivityReminderDuration(): Long = withContext(Dispatchers.IO) {
        prefsRepo.inactivityReminderDuration
    }

    suspend fun setInactivityReminderDuration(duration: Long) = withContext(Dispatchers.IO) {
        prefsRepo.inactivityReminderDuration = duration
    }

    suspend fun getInactivityReminderRecurrent(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.inactivityReminderRecurrent
    }

    suspend fun setInactivityReminderRecurrent(isRecurrent: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.inactivityReminderRecurrent = isRecurrent
    }

    suspend fun getInactivityReminderDoNotDisturbStart(): Long = withContext(Dispatchers.IO) {
        prefsRepo.inactivityReminderDoNotDisturbStart
    }

    suspend fun setInactivityReminderDoNotDisturbStart(start: Long) = withContext(Dispatchers.IO) {
        prefsRepo.inactivityReminderDoNotDisturbStart = start
    }

    suspend fun getInactivityReminderDoNotDisturbEnd(): Long = withContext(Dispatchers.IO) {
        prefsRepo.inactivityReminderDoNotDisturbEnd
    }

    suspend fun setInactivityReminderDoNotDisturbEnd(end: Long) = withContext(Dispatchers.IO) {
        prefsRepo.inactivityReminderDoNotDisturbEnd = end
    }

    suspend fun getActivityReminderDuration(): Long = withContext(Dispatchers.IO) {
        prefsRepo.activityReminderDuration
    }

    suspend fun setActivityReminderDuration(duration: Long) = withContext(Dispatchers.IO) {
        prefsRepo.activityReminderDuration = duration
    }

    suspend fun getActivityReminderRecurrent(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.activityReminderRecurrent
    }

    suspend fun setActivityReminderRecurrent(isRecurrent: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.activityReminderRecurrent = isRecurrent
    }

    suspend fun getActivityReminderDoNotDisturbStart(): Long = withContext(Dispatchers.IO) {
        prefsRepo.activityReminderDoNotDisturbStart
    }

    suspend fun setActivityReminderDoNotDisturbStart(start: Long) = withContext(Dispatchers.IO) {
        prefsRepo.activityReminderDoNotDisturbStart = start
    }

    suspend fun getActivityReminderDoNotDisturbEnd(): Long = withContext(Dispatchers.IO) {
        prefsRepo.activityReminderDoNotDisturbEnd
    }

    suspend fun setActivityReminderDoNotDisturbEnd(end: Long) = withContext(Dispatchers.IO) {
        prefsRepo.activityReminderDoNotDisturbEnd = end
    }

    suspend fun getIgnoreShortRecordsDuration(): Long = withContext(Dispatchers.IO) {
        prefsRepo.ignoreShortRecordsDuration
    }

    suspend fun setIgnoreShortRecordsDuration(duration: Long) = withContext(Dispatchers.IO) {
        prefsRepo.ignoreShortRecordsDuration = duration
    }

    suspend fun getIgnoreShortUntrackedDuration(): Long = withContext(Dispatchers.IO) {
        prefsRepo.ignoreShortUntrackedDuration
    }

    suspend fun setIgnoreShortUntrackedDuration(duration: Long) = withContext(Dispatchers.IO) {
        prefsRepo.ignoreShortUntrackedDuration = duration
    }

    suspend fun getUntrackedRangeEnabled(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.untrackedRangeEnabled
    }

    suspend fun setUntrackedRangeEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.untrackedRangeEnabled = enabled
    }

    suspend fun getUntrackedRangeStart(): Long = withContext(Dispatchers.IO) {
        prefsRepo.untrackedRangeStart
    }

    suspend fun setUntrackedRangeStart(start: Long) = withContext(Dispatchers.IO) {
        prefsRepo.untrackedRangeStart = start
    }

    suspend fun getUntrackedRangeEnd(): Long = withContext(Dispatchers.IO) {
        prefsRepo.untrackedRangeEnd
    }

    suspend fun setUntrackedRangeEnd(end: Long) = withContext(Dispatchers.IO) {
        prefsRepo.untrackedRangeEnd = end
    }

    suspend fun getSelectedDarkMode(): DarkMode = withContext(Dispatchers.IO) {
        when (prefsRepo.darkMode) {
            0 -> DarkMode.System
            1 -> DarkMode.Enabled
            2 -> DarkMode.Disabled
            else -> DarkMode.System
        }
    }

    suspend fun getDarkMode(): Boolean = withContext(Dispatchers.IO) {
        when (getSelectedDarkMode()) {
            DarkMode.Enabled -> true
            DarkMode.Disabled -> false
            DarkMode.System -> isSystemInDarkModeInteractor.execute()
        }
    }

    suspend fun setDarkMode(mode: DarkMode) = withContext(Dispatchers.IO) {
        prefsRepo.darkMode = when (mode) {
            DarkMode.System -> 0
            DarkMode.Enabled -> 1
            DarkMode.Disabled -> 2
        }
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

    suspend fun getUseMonthDayTimeFormat(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.useMonthDayTimeFormat
    }

    suspend fun setUseMonthDayTimeFormat(isUsed: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.useMonthDayTimeFormat = isUsed
    }

    suspend fun getDurationFormat(): DurationFormat = withContext(Dispatchers.IO) {
        when (prefsRepo.durationFormat) {
            0 -> DurationFormat.HOURS
            1 -> DurationFormat.PROPORTIONAL_MINUTES
            2 -> DurationFormat.MINUTES
            3 -> DurationFormat.DAYS
            else -> DurationFormat.HOURS
        }
    }

    suspend fun setDurationFormat(format: DurationFormat) = withContext(Dispatchers.IO) {
        prefsRepo.durationFormat = when (format) {
            DurationFormat.HOURS -> 0
            DurationFormat.PROPORTIONAL_MINUTES -> 1
            DurationFormat.MINUTES -> 2
            DurationFormat.DAYS -> 3
        }
    }

    suspend fun getShowSeconds(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showSeconds
    }

    suspend fun setShowSeconds(isUsed: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showSeconds = isUsed
    }

    suspend fun getKeepScreenOn(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.keepScreenOn
    }

    suspend fun setKeepScreenOn(keep: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.keepScreenOn = keep
    }

    suspend fun getStartTimerByLongClick(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.startTimerByLongClick
    }

    suspend fun setStartTimerByLongClick(enabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.startTimerByLongClick = enabled
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

    suspend fun getRecordTagSelectionExcludeActivities(): List<Long> = withContext(Dispatchers.IO) {
        prefsRepo.recordTagSelectionExcludeActivities
            .mapNotNull(String::toLongOrNull)
    }

    suspend fun setRecordTagSelectionExcludeActivities(value: List<Long>) = withContext(Dispatchers.IO) {
        prefsRepo.recordTagSelectionExcludeActivities = value
            .map(Long::toString).toSet()
    }

    suspend fun getShowCommentInput(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.showCommentInput
    }

    suspend fun setShowCommentInput(value: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.showCommentInput = value
    }

    suspend fun getCommentInputExcludeActivities(): List<Long> = withContext(Dispatchers.IO) {
        prefsRepo.commentInputExcludeActivities
            .mapNotNull(String::toLongOrNull)
    }

    suspend fun setCommentInputExcludeActivities(value: List<Long>) = withContext(Dispatchers.IO) {
        prefsRepo.commentInputExcludeActivities = value
            .map(Long::toString).toSet()
    }

    suspend fun getAutostartPomodoroActivities(): List<Long> = withContext(Dispatchers.IO) {
        prefsRepo.autostartPomodoroActivities
            .mapNotNull(String::toLongOrNull)
    }

    suspend fun setAutostartPomodoroActivities(value: List<Long>) = withContext(Dispatchers.IO) {
        prefsRepo.autostartPomodoroActivities = value
            .map(Long::toString).toSet()
    }

    suspend fun getAutomatedTrackingSendEvents(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.automatedTrackingSendEvents
    }

    suspend fun setAutomatedTrackingSendEvents(value: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.automatedTrackingSendEvents = value
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

    suspend fun getStatisticsWidgetLastDays(widgetId: Int): Int = withContext(Dispatchers.IO) {
        prefsRepo.getStatisticsWidgetLastDays(widgetId)
    }

    suspend fun removeStatisticsWidget(widgetId: Int) = withContext(Dispatchers.IO) {
        prefsRepo.removeStatisticsWidget(widgetId)
    }

    suspend fun setQuickSettingsWidget(widgetId: Int, data: QuickSettingsWidgetType) = withContext(Dispatchers.IO) {
        prefsRepo.setQuickSettingsWidget(widgetId, data)
    }

    suspend fun getQuickSettingsWidget(widgetId: Int): QuickSettingsWidgetType = withContext(Dispatchers.IO) {
        prefsRepo.getQuickSettingsWidget(widgetId)
    }

    suspend fun removeQuickSettingsWidget(widgetId: Int) = withContext(Dispatchers.IO) {
        prefsRepo.removeQuickSettingsWidget(widgetId)
    }

    suspend fun setGridWidget(widgetId: Int, page: Int) = withContext(Dispatchers.IO) {
        prefsRepo.setGridWidget(widgetId, page)
    }

    suspend fun getGridWidget(widgetId: Int): Int = withContext(Dispatchers.IO) {
        prefsRepo.getGridWidget(widgetId)
    }

    suspend fun setGridWidgetData(widgetId: Int, data: GridWidgetData) = withContext(Dispatchers.IO) {
        prefsRepo.setGridWidgetData(widgetId, data)
    }

    suspend fun getGridWidgetData(widgetId: Int): GridWidgetData = withContext(Dispatchers.IO) {
        prefsRepo.getGridWidgetData(widgetId)
    }

    suspend fun removeGridWidget(widgetId: Int) = withContext(Dispatchers.IO) {
        prefsRepo.removeGridWidget(widgetId)
    }

    suspend fun setCardOrderManual(cardsOrder: Map<Long, Long>) = withContext(Dispatchers.IO) {
        prefsRepo.cardOrderManual = mapOrderManual(cardsOrder)
    }

    suspend fun getCardOrderManual(): Map<Long, Long> = withContext(Dispatchers.IO) {
        mapOrderManual(prefsRepo.cardOrderManual)
    }

    suspend fun setCategoryOrderManual(cardsOrder: Map<Long, Long>) = withContext(Dispatchers.IO) {
        prefsRepo.categoryOrderManual = mapOrderManual(cardsOrder)
    }

    suspend fun getCategoryOrderManual(): Map<Long, Long> = withContext(Dispatchers.IO) {
        mapOrderManual(prefsRepo.categoryOrderManual)
    }

    suspend fun setTagOrderManual(cardsOrder: Map<Long, Long>) = withContext(Dispatchers.IO) {
        prefsRepo.tagOrderManual = mapOrderManual(cardsOrder)
    }

    suspend fun getTagOrderManual(): Map<Long, Long> = withContext(Dispatchers.IO) {
        mapOrderManual(prefsRepo.tagOrderManual)
    }

    suspend fun setAutomaticBackupUri(uri: String) = withContext(Dispatchers.IO) {
        prefsRepo.automaticBackupUri = uri
    }

    suspend fun getAutomaticBackupUri(): String = withContext(Dispatchers.IO) {
        prefsRepo.automaticBackupUri
    }

    suspend fun setAutomaticBackupError(isError: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.automaticBackupError = isError
    }

    suspend fun getAutomaticBackupError(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.automaticBackupError
    }

    suspend fun setAutomaticBackupLastSaveTime(value: Long) = withContext(Dispatchers.IO) {
        prefsRepo.automaticBackupLastSaveTime = value
    }

    suspend fun getAutomaticBackupLastSaveTime(): Long = withContext(Dispatchers.IO) {
        prefsRepo.automaticBackupLastSaveTime
    }

    suspend fun setAutomaticBackupTriggerTime(value: Long) = withContext(Dispatchers.IO) {
        prefsRepo.automaticBackupTriggerTime = value
    }

    suspend fun getAutomaticBackupTriggerTime(): Long = withContext(Dispatchers.IO) {
        prefsRepo.automaticBackupTriggerTime
    }

    suspend fun setAutomaticExportUri(uri: String) = withContext(Dispatchers.IO) {
        prefsRepo.automaticExportUri = uri
    }

    suspend fun getAutomaticExportUri(): String = withContext(Dispatchers.IO) {
        prefsRepo.automaticExportUri
    }

    suspend fun setAutomaticExportError(isError: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.automaticExportError = isError
    }

    suspend fun getAutomaticExportError(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.automaticExportError
    }

    suspend fun setAutomaticExportLastSaveTime(value: Long) = withContext(Dispatchers.IO) {
        prefsRepo.automaticExportLastSaveTime = value
    }

    suspend fun getAutomaticExportLastSaveTime(): Long = withContext(Dispatchers.IO) {
        prefsRepo.automaticExportLastSaveTime
    }

    suspend fun setAutomaticExportTriggerTime(value: Long) = withContext(Dispatchers.IO) {
        prefsRepo.automaticExportTriggerTime = value
    }

    suspend fun getAutomaticExportTriggerTime(): Long = withContext(Dispatchers.IO) {
        prefsRepo.automaticExportTriggerTime
    }

    suspend fun getRepeatButtonType(): RepeatButtonType = withContext(Dispatchers.IO) {
        when (prefsRepo.repeatButtonType) {
            0 -> RepeatButtonType.RepeatLast
            1 -> RepeatButtonType.RepeatBeforeLast
            else -> RepeatButtonType.RepeatLast
        }
    }

    suspend fun setRepeatButtonType(mode: RepeatButtonType) = withContext(Dispatchers.IO) {
        prefsRepo.repeatButtonType = when (mode) {
            RepeatButtonType.RepeatLast -> 0
            RepeatButtonType.RepeatBeforeLast -> 1
        }
    }

    suspend fun setWidgetBackgroundTransparencyPercent(value: Long) = withContext(Dispatchers.IO) {
        prefsRepo.widgetBackgroundTransparencyPercent = value
    }

    suspend fun getWidgetBackgroundTransparencyPercent(): Long = withContext(Dispatchers.IO) {
        prefsRepo.widgetBackgroundTransparencyPercent
    }

    suspend fun getDefaultTypesHidden(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.defaultTypesHidden
    }

    suspend fun setDefaultTypesHidden(value: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.defaultTypesHidden = value
    }

    suspend fun getIsNavBarAtTheBottom(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.isNavBarAtTheBottom
    }

    suspend fun setIsNavBarAtTheBottom(value: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.isNavBarAtTheBottom = value
    }

    suspend fun getIsCategoriesSearchEnabled(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.isCategoriesSearchEnabled
    }

    suspend fun setIsCategoriesSearchEnabled(value: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.isCategoriesSearchEnabled = value
    }

    suspend fun getIsArchiveSearchEnabled(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.isArchiveSearchEnabled
    }

    suspend fun setIsArchiveSearchEnabled(value: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.isArchiveSearchEnabled = value
    }

    suspend fun getHiddenCommentFilters(): Set<CommentFilterType> = withContext(Dispatchers.IO) {
        fun map(data: String): CommentFilterType? {
            return when (data.toIntOrNull()) {
                0 -> CommentFilterType.Similar
                1 -> CommentFilterType.Favourite
                2 -> CommentFilterType.Last
                else -> null
            }
        }
        prefsRepo.hiddenCommentFilters.mapNotNull(::map).toSet()
    }

    suspend fun setHiddenCommentFilters(data: Set<CommentFilterType>) = withContext(Dispatchers.IO) {
        fun map(data: CommentFilterType): String {
            return when (data) {
                is CommentFilterType.Similar -> 0
                is CommentFilterType.Favourite -> 1
                is CommentFilterType.Last -> 2
            }.toString()
        }
        prefsRepo.hiddenCommentFilters = data.map(::map).toSet()
    }

    suspend fun getDurationSuggestionsWasPrepopulated(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.durationSuggestionsWasPrepopulated
    }

    suspend fun setDurationSuggestionsWasPrepopulated(value: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.durationSuggestionsWasPrepopulated = value
    }

    suspend fun getTypeAdditionalFieldsShown(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.typeAdditionalFieldsShown
    }

    suspend fun setTypeAdditionalFieldsShown(value: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.typeAdditionalFieldsShown = value
    }

    suspend fun getCategoryAdditionalFieldsShown(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.categoryAdditionalFieldsShown
    }

    suspend fun setCategoryAdditionalFieldsShown(value: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.categoryAdditionalFieldsShown = value
    }

    suspend fun getTagAdditionalFieldsShown(): Boolean = withContext(Dispatchers.IO) {
        prefsRepo.tagAdditionalFieldsShown
    }

    suspend fun setTagAdditionalFieldsShown(value: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.tagAdditionalFieldsShown = value
    }

    suspend fun getStatisticsStreaksType(): StatisticsStreaksType = withContext(Dispatchers.IO) {
        when (prefsRepo.statisticsDetailStreakType) {
            0 -> StatisticsStreaksType.LONGEST
            1 -> StatisticsStreaksType.LATEST
            else -> StatisticsStreaksType.LONGEST
        }
    }

    suspend fun setStatisticsStreaksType(type: StatisticsStreaksType) = withContext(Dispatchers.IO) {
        prefsRepo.statisticsDetailStreakType = when (type) {
            StatisticsStreaksType.LONGEST -> 0
            StatisticsStreaksType.LATEST -> 1
        }
    }

    suspend fun getHiddenContainerOptions(): Set<ContainerOptionsModel> = withContext(Dispatchers.IO) {
        fun map(data: String): ContainerOptionsModel? {
            return when (data.toIntOrNull()) {
                0 -> ContainerOptionsModel.Records.CalendarView
                1 -> ContainerOptionsModel.Records.Filter
                2 -> ContainerOptionsModel.Records.Share
                3 -> ContainerOptionsModel.Records.BackToToday
                4 -> ContainerOptionsModel.Records.SelectDate
                100 -> ContainerOptionsModel.Statistics.Filter
                101 -> ContainerOptionsModel.Statistics.Share
                102 -> ContainerOptionsModel.Statistics.BackToToday
                103 -> ContainerOptionsModel.Statistics.SelectDate
                104 -> ContainerOptionsModel.Statistics.SelectRange
                200 -> ContainerOptionsModel.DetailedStatistics.Compare
                201 -> ContainerOptionsModel.DetailedStatistics.Filter
                202 -> ContainerOptionsModel.DetailedStatistics.SelectDate
                203 -> ContainerOptionsModel.DetailedStatistics.SelectRange
                204 -> ContainerOptionsModel.DetailedStatistics.BackToToday
                else -> null
            }
        }
        prefsRepo.hiddenContainerOptions.mapNotNull(::map).toSet()
    }

    suspend fun setHiddenContainerOptions(data: Set<ContainerOptionsModel>) = withContext(Dispatchers.IO) {
        fun map(data: ContainerOptionsModel): String {
            return when (data) {
                is ContainerOptionsModel.Records.CalendarView -> 0
                is ContainerOptionsModel.Records.Filter -> 1
                is ContainerOptionsModel.Records.Share -> 2
                is ContainerOptionsModel.Records.BackToToday -> 3
                is ContainerOptionsModel.Records.SelectDate -> 4
                is ContainerOptionsModel.Statistics.Filter -> 100
                is ContainerOptionsModel.Statistics.Share -> 101
                is ContainerOptionsModel.Statistics.BackToToday -> 102
                is ContainerOptionsModel.Statistics.SelectDate -> 103
                is ContainerOptionsModel.Statistics.SelectRange -> 104
                is ContainerOptionsModel.DetailedStatistics.Compare -> 200
                is ContainerOptionsModel.DetailedStatistics.Filter -> 201
                is ContainerOptionsModel.DetailedStatistics.SelectDate -> 202
                is ContainerOptionsModel.DetailedStatistics.SelectRange -> 203
                is ContainerOptionsModel.DetailedStatistics.BackToToday -> 204
            }.toString()
        }
        prefsRepo.hiddenContainerOptions = data.map(::map).toSet()
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        prefsRepo.clear()
    }

    suspend fun clearDefaultTypesHidden() = withContext(Dispatchers.IO) {
        prefsRepo.clearDefaultTypesHidden()
    }

    suspend fun clearRetroactiveMultitaskingHidden() = withContext(Dispatchers.IO) {
        prefsRepo.clearRetroactiveMultitaskingHidden()
    }

    suspend fun clearPomodoroSettingsClick() = withContext(Dispatchers.IO) {
        prefsRepo.clearPomodoroSettingsClick()
    }

    suspend fun clearDurationSuggestionsPrepopulated() = withContext(Dispatchers.IO) {
        prefsRepo.clearDurationSuggestionsPrepopulated()
    }

    private fun mapToRange(
        value: Int,
        customStart: Long,
        customEnd: Long,
        lastDays: Int,
    ): RangeLength {
        return when (value) {
            0 -> RangeLength.Day
            1 -> RangeLength.Week
            2 -> RangeLength.Month
            3 -> RangeLength.Year
            4 -> RangeLength.All
            5 -> Range(
                timeStarted = customStart,
                timeEnded = customEnd,
            ).let(RangeLength::Custom)
            6 -> lastDays.let(RangeLength::Last)
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

    private fun mapToCardOrder(data: Int): CardOrder {
        return when (data) {
            0 -> CardOrder.NAME
            1 -> CardOrder.COLOR
            2 -> CardOrder.MANUAL
            else -> CardOrder.NAME
        }
    }

    private fun mapFromCardOrder(data: CardOrder): Int {
        return when (data) {
            CardOrder.NAME -> 0
            CardOrder.COLOR -> 1
            CardOrder.MANUAL -> 2
        }
    }

    private fun mapToCardTagOrder(data: Int): CardTagOrder {
        return when (data) {
            0 -> CardTagOrder.NAME
            1 -> CardTagOrder.COLOR
            2 -> CardTagOrder.MANUAL
            3 -> CardTagOrder.ACTIVITY
            else -> CardTagOrder.NAME
        }
    }

    private fun mapFromCardTagOrder(data: CardTagOrder): Int {
        return when (data) {
            CardTagOrder.NAME -> 0
            CardTagOrder.COLOR -> 1
            CardTagOrder.MANUAL -> 2
            CardTagOrder.ACTIVITY -> 3
        }
    }

    private fun mapToChartFilterType(data: Int): ChartFilterType {
        return when (data) {
            0 -> ChartFilterType.ACTIVITY
            1 -> ChartFilterType.CATEGORY
            2 -> ChartFilterType.RECORD_TAG
            else -> ChartFilterType.ACTIVITY
        }
    }

    private fun mapFromChartFilterType(data: ChartFilterType): Int {
        return when (data) {
            ChartFilterType.ACTIVITY -> 0
            ChartFilterType.CATEGORY -> 1
            ChartFilterType.RECORD_TAG -> 2
        }
    }

    private fun mapToExportDateTimeFormat(data: Int): ExportDateTimeFormat {
        return when (data) {
            0 -> ExportDateTimeFormat.Local
            1 -> ExportDateTimeFormat.Utc
            2 -> ExportDateTimeFormat.TimeZone
            else -> ExportDateTimeFormat.Local
        }
    }

    private fun mapFromExportDateTimeFormat(data: ExportDateTimeFormat): Int {
        return when (data) {
            ExportDateTimeFormat.Local -> 0
            ExportDateTimeFormat.Utc -> 1
            ExportDateTimeFormat.TimeZone -> 2
        }
    }

    private fun mapOrderManual(
        cardOrder: Map<Long, Long>,
    ): Set<String> {
        return cardOrder.map { (typeId, order) ->
            "$typeId$CARDS_ORDER_DELIMITER${order.toShort()}"
        }.toSet()
    }

    private fun mapOrderManual(
        set: Set<String>?,
    ): Map<Long, Long> {
        return set
            ?.associate { string ->
                string.split(CARDS_ORDER_DELIMITER).let { parts ->
                    parts.getOrNull(0).orEmpty() to parts.getOrNull(1).orEmpty()
                }
            }
            ?.mapKeys { it.key.toLongOrNull().orZero() }
            ?.mapValues { it.value.toLongOrNull().orZero() }
            ?: emptyMap()
    }

    companion object {
        private const val CARDS_ORDER_DELIMITER = "_"
    }
}