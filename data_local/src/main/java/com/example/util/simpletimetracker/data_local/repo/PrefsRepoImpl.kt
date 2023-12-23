package com.example.util.simpletimetracker.data_local.repo

import android.content.SharedPreferences
import com.example.util.simpletimetracker.data_local.utils.delegate
import com.example.util.simpletimetracker.domain.extension.orZero
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

    override var recordTypesFilteredOnChart: Set<String> by prefs.delegate(
        KEY_RECORD_TYPES_FILTERED_ON_CHART, emptySet()
    )

    override var categoriesFilteredOnChart: Set<String> by prefs.delegate(
        KEY_CATEGORIES_TYPES_FILTERED_ON_CHART, emptySet()
    )

    override var tagsFilteredOnChart: Set<String> by prefs.delegate(
        KEY_TAGS_FILTERED_ON_CHART, emptySet()
    )

    override var chartFilterType: Int by prefs.delegate(
        KEY_CHART_FILTER_TYPE, 0
    )

    override var cardOrder: Int by prefs.delegate(
        KEY_CARD_ORDER, 0
    )

    override var statisticsRange: Int by prefs.delegate(
        KEY_STATISTICS_RANGE, 0
    )

    override var statisticsRangeCustomStart: Long by prefs.delegate(
        KEY_STATISTICS_RANGE_CUSTOM_START, 0
    )

    override var statisticsRangeCustomEnd: Long by prefs.delegate(
        KEY_STATISTICS_RANGE_CUSTOM_END, 0
    )

    override var statisticsDetailRange: Int by prefs.delegate(
        KEY_STATISTICS_DETAIL_RANGE, 0
    )

    override var statisticsDetailRangeCustomStart: Long by prefs.delegate(
        KEY_STATISTICS_DETAIL_RANGE_CUSTOM_START, 0
    )

    override var statisticsDetailRangeCustomEnd: Long by prefs.delegate(
        KEY_STATISTICS_DETAIL_RANGE_CUSTOM_END, 0
    )

    override var keepStatisticsRange: Boolean by prefs.delegate(
        KEY_KEEP_STATISTICS_RANGE, false
    )

    override var firstDayOfWeek: Int by prefs.delegate(
        KEY_FIRST_DAY_OF_WEEK, firstDayOfWeekDefault
    )

    override var startOfDayShift: Long by prefs.delegate(
        KEY_START_OF_DAY_SHIFT, 0
    )

    override var showUntrackedInRecords: Boolean by prefs.delegate(
        KEY_SHOW_UNTRACKED_IN_RECORDS, false
    )

    override var showUntrackedInStatistics: Boolean by prefs.delegate(
        KEY_SHOW_UNTRACKED_IN_STATISTICS, false
    )

    override var showRecordsCalendar: Boolean by prefs.delegate(
        KEY_SHOW_RECORDS_CALENDAR, false
    )

    override var reverseOrderInCalendar: Boolean by prefs.delegate(
        KEY_REVERSE_ORDER_IN_CALENDAR, false
    )

    override var daysInCalendar: Int by prefs.delegate(
        KEY_DAYS_IN_CALENDAR, 0
    )

    override var showActivityFilters: Boolean by prefs.delegate(
        KEY_SHOW_ACTIVITY_FILTERS, false
    )

    override var showGoalsSeparately: Boolean by prefs.delegate(
        KEY_SHOW_GOALS_SEPARATELY, false
    )

    override var allowMultitasking: Boolean by prefs.delegate(
        KEY_ALLOW_MULTITASKING, true
    )

    override var showNotifications: Boolean by prefs.delegate(
        KEY_SHOW_NOTIFICATIONS, false
    )

    override var showNotificationsControls: Boolean by prefs.delegate(
        KEY_SHOW_NOTIFICATIONS_CONTROLS, true
    )

    override var inactivityReminderDuration: Long by prefs.delegate(
        KEY_INACTIVITY_REMINDER_DURATION, 0 // 0 is for disabled
    )

    override var inactivityReminderRecurrent: Boolean by prefs.delegate(
        KEY_INACTIVITY_REMINDER_RECURRENT, false
    )

    override var inactivityReminderDoNotDisturbStart: Long by prefs.delegate(
        KEY_INACTIVITY_REMINDER_DND_START, DO_NOT_DISTURB_PERIOD_START
    )

    override var inactivityReminderDoNotDisturbEnd: Long by prefs.delegate(
        KEY_INACTIVITY_REMINDER_DND_END, DO_NOT_DISTURB_PERIOD_END
    )

    override var activityReminderDuration: Long by prefs.delegate(
        KEY_ACTIVITY_REMINDER_DURATION, 0 // 0 is for disabled
    )

    override var activityReminderRecurrent: Boolean by prefs.delegate(
        KEY_ACTIVITY_REMINDER_RECURRENT, false
    )

    override var activityReminderDoNotDisturbStart: Long by prefs.delegate(
        KEY_ACTIVITY_REMINDER_DND_START, DO_NOT_DISTURB_PERIOD_START
    )

    override var activityReminderDoNotDisturbEnd: Long by prefs.delegate(
        KEY_ACTIVITY_REMINDER_DND_END, DO_NOT_DISTURB_PERIOD_END
    )

    override var ignoreShortRecordsDuration: Long by prefs.delegate(
        KEY_IGNORE_SHORT_RECORDS_DURATION, 0 // 0 is for disabled
    )

    override var ignoreShortUntrackedDuration: Long by prefs.delegate(
        KEY_IGNORE_SHORT_UNTRACKED_DURATION, 60 // 0 is for disabled
    )

    override var untrackedRangeEnabled: Boolean by prefs.delegate(
        KEY_UNTRACKED_RANGE_ENABLED, false
    )

    override var untrackedRangeStart: Long by prefs.delegate(
        KEY_UNTRACKED_RANGE_START, 0
    )

    override var untrackedRangeEnd: Long by prefs.delegate(
        KEY_UNTRACKED_RANGE_END, 0
    )

    override var darkMode: Int by prefs.delegate(
        KEY_DARK_MODE_2, 0
    )

    override var numberOfCards: Int by prefs.delegate(
        KEY_NUMBER_OF_CARDS, 0
    )

    override var useMilitaryTimeFormat: Boolean by prefs.delegate(
        KEY_USE_MILITARY_TIME_FORMAT, true
    )

    override var useMonthDayTimeFormat: Boolean by prefs.delegate(
        KEY_USE_MONTH_DAY_TIME_FORMAT, false
    )

    override var useProportionalMinutes: Boolean by prefs.delegate(
        KEY_USE_PROPORTIONAL_MINUTES, false
    )

    override var showSeconds: Boolean by prefs.delegate(
        KEY_SHOW_SECONDS, false
    )

    override var keepScreenOn: Boolean by prefs.delegate(
        KEY_KEEP_SCREEN_ON, false
    )

    override var showRecordTagSelection: Boolean by prefs.delegate(
        KEY_SHOW_RECORD_TAG_SELECTION, false
    )

    override var recordTagSelectionCloseAfterOne: Boolean by prefs.delegate(
        KEY_RECORD_TAG_SELECTION_CLOSE_AFTER_ONE, false
    )

    override var recordTagSelectionEvenForGeneralTags: Boolean by prefs.delegate(
        KEY_RECORD_TAG_SELECTION_EVEN_FOR_GENERAL_TAGS, false
    )

    override var automatedTrackingSendEvents: Boolean by prefs.delegate(
        KEY_AUTOMATED_TRACKING_SEND_EVENTS, false
    )

    override var automaticBackupUri: String by prefs.delegate(
        KEY_AUTOMATIC_BACKUP_URI, ""
    )

    override var automaticBackupError: Boolean by prefs.delegate(
        KEY_AUTOMATIC_BACKUP_ERROR, false
    )

    override var automaticBackupLastSaveTime: Long by prefs.delegate(
        KEY_AUTOMATIC_BACKUP_LAST_SAVE_TIME, 0
    )

    override var automaticExportUri: String by prefs.delegate(
        KEY_AUTOMATIC_EXPORT_URI, ""
    )

    override var automaticExportError: Boolean by prefs.delegate(
        KEY_AUTOMATIC_EXPORT_ERROR, false
    )

    override var automaticExportLastSaveTime: Long by prefs.delegate(
        KEY_AUTOMATIC_EXPORT_LAST_SAVE_TIME, 0
    )

    override var repeatButtonType: Int by prefs.delegate(
        KEY_REPEAT_BUTTON_TYPE, 0
    )

    override fun setWidget(widgetId: Int, recordType: Long) {
        prefs.edit().putLong(KEY_WIDGET + widgetId, recordType).apply()
    }

    override fun getWidget(widgetId: Int): Long {
        return prefs.getLong(KEY_WIDGET + widgetId, 0)
    }

    override fun removeWidget(widgetId: Int) {
        prefs.edit().remove(KEY_WIDGET + widgetId).apply()
    }

    override fun setStatisticsWidget(widgetId: Int, data: StatisticsWidgetData) {
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
        val filteredTypesData = data.filteredTypes.map(Long::toString).toSet()
        val filteredCategoriesData = data.filteredCategories.map(Long::toString).toSet()
        val filteredTagsData = data.filteredTags.map(Long::toString).toSet()

        prefs.edit()
            .putInt(KEY_STATISTICS_WIDGET_FILTER_TYPE + widgetId, filterTypeData)
            .putInt(KEY_STATISTICS_WIDGET_RANGE + widgetId, rangeData)
            .putStringSet(KEY_STATISTICS_WIDGET_FILTERED_TYPES + widgetId, filteredTypesData)
            .putStringSet(KEY_STATISTICS_WIDGET_FILTERED_CATEGORIES + widgetId, filteredCategoriesData)
            .putStringSet(KEY_STATISTICS_WIDGET_FILTERED_TAGS + widgetId, filteredTagsData)
            .apply()
    }

    override fun getStatisticsWidget(widgetId: Int): StatisticsWidgetData {
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
            5 -> RangeLength.Last
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

    override fun removeStatisticsWidget(widgetId: Int) {
        prefs.edit()
            .remove(KEY_STATISTICS_WIDGET_FILTER_TYPE + widgetId)
            .remove(KEY_STATISTICS_WIDGET_RANGE + widgetId)
            .remove(KEY_STATISTICS_WIDGET_FILTERED_TYPES + widgetId)
            .remove(KEY_STATISTICS_WIDGET_FILTERED_CATEGORIES + widgetId)
            .remove(KEY_STATISTICS_WIDGET_FILTERED_TAGS + widgetId)
            .apply()
    }

    override fun setQuickSettingsWidget(widgetId: Int, data: QuickSettingsWidgetType) {
        val type = when (data) {
            is QuickSettingsWidgetType.AllowMultitasking -> 0L
            is QuickSettingsWidgetType.ShowRecordTagSelection -> 1L
        }
        prefs.edit().putLong(KEY_QUICK_SETTINGS_WIDGET_TYPE + widgetId, type).apply()
    }

    override fun getQuickSettingsWidget(widgetId: Int): QuickSettingsWidgetType {
        return when (prefs.getLong(KEY_QUICK_SETTINGS_WIDGET_TYPE + widgetId, 0)) {
            0L -> QuickSettingsWidgetType.AllowMultitasking
            1L -> QuickSettingsWidgetType.ShowRecordTagSelection
            else -> QuickSettingsWidgetType.AllowMultitasking
        }
    }

    override fun removeQuickSettingsWidget(widgetId: Int) {
        prefs.edit().remove(KEY_QUICK_SETTINGS_WIDGET_TYPE + widgetId).apply()
    }

    override fun setCardOrderManual(cardOrder: Map<Long, Long>) {
        val set = cardOrder.map { (typeId, order) ->
            "$typeId$CARDS_ORDER_DELIMITER${order.toShort()}"
        }.toSet()

        prefs.edit().putStringSet(KEY_CARD_ORDER_MANUAL, set).apply()
    }

    override fun getCardOrderManual(): Map<Long, Long> {
        val set = prefs.getStringSet(KEY_CARD_ORDER_MANUAL, emptySet())

        return set
            ?.map { string ->
                string.split(CARDS_ORDER_DELIMITER).let { parts ->
                    parts.getOrNull(0).orEmpty() to parts.getOrNull(1).orEmpty()
                }
            }
            ?.toMap()
            ?.mapKeys { it.key.toLongOrNull().orZero() }
            ?.mapValues { it.value.toLongOrNull().orZero() }
            ?: emptyMap()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    @Suppress("unused")
    companion object {
        private const val DO_NOT_DISTURB_PERIOD_START: Long = 0 // midnight
        private const val DO_NOT_DISTURB_PERIOD_END: Long = 1000 * 60 * 60 * 8 // 8 hours in the morning
        private const val CARDS_ORDER_DELIMITER = "_"

        private const val KEY_RECORD_TYPES_FILTERED_ON_CHART = "recordTypesFilteredOnChart"
        private const val KEY_CATEGORIES_TYPES_FILTERED_ON_CHART = "categoriesFilteredOnChart"
        private const val KEY_TAGS_FILTERED_ON_CHART = "tagsFilteredOnChart"
        private const val KEY_CHART_FILTER_TYPE = "chartFilterType"
        private const val KEY_CARD_ORDER = "cardOrder"
        private const val KEY_STATISTICS_RANGE = "statisticsRange"
        private const val KEY_STATISTICS_RANGE_CUSTOM_START = "statisticsRangeCustomStart"
        private const val KEY_STATISTICS_RANGE_CUSTOM_END = "statisticsRangeCustomEnd"
        private const val KEY_STATISTICS_DETAIL_RANGE = "statisticsDetailRange"
        private const val KEY_STATISTICS_DETAIL_RANGE_CUSTOM_START = "statisticsDetailRangeCustomStart"
        private const val KEY_STATISTICS_DETAIL_RANGE_CUSTOM_END = "statisticsDetailRangeCustomEnd"
        private const val KEY_KEEP_STATISTICS_RANGE = "keepStatisticsRange"
        private const val KEY_FIRST_DAY_OF_WEEK = "firstDayOfWeek"
        private const val KEY_START_OF_DAY_SHIFT = "startOfDayShift"
        private const val KEY_SHOW_UNTRACKED_IN_RECORDS = "showUntrackedInRecords"
        private const val KEY_SHOW_UNTRACKED_IN_STATISTICS = "showUntrackedInStatistics"
        private const val KEY_SHOW_RECORDS_CALENDAR = "showRecordsCalendar"
        private const val KEY_REVERSE_ORDER_IN_CALENDAR = "reverseOrderInCalendar"
        private const val KEY_DAYS_IN_CALENDAR = "daysInCalendar"
        private const val KEY_SHOW_ACTIVITY_FILTERS = "showActivityFilters"
        private const val KEY_SHOW_GOALS_SEPARATELY = "showGoalsSeparately"
        private const val KEY_ALLOW_MULTITASKING = "allowMultitasking"
        private const val KEY_SHOW_NOTIFICATIONS = "showNotifications"
        private const val KEY_SHOW_NOTIFICATIONS_CONTROLS = "showNotificationsControls"
        private const val KEY_INACTIVITY_REMINDER_DURATION = "inactivityReminderDuration"
        private const val KEY_INACTIVITY_REMINDER_RECURRENT = "inactivityReminderRecurrent"
        private const val KEY_INACTIVITY_REMINDER_DND_START = "inactivityReminderDndStart"
        private const val KEY_INACTIVITY_REMINDER_DND_END = "inactivityReminderDndEnd"
        private const val KEY_ACTIVITY_REMINDER_DURATION = "activityReminderDuration"
        private const val KEY_ACTIVITY_REMINDER_RECURRENT = "activityReminderRecurrent"
        private const val KEY_ACTIVITY_REMINDER_DND_START = "activityReminderDndStart"
        private const val KEY_ACTIVITY_REMINDER_DND_END = "activityReminderDndEnd"
        private const val KEY_IGNORE_SHORT_RECORDS_DURATION = "ignoreShortRecordsDuration"
        private const val KEY_IGNORE_SHORT_UNTRACKED_DURATION = "ignoreShortUntrackedDuration"
        private const val KEY_UNTRACKED_RANGE_ENABLED = "untrackedRangeEnabled"
        private const val KEY_UNTRACKED_RANGE_START = "untrackedRangeStart"
        private const val KEY_UNTRACKED_RANGE_END = "untrackedRangeEnd"
        private const val KEY_DARK_MODE_2 = "darkMode2"
        private const val KEY_NUMBER_OF_CARDS = "numberOfCards" // 0 - default width
        private const val KEY_USE_MILITARY_TIME_FORMAT = "useMilitaryTimeFormat"
        private const val KEY_USE_MONTH_DAY_TIME_FORMAT = "useMonthDayTimeFormat"
        private const val KEY_USE_PROPORTIONAL_MINUTES = "useProportionalMinutes"
        private const val KEY_SHOW_SECONDS = "showSeconds"
        private const val KEY_KEEP_SCREEN_ON = "keepScreenOn"
        private const val KEY_SHOW_RECORD_TAG_SELECTION = "showRecordTagSelection"
        private const val KEY_RECORD_TAG_SELECTION_CLOSE_AFTER_ONE = "recordTagSelectionCloseAfterOne"
        private const val KEY_RECORD_TAG_SELECTION_EVEN_FOR_GENERAL_TAGS = "recordTagSelectionEvenForGeneralTags"
        private const val KEY_AUTOMATED_TRACKING_SEND_EVENTS = "automatedTrackingSendEvents"
        private const val KEY_AUTOMATIC_BACKUP_URI = "automaticBackupUri"
        private const val KEY_AUTOMATIC_BACKUP_ERROR = "automaticBackupError"
        private const val KEY_AUTOMATIC_BACKUP_LAST_SAVE_TIME = "automaticBackupLastSaveTime"
        private const val KEY_AUTOMATIC_EXPORT_URI = "automaticExportUri"
        private const val KEY_AUTOMATIC_EXPORT_ERROR = "automaticExportError"
        private const val KEY_AUTOMATIC_EXPORT_LAST_SAVE_TIME = "automaticExportLastSaveTime"
        private const val KEY_REPEAT_BUTTON_TYPE = "repeatButtonType"
        private const val KEY_WIDGET = "widget_"
        private const val KEY_STATISTICS_WIDGET_FILTERED_TYPES = "statistics_widget_filtered_types_"
        private const val KEY_STATISTICS_WIDGET_FILTERED_CATEGORIES = "statistics_widget_filtered_categories_"
        private const val KEY_STATISTICS_WIDGET_FILTERED_TAGS = "statistics_widget_filtered_tags_"
        private const val KEY_STATISTICS_WIDGET_FILTER_TYPE = "statistics_widget_filter_type_"
        private const val KEY_STATISTICS_WIDGET_RANGE = "statistics_widget_range_"
        private const val KEY_QUICK_SETTINGS_WIDGET_TYPE = "quick_settings_widget_type_"
        private const val KEY_CARD_ORDER_MANUAL = "cardOrderManual"

        // Removed
        private const val KEY_SORT_RECORD_TYPES_BY_COLOR = "sortRecordTypesByColor" // Boolean
        private const val KEY_DARK_MODE = "darkMode"
    }
}