package com.example.util.simpletimetracker.data_local.repo

import android.content.SharedPreferences
import com.example.util.simpletimetracker.data_local.utils.delegate
import com.example.util.simpletimetracker.data_local.utils.logPrefsDataAccess
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.QuickSettingsWidgetType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.StatisticsWidgetData
import com.example.util.simpletimetracker.domain.repo.PrefsRepo
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsRepoImpl @Inject constructor(
    private val prefs: SharedPreferences,
) : PrefsRepo {

    private val firstDayOfWeekDefault: Int by lazy {
        Calendar.getInstance().firstDayOfWeek
    }

    override var recordTypesFilteredOnList: Set<String> by prefs.delegate(
        KEY_RECORD_TYPES_FILTERED_ON_LIST, emptySet(),
    )

    override var recordTypesFilteredOnChart: Set<String> by prefs.delegate(
        KEY_RECORD_TYPES_FILTERED_ON_CHART, emptySet(),
    )

    override var categoriesFilteredOnChart: Set<String> by prefs.delegate(
        KEY_CATEGORIES_TYPES_FILTERED_ON_CHART, emptySet(),
    )

    override var tagsFilteredOnChart: Set<String> by prefs.delegate(
        KEY_TAGS_FILTERED_ON_CHART, emptySet(),
    )

    override var chartFilterType: Int by prefs.delegate(
        KEY_CHART_FILTER_TYPE, 0,
    )

    override var cardOrder: Int by prefs.delegate(
        KEY_CARD_ORDER, 0, // Default to name sort.
    )

    override val hasCardOrder: Boolean
        get() = prefs.contains(KEY_CARD_ORDER)

    override var categoryOrder: Int by prefs.delegate(
        KEY_CATEGORY_ORDER, 0, // Default to name sort.
    )

    override var tagOrder: Int by prefs.delegate(
        KEY_TAG_ORDER, 3, // Default to activity sort.
    )

    override var cardOrderManual: Set<String> by prefs.delegate(
        KEY_CARD_ORDER_MANUAL, emptySet(),
    )

    override var categoryOrderManual: Set<String> by prefs.delegate(
        KEY_CATEGORY_ORDER_MANUAL, emptySet(),
    )

    override var tagOrderManual: Set<String> by prefs.delegate(
        KEY_TAG_ORDER_MANUAL, emptySet(),
    )

    override var statisticsRange: Int by prefs.delegate(
        KEY_STATISTICS_RANGE, 0,
    )

    override var statisticsRangeCustomStart: Long by prefs.delegate(
        KEY_STATISTICS_RANGE_CUSTOM_START, 0,
    )

    override var statisticsRangeCustomEnd: Long by prefs.delegate(
        KEY_STATISTICS_RANGE_CUSTOM_END, 0,
    )

    override var statisticsRangeLastDays: Int by prefs.delegate(
        KEY_STATISTICS_RANGE_LAST_DAYS, RANGE_LAST_DAYS_DEFAULT,
    )

    override var statisticsDetailRange: Int by prefs.delegate(
        KEY_STATISTICS_DETAIL_RANGE, 0,
    )

    override var statisticsDetailRangeCustomStart: Long by prefs.delegate(
        KEY_STATISTICS_DETAIL_RANGE_CUSTOM_START, 0,
    )

    override var statisticsDetailRangeCustomEnd: Long by prefs.delegate(
        KEY_STATISTICS_DETAIL_RANGE_CUSTOM_END, 0,
    )

    override var statisticsDetailRangeLastDays: Int by prefs.delegate(
        KEY_STATISTICS_DETAIL_RANGE_LAST_DAYS, RANGE_LAST_DAYS_DEFAULT,
    )

    override var fileExportRange: Int by prefs.delegate(
        KEY_FILE_EXPORT_RANGE, 0,
    )

    override var fileExportRangeCustomStart: Long by prefs.delegate(
        KEY_FILE_EXPORT_RANGE_CUSTOM_START, 0,
    )

    override var fileExportRangeCustomEnd: Long by prefs.delegate(
        KEY_FILE_EXPORT_RANGE_CUSTOM_END, 0,
    )

    override var fileExportRangeLastDays: Int by prefs.delegate(
        KEY_FILE_EXPORT_RANGE_LAST_DAYS, RANGE_LAST_DAYS_DEFAULT,
    )

    override var csvExportCustomFileName: String by prefs.delegate(
        KEY_CSV_EXPORT_CUSTOM_FILENAME, "",
    )

    override var icsExportCustomFileName: String by prefs.delegate(
        KEY_ICS_EXPORT_CUSTOM_FILENAME, "",
    )

    override var keepStatisticsRange: Boolean by prefs.delegate(
        KEY_KEEP_STATISTICS_RANGE, false,
    )

    override var firstDayOfWeek: Int by prefs.delegate(
        KEY_FIRST_DAY_OF_WEEK, firstDayOfWeekDefault,
    )

    override var startOfDayShift: Long by prefs.delegate(
        KEY_START_OF_DAY_SHIFT, 0,
    )

    override var showUntrackedInRecords: Boolean by prefs.delegate(
        KEY_SHOW_UNTRACKED_IN_RECORDS, false,
    )

    override var showUntrackedInStatistics: Boolean by prefs.delegate(
        KEY_SHOW_UNTRACKED_IN_STATISTICS, false,
    )

    override var showRecordsCalendar: Boolean by prefs.delegate(
        KEY_SHOW_RECORDS_CALENDAR, false,
    )

    override var showCalendarButtonOnRecordsTab: Boolean by prefs.delegate(
        KEY_SHOW_CALENDAR_BUTTON_ON_RECORDS_TAB, false,
    )

    override var reverseOrderInCalendar: Boolean by prefs.delegate(
        KEY_REVERSE_ORDER_IN_CALENDAR, false,
    )

    override var daysInCalendar: Int by prefs.delegate(
        KEY_DAYS_IN_CALENDAR, 0,
    )

    override var showActivityFilters: Boolean by prefs.delegate(
        KEY_SHOW_ACTIVITY_FILTERS, false,
    )

    override var enableRepeatButton: Boolean by prefs.delegate(
        KEY_ENABLE_REPEAT_BUTTON, false,
    )

    override var enablePomodoroMode: Boolean by prefs.delegate(
        KEY_ENABLE_POMODORO_MODE, true,
    )

    override var pomodoroModeStartedTimestamp: Long by prefs.delegate(
        KEY_POMODORO_MODE_STARTED_TIMESTAMP, 0,
    )

    override var pomodoroFocusTime: Long by prefs.delegate(
        KEY_POMODORO_FOCUS_TIME, POMODORO_DEFAULT_FOCUS_TIME_SEC,
    )

    override var pomodoroBreakTime: Long by prefs.delegate(
        KEY_POMODORO_BREAK_TIME, POMODORO_DEFAULT_BREAK_TIME_SEC,
    )

    override var pomodoroLongBreakTime: Long by prefs.delegate(
        KEY_POMODORO_LONG_BREAK_TIME, POMODORO_DEFAULT_LONG_BREAK_TIME_SEC,
    )

    override var pomodoroPeriodsUntilLongBreak: Long by prefs.delegate(
        KEY_POMODORO_PERIODS_UNTIL_LONG_BREAK, POMODORO_DEFAULT_UNTIL_LONG_BREAK,
    )

    override var allowMultipleActivityFilters: Boolean by prefs.delegate(
        KEY_ALLOW_MULTIPLE_ACTIVITY_FILTERS, true,
    )

    override var showGoalsSeparately: Boolean by prefs.delegate(
        KEY_SHOW_GOALS_SEPARATELY, false,
    )

    override var allowMultitasking: Boolean by prefs.delegate(
        KEY_ALLOW_MULTITASKING, true,
    )

    override var showNotifications: Boolean by prefs.delegate(
        KEY_SHOW_NOTIFICATIONS, false,
    )

    override var showNotificationsControls: Boolean by prefs.delegate(
        KEY_SHOW_NOTIFICATIONS_CONTROLS, true,
    )

    override var inactivityReminderDuration: Long by prefs.delegate(
        KEY_INACTIVITY_REMINDER_DURATION, 0, // 0 is for disabled
    )

    override var inactivityReminderRecurrent: Boolean by prefs.delegate(
        KEY_INACTIVITY_REMINDER_RECURRENT, false,
    )

    override var inactivityReminderDoNotDisturbStart: Long by prefs.delegate(
        KEY_INACTIVITY_REMINDER_DND_START, DO_NOT_DISTURB_PERIOD_START,
    )

    override var inactivityReminderDoNotDisturbEnd: Long by prefs.delegate(
        KEY_INACTIVITY_REMINDER_DND_END, DO_NOT_DISTURB_PERIOD_END,
    )

    override var activityReminderDuration: Long by prefs.delegate(
        KEY_ACTIVITY_REMINDER_DURATION, 0, // 0 is for disabled
    )

    override var activityReminderRecurrent: Boolean by prefs.delegate(
        KEY_ACTIVITY_REMINDER_RECURRENT, false,
    )

    override var activityReminderDoNotDisturbStart: Long by prefs.delegate(
        KEY_ACTIVITY_REMINDER_DND_START, DO_NOT_DISTURB_PERIOD_START,
    )

    override var activityReminderDoNotDisturbEnd: Long by prefs.delegate(
        KEY_ACTIVITY_REMINDER_DND_END, DO_NOT_DISTURB_PERIOD_END,
    )

    override var ignoreShortRecordsDuration: Long by prefs.delegate(
        KEY_IGNORE_SHORT_RECORDS_DURATION, 0, // 0 is for disabled
    )

    override var ignoreShortUntrackedDuration: Long by prefs.delegate(
        KEY_IGNORE_SHORT_UNTRACKED_DURATION, 60, // 0 is for disabled
    )

    override var untrackedRangeEnabled: Boolean by prefs.delegate(
        KEY_UNTRACKED_RANGE_ENABLED, false,
    )

    override var untrackedRangeStart: Long by prefs.delegate(
        KEY_UNTRACKED_RANGE_START, 0,
    )

    override var untrackedRangeEnd: Long by prefs.delegate(
        KEY_UNTRACKED_RANGE_END, 0,
    )

    override var darkMode: Int by prefs.delegate(
        KEY_DARK_MODE_2, 0,
    )

    override var numberOfCards: Int by prefs.delegate(
        KEY_NUMBER_OF_CARDS, 0,
    )

    override var useMilitaryTimeFormat: Boolean by prefs.delegate(
        KEY_USE_MILITARY_TIME_FORMAT, true,
    )

    override var useMonthDayTimeFormat: Boolean by prefs.delegate(
        KEY_USE_MONTH_DAY_TIME_FORMAT, false,
    )

    override var useProportionalMinutes: Boolean by prefs.delegate(
        KEY_USE_PROPORTIONAL_MINUTES, false,
    )

    override var showSeconds: Boolean by prefs.delegate(
        KEY_SHOW_SECONDS, false,
    )

    override var keepScreenOn: Boolean by prefs.delegate(
        KEY_KEEP_SCREEN_ON, false,
    )

    override var showRecordTagSelection: Boolean by prefs.delegate(
        KEY_SHOW_RECORD_TAG_SELECTION, false,
    )

    override var recordTagSelectionCloseAfterOne: Boolean by prefs.delegate(
        KEY_RECORD_TAG_SELECTION_CLOSE_AFTER_ONE, false,
    )

    override var recordTagSelectionExcludeActivities: Set<String> by prefs.delegate(
        KEY_SHOW_RECORD_TAG_SELECTION_EXCLUDE_ACTIVITIES, emptySet(),
    )

    override var autostartPomodoroActivities: Set<String> by prefs.delegate(
        KEY_AUTOSTART_POMODORO_ACTIVITIES, emptySet(),
    )

    override var automatedTrackingSendEvents: Boolean by prefs.delegate(
        KEY_AUTOMATED_TRACKING_SEND_EVENTS, false,
    )

    override var automaticBackupUri: String by prefs.delegate(
        KEY_AUTOMATIC_BACKUP_URI, "",
    )

    override var automaticBackupError: Boolean by prefs.delegate(
        KEY_AUTOMATIC_BACKUP_ERROR, false,
    )

    override var automaticBackupLastSaveTime: Long by prefs.delegate(
        KEY_AUTOMATIC_BACKUP_LAST_SAVE_TIME, 0,
    )

    override var automaticExportUri: String by prefs.delegate(
        KEY_AUTOMATIC_EXPORT_URI, "",
    )

    override var automaticExportError: Boolean by prefs.delegate(
        KEY_AUTOMATIC_EXPORT_ERROR, false,
    )

    override var automaticExportLastSaveTime: Long by prefs.delegate(
        KEY_AUTOMATIC_EXPORT_LAST_SAVE_TIME, 0,
    )

    override var repeatButtonType: Int by prefs.delegate(
        KEY_REPEAT_BUTTON_TYPE, 0,
    )

    override var widgetBackgroundTransparencyPercent: Long by prefs.delegate(
        KEY_WIDGET_TRANSPARENCY_PERCENT, 60,
    )

    override var defaultTypesHidden: Boolean by prefs.delegate(
        KEY_DEFAULT_TYPES_HIDDEN, false,
    )

    override var isNavBarAtTheBottom: Boolean by prefs.delegate(
        KEY_IS_NAV_BAR_AT_THE_BOTTOM, false,
    )

    override fun setWidget(widgetId: Int, recordType: Long) {
        val key = KEY_WIDGET + widgetId
        logPrefsDataAccess("set $key")
        prefs.edit().putLong(key, recordType).apply()
    }

    override fun getWidget(widgetId: Int): Long {
        val key = KEY_WIDGET + widgetId
        logPrefsDataAccess("get $key")
        return prefs.getLong(key, 0)
    }

    override fun removeWidget(widgetId: Int) {
        val key = KEY_WIDGET + widgetId
        logPrefsDataAccess("remove $key")
        prefs.edit().remove(key).apply()
    }

    override fun setStatisticsWidget(widgetId: Int, data: StatisticsWidgetData) {
        logPrefsDataAccess("setStatisticsWidget $widgetId")
        val filterTypeData = when (data.chartFilterType) {
            ChartFilterType.ACTIVITY -> 0
            ChartFilterType.CATEGORY -> 1
            ChartFilterType.RECORD_TAG -> 2
        }
        val rangeData = when (data.rangeLength) {
            is RangeLength.Day -> 0
            is RangeLength.Week -> 1
            is RangeLength.Month -> 2
            is RangeLength.Year -> 3
            is RangeLength.All -> 4
            is RangeLength.Last -> 5
            is RangeLength.Custom -> 0 // Not possible
        }
        val rangeDataLastDays = (data.rangeLength as? RangeLength.Last)?.days
        val filteredTypesData = data.filteredTypes.map(Long::toString).toSet()
        val filteredCategoriesData = data.filteredCategories.map(Long::toString).toSet()
        val filteredTagsData = data.filteredTags.map(Long::toString).toSet()

        prefs.edit()
            .putInt(KEY_STATISTICS_WIDGET_FILTER_TYPE + widgetId, filterTypeData)
            .putInt(KEY_STATISTICS_WIDGET_RANGE + widgetId, rangeData)
            .apply {
                if (rangeDataLastDays != null) {
                    putInt(KEY_STATISTICS_WIDGET_RANGE_LAST_DAYS + widgetId, rangeDataLastDays)
                }
            }
            .putStringSet(KEY_STATISTICS_WIDGET_FILTERED_TYPES + widgetId, filteredTypesData)
            .putStringSet(KEY_STATISTICS_WIDGET_FILTERED_CATEGORIES + widgetId, filteredCategoriesData)
            .putStringSet(KEY_STATISTICS_WIDGET_FILTERED_TAGS + widgetId, filteredTagsData)
            .apply()
    }

    override fun getStatisticsWidget(widgetId: Int): StatisticsWidgetData {
        logPrefsDataAccess("getStatisticsWidget $widgetId")
        val filterType = when (prefs.getInt(KEY_STATISTICS_WIDGET_FILTER_TYPE + widgetId, 0)) {
            0 -> ChartFilterType.ACTIVITY
            1 -> ChartFilterType.CATEGORY
            2 -> ChartFilterType.RECORD_TAG
            else -> ChartFilterType.ACTIVITY
        }
        val range = when (prefs.getInt(KEY_STATISTICS_WIDGET_RANGE + widgetId, 0)) {
            0 -> RangeLength.Day
            1 -> RangeLength.Week
            2 -> RangeLength.Month
            3 -> RangeLength.Year
            4 -> RangeLength.All
            5 -> RangeLength.Last(
                days = getStatisticsWidgetLastDays(widgetId),
            )
            else -> RangeLength.Day
        }
        val filteredTypes = prefs
            .getStringSet(KEY_STATISTICS_WIDGET_FILTERED_TYPES + widgetId, emptySet())
            ?.mapNotNull { it.toLongOrNull() }.orEmpty().toSet()
        val filteredCategories = prefs
            .getStringSet(KEY_STATISTICS_WIDGET_FILTERED_CATEGORIES + widgetId, emptySet())
            ?.mapNotNull { it.toLongOrNull() }.orEmpty().toSet()
        val filteredTags = prefs
            .getStringSet(KEY_STATISTICS_WIDGET_FILTERED_TAGS + widgetId, emptySet())
            ?.mapNotNull { it.toLongOrNull() }.orEmpty().toSet()

        return StatisticsWidgetData(
            chartFilterType = filterType,
            rangeLength = range,
            filteredTypes = filteredTypes,
            filteredCategories = filteredCategories,
            filteredTags = filteredTags,
        )
    }

    override fun getStatisticsWidgetLastDays(widgetId: Int): Int {
        return prefs.getInt(
            KEY_STATISTICS_WIDGET_RANGE_LAST_DAYS + widgetId,
            RANGE_LAST_DAYS_DEFAULT,
        )
    }

    override fun removeStatisticsWidget(widgetId: Int) {
        logPrefsDataAccess("removeStatisticsWidget $widgetId")
        prefs.edit()
            .remove(KEY_STATISTICS_WIDGET_FILTER_TYPE + widgetId)
            .remove(KEY_STATISTICS_WIDGET_RANGE + widgetId)
            .remove(KEY_STATISTICS_WIDGET_RANGE_LAST_DAYS + widgetId)
            .remove(KEY_STATISTICS_WIDGET_FILTERED_TYPES + widgetId)
            .remove(KEY_STATISTICS_WIDGET_FILTERED_CATEGORIES + widgetId)
            .remove(KEY_STATISTICS_WIDGET_FILTERED_TAGS + widgetId)
            .apply()
    }

    override fun setQuickSettingsWidget(widgetId: Int, data: QuickSettingsWidgetType) {
        val key = KEY_QUICK_SETTINGS_WIDGET_TYPE + widgetId
        logPrefsDataAccess("set $widgetId")
        val type = when (data) {
            is QuickSettingsWidgetType.AllowMultitasking -> 0L
            is QuickSettingsWidgetType.ShowRecordTagSelection -> 1L
        }
        prefs.edit().putLong(key, type).apply()
    }

    override fun getQuickSettingsWidget(widgetId: Int): QuickSettingsWidgetType {
        val key = KEY_QUICK_SETTINGS_WIDGET_TYPE + widgetId
        logPrefsDataAccess("get $key")
        return when (prefs.getLong(key, 0)) {
            0L -> QuickSettingsWidgetType.AllowMultitasking
            1L -> QuickSettingsWidgetType.ShowRecordTagSelection
            else -> QuickSettingsWidgetType.AllowMultitasking
        }
    }

    override fun removeQuickSettingsWidget(widgetId: Int) {
        val key = KEY_QUICK_SETTINGS_WIDGET_TYPE + widgetId
        logPrefsDataAccess("remove $key")
        prefs.edit().remove(key).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    override fun clearDefaultTypesHidden() {
        prefs.edit().remove(KEY_DEFAULT_TYPES_HIDDEN).apply()
    }

    override fun clearPomodoroSettingsClick() {
        prefs.edit().remove(KEY_POMODORO_FOCUS_TIME).apply()
        prefs.edit().remove(KEY_POMODORO_BREAK_TIME).apply()
        prefs.edit().remove(KEY_POMODORO_LONG_BREAK_TIME).apply()
        prefs.edit().remove(KEY_POMODORO_PERIODS_UNTIL_LONG_BREAK).apply()
    }

    override fun hasValueSaved(key: String): Boolean {
        return prefs.contains(key)
    }

    @Suppress("unused")
    companion object {
        private const val DO_NOT_DISTURB_PERIOD_START: Long = 0 // midnight
        private const val DO_NOT_DISTURB_PERIOD_END: Long = 1000 * 60 * 60 * 8 // 8 hours in the morning
        private const val POMODORO_DEFAULT_FOCUS_TIME_SEC: Long = 60 * 25 // 25 min
        private const val POMODORO_DEFAULT_BREAK_TIME_SEC: Long = 60 * 5 // 5 min
        private const val POMODORO_DEFAULT_LONG_BREAK_TIME_SEC: Long = 60 * 15 // 15 min
        private const val POMODORO_DEFAULT_UNTIL_LONG_BREAK: Long = 4
        private const val RANGE_LAST_DAYS_DEFAULT: Int = 7

        const val KEY_RECORD_TYPES_FILTERED_ON_LIST = "recordTypesFilteredOnList"
        const val KEY_RECORD_TYPES_FILTERED_ON_CHART = "recordTypesFilteredOnChart"
        const val KEY_CATEGORIES_TYPES_FILTERED_ON_CHART = "categoriesFilteredOnChart"
        const val KEY_TAGS_FILTERED_ON_CHART = "tagsFilteredOnChart"
        const val KEY_CHART_FILTER_TYPE = "chartFilterType"
        const val KEY_CARD_ORDER = "cardOrder"
        const val KEY_CATEGORY_ORDER = "categoryOrder"
        const val KEY_TAG_ORDER = "tagOrder"
        const val KEY_STATISTICS_RANGE = "statisticsRange"
        const val KEY_STATISTICS_RANGE_CUSTOM_START = "statisticsRangeCustomStart"
        const val KEY_STATISTICS_RANGE_CUSTOM_END = "statisticsRangeCustomEnd"
        const val KEY_STATISTICS_RANGE_LAST_DAYS = "statisticsRangeLastDays"
        const val KEY_STATISTICS_DETAIL_RANGE = "statisticsDetailRange"
        const val KEY_STATISTICS_DETAIL_RANGE_CUSTOM_START = "statisticsDetailRangeCustomStart"
        const val KEY_STATISTICS_DETAIL_RANGE_CUSTOM_END = "statisticsDetailRangeCustomEnd"
        const val KEY_STATISTICS_DETAIL_RANGE_LAST_DAYS = "statisticsDetailRangeLastDays"
        const val KEY_FILE_EXPORT_RANGE = "fileExportRange"
        const val KEY_FILE_EXPORT_RANGE_CUSTOM_START = "fileExportRangeCustomStart"
        const val KEY_FILE_EXPORT_RANGE_CUSTOM_END = "fileExportRangeCustomEnd"
        const val KEY_FILE_EXPORT_RANGE_LAST_DAYS = "fileExportRangeLastDays"
        const val KEY_CSV_EXPORT_CUSTOM_FILENAME = "csvExportCustomFilename"
        const val KEY_ICS_EXPORT_CUSTOM_FILENAME = "icsExportCustomFilename"
        const val KEY_KEEP_STATISTICS_RANGE = "keepStatisticsRange"
        const val KEY_FIRST_DAY_OF_WEEK = "firstDayOfWeek"
        const val KEY_START_OF_DAY_SHIFT = "startOfDayShift"
        const val KEY_SHOW_UNTRACKED_IN_RECORDS = "showUntrackedInRecords"
        const val KEY_SHOW_UNTRACKED_IN_STATISTICS = "showUntrackedInStatistics"
        const val KEY_SHOW_RECORDS_CALENDAR = "showRecordsCalendar"
        const val KEY_SHOW_CALENDAR_BUTTON_ON_RECORDS_TAB = "showCalendarButtonOnRecordsTab"
        const val KEY_REVERSE_ORDER_IN_CALENDAR = "reverseOrderInCalendar"
        const val KEY_DAYS_IN_CALENDAR = "daysInCalendar"
        const val KEY_SHOW_ACTIVITY_FILTERS = "showActivityFilters"
        const val KEY_ENABLE_REPEAT_BUTTON = "enableRepeatButton"
        const val KEY_ENABLE_POMODORO_MODE = "enablePomodoroMode"
        const val KEY_POMODORO_FOCUS_TIME = "pomodoroFocusTime"
        const val KEY_POMODORO_BREAK_TIME = "pomodoroBreakTime"
        const val KEY_POMODORO_LONG_BREAK_TIME = "pomodoroLongBreakTime"
        const val KEY_POMODORO_PERIODS_UNTIL_LONG_BREAK = "pomodoroPeriodsUntilLongBreak"
        const val KEY_ALLOW_MULTIPLE_ACTIVITY_FILTERS = "allowMultipleActivityFilters"
        const val KEY_SHOW_GOALS_SEPARATELY = "showGoalsSeparately"
        const val KEY_ALLOW_MULTITASKING = "allowMultitasking"
        const val KEY_SHOW_NOTIFICATIONS = "showNotifications"
        const val KEY_SHOW_NOTIFICATIONS_CONTROLS = "showNotificationsControls"
        const val KEY_INACTIVITY_REMINDER_DURATION = "inactivityReminderDuration"
        const val KEY_INACTIVITY_REMINDER_RECURRENT = "inactivityReminderRecurrent"
        const val KEY_INACTIVITY_REMINDER_DND_START = "inactivityReminderDndStart"
        const val KEY_INACTIVITY_REMINDER_DND_END = "inactivityReminderDndEnd"
        const val KEY_ACTIVITY_REMINDER_DURATION = "activityReminderDuration"
        const val KEY_ACTIVITY_REMINDER_RECURRENT = "activityReminderRecurrent"
        const val KEY_ACTIVITY_REMINDER_DND_START = "activityReminderDndStart"
        const val KEY_ACTIVITY_REMINDER_DND_END = "activityReminderDndEnd"
        const val KEY_IGNORE_SHORT_RECORDS_DURATION = "ignoreShortRecordsDuration"
        const val KEY_IGNORE_SHORT_UNTRACKED_DURATION = "ignoreShortUntrackedDuration"
        const val KEY_UNTRACKED_RANGE_ENABLED = "untrackedRangeEnabled"
        const val KEY_UNTRACKED_RANGE_START = "untrackedRangeStart"
        const val KEY_UNTRACKED_RANGE_END = "untrackedRangeEnd"
        const val KEY_DARK_MODE_2 = "darkMode2"
        const val KEY_NUMBER_OF_CARDS = "numberOfCards" // 0 - default width
        const val KEY_USE_MILITARY_TIME_FORMAT = "useMilitaryTimeFormat"
        const val KEY_USE_MONTH_DAY_TIME_FORMAT = "useMonthDayTimeFormat"
        const val KEY_USE_PROPORTIONAL_MINUTES = "useProportionalMinutes"
        const val KEY_SHOW_SECONDS = "showSeconds"
        const val KEY_KEEP_SCREEN_ON = "keepScreenOn"
        const val KEY_SHOW_RECORD_TAG_SELECTION = "showRecordTagSelection"
        const val KEY_SHOW_RECORD_TAG_SELECTION_EXCLUDE_ACTIVITIES = "showRecordTagSelectionExcludeActivities"
        const val KEY_AUTOSTART_POMODORO_ACTIVITIES = "autostartPomodoroActivities"
        const val KEY_RECORD_TAG_SELECTION_CLOSE_AFTER_ONE = "recordTagSelectionCloseAfterOne"
        const val KEY_AUTOMATED_TRACKING_SEND_EVENTS = "automatedTrackingSendEvents"
        const val KEY_REPEAT_BUTTON_TYPE = "repeatButtonType"
        const val KEY_WIDGET_TRANSPARENCY_PERCENT = "widgetTransparencyPercent"
        const val KEY_DEFAULT_TYPES_HIDDEN = "defaultTypesHidden"
        const val KEY_IS_NAV_BAR_AT_THE_BOTTOM = "isNavBarAtTheBottom"
        const val KEY_CARD_ORDER_MANUAL = "cardOrderManual"
        const val KEY_CATEGORY_ORDER_MANUAL = "categoryOrderManual"
        const val KEY_TAG_ORDER_MANUAL = "tagOrderManual"

        private const val KEY_AUTOMATIC_BACKUP_URI = "automaticBackupUri"
        private const val KEY_AUTOMATIC_BACKUP_ERROR = "automaticBackupError"
        private const val KEY_AUTOMATIC_BACKUP_LAST_SAVE_TIME = "automaticBackupLastSaveTime"
        private const val KEY_AUTOMATIC_EXPORT_URI = "automaticExportUri"
        private const val KEY_AUTOMATIC_EXPORT_ERROR = "automaticExportError"
        private const val KEY_AUTOMATIC_EXPORT_LAST_SAVE_TIME = "automaticExportLastSaveTime"
        private const val KEY_POMODORO_MODE_STARTED_TIMESTAMP = "pomodoroModeStartedTimestamp"
        private const val KEY_WIDGET = "widget_"
        private const val KEY_STATISTICS_WIDGET_FILTERED_TYPES = "statistics_widget_filtered_types_"
        private const val KEY_STATISTICS_WIDGET_FILTERED_CATEGORIES = "statistics_widget_filtered_categories_"
        private const val KEY_STATISTICS_WIDGET_FILTERED_TAGS = "statistics_widget_filtered_tags_"
        private const val KEY_STATISTICS_WIDGET_FILTER_TYPE = "statistics_widget_filter_type_"
        private const val KEY_STATISTICS_WIDGET_RANGE = "statistics_widget_range_"
        private const val KEY_STATISTICS_WIDGET_RANGE_LAST_DAYS = "statistics_widget_range_last_days_"
        private const val KEY_QUICK_SETTINGS_WIDGET_TYPE = "quick_settings_widget_type_"

        // Removed
        private const val KEY_SORT_RECORD_TYPES_BY_COLOR = "sortRecordTypesByColor" // Boolean
        private const val KEY_DARK_MODE = "darkMode"
        private const val KEY_RECORD_TAG_SELECTION_EVEN_FOR_GENERAL_TAGS = "recordTagSelectionEvenForGeneralTags"
    }
}