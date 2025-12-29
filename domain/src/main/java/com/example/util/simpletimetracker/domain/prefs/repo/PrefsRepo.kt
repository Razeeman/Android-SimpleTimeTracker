package com.example.util.simpletimetracker.domain.prefs.repo

import com.example.util.simpletimetracker.domain.statistics.model.StatisticsWidgetData
import com.example.util.simpletimetracker.domain.widget.model.QuickSettingsWidgetType

interface PrefsRepo {

    var recordTypesFilteredOnList: Set<String>
    var categoriesFilteredOnList: Set<String>
    var tagsFilteredOnList: Set<String>
    var listFilterType: Int

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

    var csvExportDateTimeFormat: Int

    var csvExportCustomFileName: String
    var icsExportCustomFileName: String

    var keepStatisticsRange: Boolean

    var retroactiveTrackingMode: Boolean

    var retroactiveMultitaskingHintWasHidden: Boolean

    var firstDayOfWeek: Int

    var startOfDayShift: Long // in milliseconds

    var showUntrackedInRecords: Boolean

    var showUntrackedInStatistics: Boolean

    var showRecordsCalendar: Boolean

    var reverseOrderInCalendar: Boolean

    var daysInCalendar: Int

    var showActivityFilters: Boolean

    var isActivityFiltersCollapsed: Boolean

    var allowMultipleActivityFilters: Boolean

    var showCategoriesAsPredefinedFilters: Boolean

    var selectedPredefinedFilters: Set<String>

    var enableRepeatButton: Boolean

    var enableSearchOnMain: Boolean

    var enablePomodoroMode: Boolean

    var pomodoroModeStartedTimestamp: Long // in milliseconds, 0 - disabled

    var pomodoroModePausedTimestamp: Long // in milliseconds, 0 - disabled

    var pomodoroFocusTime: Long // in seconds, 0 - disabled

    var pomodoroBreakTime: Long // in seconds, 0 - disabled

    var pomodoroLongBreakTime: Long // in seconds, 0 - disabled

    var pomodoroPeriodsUntilLongBreak: Long // 0 - disabled

    var pomodoroShowMoreControls: Boolean

    var showGoalsSeparately: Boolean

    var allowMultitasking: Boolean

    var showNotifications: Boolean

    var showNotificationsControls: Boolean

    var showNotificationEvenWithNoTimers: Boolean

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

    var durationFormat: Int

    var showSeconds: Boolean

    var keepScreenOn: Boolean

    var startTimerByLongClick: Boolean

    var showRecordTagSelection: Boolean

    var recordTagSelectionCloseAfterOne: Boolean

    var recordTagSelectionExcludeActivities: Set<String>

    var showCommentInput: Boolean

    var commentInputExcludeActivities: Set<String>

    var autostartPomodoroActivities: Set<String>

    var automatedTrackingSendEvents: Boolean

    var automaticBackupUri: String

    var automaticBackupError: Boolean

    var automaticBackupLastSaveTime: Long

    var automaticBackupTriggerTime: Long

    var automaticExportUri: String

    var automaticExportError: Boolean

    var automaticExportLastSaveTime: Long

    var automaticExportTriggerTime: Long

    var repeatButtonType: Int

    var widgetBackgroundTransparencyPercent: Long

    var defaultTypesHidden: Boolean

    var isNavBarAtTheBottom: Boolean

    var isCategoriesSearchEnabled: Boolean

    var isArchiveSearchEnabled: Boolean

    var hiddenCommentFilters: Set<String>

    var durationSuggestionsWasPrepopulated: Boolean

    var typeAdditionalFieldsShown: Boolean

    var categoryAdditionalFieldsShown: Boolean

    var tagAdditionalFieldsShown: Boolean

    var statisticsDetailStreakType: Int

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
    fun clearRetroactiveMultitaskingHidden()
    fun clearPomodoroSettingsClick()
    fun clearDurationSuggestionsPrepopulated()

    fun hasValueSaved(key: String): Boolean
}