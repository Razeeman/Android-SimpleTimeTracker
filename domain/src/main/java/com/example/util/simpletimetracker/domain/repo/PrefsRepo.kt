package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.QuickSettingsWidgetType
import com.example.util.simpletimetracker.domain.model.StatisticsWidgetData

interface PrefsRepo {

    var recordTypesFilteredOnList: Set<String>

    var recordTypesFilteredOnChart: Set<String>

    var categoriesFilteredOnChart: Set<String>

    var tagsFilteredOnChart: Set<String>

    var chartFilterType: Int

    var cardOrder: Int
    val hasCardOrder: Boolean
    var categoryOrder: Int
    var tagOrder: Int

    var cardOrderManual: Set<String>
    var categoryOrderManual: Set<String>
    var tagOrderManual: Set<String>

    var statisticsRange: Int
    var statisticsRangeCustomStart: Long
    var statisticsRangeCustomEnd: Long
    var statisticsRangeLastDays: Int

    var statisticsDetailRange: Int
    var statisticsDetailRangeCustomStart: Long
    var statisticsDetailRangeCustomEnd: Long
    var statisticsDetailRangeLastDays: Int

    var fileExportRange: Int
    var fileExportRangeCustomStart: Long
    var fileExportRangeCustomEnd: Long
    var fileExportRangeLastDays: Int

    var keepStatisticsRange: Boolean

    var firstDayOfWeek: Int

    var startOfDayShift: Long // in milliseconds

    var showUntrackedInRecords: Boolean

    var showUntrackedInStatistics: Boolean

    var showRecordsCalendar: Boolean

    var showCalendarButtonOnRecordsTab: Boolean

    var reverseOrderInCalendar: Boolean

    var daysInCalendar: Int

    var showActivityFilters: Boolean

    var enableRepeatButton: Boolean

    var enablePomodoroMode: Boolean

    var pomodoroModeStartedTimestamp: Long // in milliseconds, 0 - disabled

    var pomodoroFocusTime: Long // in seconds, 0 - disabled

    var pomodoroBreakTime: Long // in seconds, 0 - disabled

    var pomodoroLongBreakTime: Long // in seconds, 0 - disabled

    var pomodoroPeriodsUntilLongBreak: Long // 0 - disabled

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

    var autostartPomodoroActivities: Set<String>

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

    var isNavBarAtTheBottom: Boolean

    fun setWidget(widgetId: Int, recordType: Long)

    fun getWidget(widgetId: Int): Long

    fun removeWidget(widgetId: Int)

    fun setStatisticsWidget(widgetId: Int, data: StatisticsWidgetData)

    fun getStatisticsWidget(widgetId: Int): StatisticsWidgetData

    fun getStatisticsWidgetLastDays(widgetId: Int): Int

    fun removeStatisticsWidget(widgetId: Int)

    fun setQuickSettingsWidget(widgetId: Int, data: QuickSettingsWidgetType)

    fun getQuickSettingsWidget(widgetId: Int): QuickSettingsWidgetType

    fun removeQuickSettingsWidget(widgetId: Int)

    fun clear()
    fun clearDefaultTypesHidden()
    fun clearPomodoroSettingsClick()

    fun hasValueSaved(key: String): Boolean
}