package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.QuickSettingsWidgetType
import com.example.util.simpletimetracker.domain.model.StatisticsWidgetData

interface PrefsRepo {

    var recordTypesFilteredOnChart: Set<String>

    var categoriesFilteredOnChart: Set<String>

    var tagsFilteredOnChart: Set<String>

    var chartFilterType: Int

    var cardOrder: Int
    var categoryOrder: Int
    var tagOrder: Int

    var statisticsRange: Int
    var statisticsRangeCustomStart: Long
    var statisticsRangeCustomEnd: Long

    var statisticsDetailRange: Int
    var statisticsDetailRangeCustomStart: Long
    var statisticsDetailRangeCustomEnd: Long

    var keepStatisticsRange: Boolean

    var firstDayOfWeek: Int

    var startOfDayShift: Long // in milliseconds

    var showUntrackedInRecords: Boolean

    var showUntrackedInStatistics: Boolean

    var showRecordsCalendar: Boolean

    var reverseOrderInCalendar: Boolean

    var daysInCalendar: Int

    var showActivityFilters: Boolean

    var allowMultipleActivityFilters: Boolean

    var showGoalsSeparately: Boolean

    var allowMultitasking: Boolean

    var showNotifications: Boolean

    var showNotificationsControls: Boolean

    var inactivityReminderDuration: Long // in seconds

    var inactivityReminderRecurrent: Boolean

    var inactivityReminderDoNotDisturbStart: Long // in milliseconds

    var inactivityReminderDoNotDisturbEnd: Long // in milliseconds

    var activityReminderDuration: Long // in seconds

    var activityReminderRecurrent: Boolean

    var activityReminderDoNotDisturbStart: Long // in milliseconds

    var activityReminderDoNotDisturbEnd: Long // in milliseconds

    var ignoreShortRecordsDuration: Long // in seconds

    var ignoreShortUntrackedDuration: Long // in seconds

    var untrackedRangeEnabled: Boolean

    var untrackedRangeStart: Long // in milliseconds

    var untrackedRangeEnd: Long // in milliseconds

    var darkMode: Int

    var numberOfCards: Int

    var useMilitaryTimeFormat: Boolean

    var useMonthDayTimeFormat: Boolean

    var useProportionalMinutes: Boolean

    var showSeconds: Boolean

    var keepScreenOn: Boolean

    var showRecordTagSelection: Boolean

    var recordTagSelectionCloseAfterOne: Boolean

    var recordTagSelectionExcludeActivities: Set<String>

    var automatedTrackingSendEvents: Boolean

    var automaticBackupUri: String

    var automaticBackupError: Boolean

    var automaticBackupLastSaveTime: Long

    var automaticExportUri: String

    var automaticExportError: Boolean

    var automaticExportLastSaveTime: Long

    var repeatButtonType: Int

    var widgetBackgroundTransparencyPercent: Long

    var defaultTypesHidden: Boolean

    fun setWidget(widgetId: Int, recordType: Long)

    fun getWidget(widgetId: Int): Long

    fun removeWidget(widgetId: Int)

    fun setStatisticsWidget(widgetId: Int, data: StatisticsWidgetData)

    fun getStatisticsWidget(widgetId: Int): StatisticsWidgetData

    fun removeStatisticsWidget(widgetId: Int)

    fun setQuickSettingsWidget(widgetId: Int, data: QuickSettingsWidgetType)

    fun getQuickSettingsWidget(widgetId: Int): QuickSettingsWidgetType

    fun removeQuickSettingsWidget(widgetId: Int)

    fun setCardOrderManual(cardOrder: Map<Long, Long>)

    fun getCardOrderManual(): Map<Long, Long>

    fun setCategoryOrderManual(cardOrder: Map<Long, Long>)

    fun getCategoryOrderManual(): Map<Long, Long>

    fun setTagOrderManual(cardOrder: Map<Long, Long>)

    fun getTagOrderManual(): Map<Long, Long>

    fun clear()
}