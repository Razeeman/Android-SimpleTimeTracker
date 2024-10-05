package com.example.util.simpletimetracker.data_local.resolver

import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_ACTIVITY_REMINDER_DND_END
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_ACTIVITY_REMINDER_DND_START
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_ACTIVITY_REMINDER_DURATION
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_ACTIVITY_REMINDER_RECURRENT
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_ALLOW_MULTIPLE_ACTIVITY_FILTERS
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_ALLOW_MULTITASKING
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_AUTOMATED_TRACKING_SEND_EVENTS
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_AUTOSTART_POMODORO_ACTIVITIES
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_CARD_ORDER
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_CARD_ORDER_MANUAL
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_CATEGORIES_TYPES_FILTERED_ON_CHART
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_CATEGORY_ORDER
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_CATEGORY_ORDER_MANUAL
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_CHART_FILTER_TYPE
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_DARK_MODE_2
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_DAYS_IN_CALENDAR
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_DEFAULT_TYPES_HIDDEN
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_ENABLE_POMODORO_MODE
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_ENABLE_REPEAT_BUTTON
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_FILE_EXPORT_RANGE
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_FILE_EXPORT_RANGE_CUSTOM_END
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_FILE_EXPORT_RANGE_CUSTOM_START
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_FILE_EXPORT_RANGE_LAST_DAYS
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_FIRST_DAY_OF_WEEK
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_IGNORE_SHORT_RECORDS_DURATION
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_IGNORE_SHORT_UNTRACKED_DURATION
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_INACTIVITY_REMINDER_DND_END
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_INACTIVITY_REMINDER_DND_START
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_INACTIVITY_REMINDER_DURATION
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_INACTIVITY_REMINDER_RECURRENT
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_IS_NAV_BAR_AT_THE_BOTTOM
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_KEEP_SCREEN_ON
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_KEEP_STATISTICS_RANGE
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_NUMBER_OF_CARDS
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_POMODORO_BREAK_TIME
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_POMODORO_FOCUS_TIME
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_POMODORO_LONG_BREAK_TIME
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_POMODORO_PERIODS_UNTIL_LONG_BREAK
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_RECORD_TAG_SELECTION_CLOSE_AFTER_ONE
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_RECORD_TYPES_FILTERED_ON_CHART
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_RECORD_TYPES_FILTERED_ON_LIST
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_REPEAT_BUTTON_TYPE
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_REVERSE_ORDER_IN_CALENDAR
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_SHOW_ACTIVITY_FILTERS
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_SHOW_CALENDAR_BUTTON_ON_RECORDS_TAB
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_SHOW_GOALS_SEPARATELY
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_SHOW_NOTIFICATIONS
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_SHOW_NOTIFICATIONS_CONTROLS
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_SHOW_RECORDS_CALENDAR
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_SHOW_RECORD_TAG_SELECTION
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_SHOW_RECORD_TAG_SELECTION_EXCLUDE_ACTIVITIES
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_SHOW_SECONDS
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_SHOW_UNTRACKED_IN_RECORDS
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_SHOW_UNTRACKED_IN_STATISTICS
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_START_OF_DAY_SHIFT
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_STATISTICS_DETAIL_RANGE
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_STATISTICS_DETAIL_RANGE_CUSTOM_END
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_STATISTICS_DETAIL_RANGE_CUSTOM_START
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_STATISTICS_DETAIL_RANGE_LAST_DAYS
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_STATISTICS_RANGE
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_STATISTICS_RANGE_CUSTOM_END
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_STATISTICS_RANGE_CUSTOM_START
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_STATISTICS_RANGE_LAST_DAYS
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_TAGS_FILTERED_ON_CHART
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_TAG_ORDER
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_TAG_ORDER_MANUAL
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_UNTRACKED_RANGE_ENABLED
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_UNTRACKED_RANGE_END
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_UNTRACKED_RANGE_START
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_USE_MILITARY_TIME_FORMAT
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_USE_MONTH_DAY_TIME_FORMAT
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_USE_PROPORTIONAL_MINUTES
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl.Companion.KEY_WIDGET_TRANSPARENCY_PERCENT
import javax.inject.Inject
import kotlin.reflect.KMutableProperty0

class BackupPrefsRepo @Inject constructor(
    private val prefsRepoImpl: PrefsRepoImpl,
) {

    fun saveToBackupString(): String = runCatching {
        val result = StringBuilder()

        getProcessorsList().filter {
            prefsRepoImpl.hasValueSaved(it.key)
        }.forEach { processor ->
            val valueString: String = when (val value = processor.getter()) {
                is Boolean -> if (value) "1" else "0"
                is Int -> value.toString()
                is Long -> value.toString()
                is String -> value
                is Set<*> -> value.joinToString(separator = ",")
                else -> null
            } ?: return@forEach

            result
                .append(PREFS_KEY).append("\t")
                .append(processor.key).append("\t")
                .append(valueString).append("\n")
        }

        return result.toString()
    }.getOrNull().orEmpty()

    fun restoreFromBackupString(parts: List<String>) = runCatching {
        val processors = getProcessorsList().associateBy { it.key }
        val processor = parts.getOrNull(1)?.let(processors::get) ?: return@runCatching
        val valueString: String = parts.getOrNull(2) ?: return@runCatching
        val currentValue: Any = processor.getter()

        val value: Any = when (currentValue) {
            is Boolean -> valueString.toIntOrNull() == 1
            is Int -> valueString.toIntOrNull()
            is Long -> valueString.toLongOrNull()
            is String -> valueString
            is Set<*> -> valueString.split(',').toSet()
            else -> null
        } ?: return@runCatching

        processor.setter(value)
    }

    private fun getProcessorsList(): List<PrefsProcessor<*>> = with(prefsRepoImpl) {
        return listOf(
            PrefsProcessor(KEY_RECORD_TYPES_FILTERED_ON_LIST, ::recordTypesFilteredOnList),
            PrefsProcessor(KEY_RECORD_TYPES_FILTERED_ON_CHART, ::recordTypesFilteredOnChart),
            PrefsProcessor(KEY_CATEGORIES_TYPES_FILTERED_ON_CHART, ::categoriesFilteredOnChart),
            PrefsProcessor(KEY_TAGS_FILTERED_ON_CHART, ::tagsFilteredOnChart),
            PrefsProcessor(KEY_CHART_FILTER_TYPE, ::chartFilterType),
            PrefsProcessor(KEY_CARD_ORDER, ::cardOrder),
            PrefsProcessor(KEY_CATEGORY_ORDER, ::categoryOrder),
            PrefsProcessor(KEY_TAG_ORDER, ::tagOrder),
            PrefsProcessor(KEY_CARD_ORDER_MANUAL, ::cardOrderManual),
            PrefsProcessor(KEY_CATEGORY_ORDER_MANUAL, ::categoryOrderManual),
            PrefsProcessor(KEY_TAG_ORDER_MANUAL, ::tagOrderManual),
            PrefsProcessor(KEY_STATISTICS_RANGE, ::statisticsRange),
            PrefsProcessor(KEY_STATISTICS_RANGE_CUSTOM_START, ::statisticsRangeCustomStart),
            PrefsProcessor(KEY_STATISTICS_RANGE_CUSTOM_END, ::statisticsRangeCustomEnd),
            PrefsProcessor(KEY_STATISTICS_RANGE_LAST_DAYS, ::statisticsRangeLastDays),
            PrefsProcessor(KEY_STATISTICS_DETAIL_RANGE, ::statisticsDetailRange),
            PrefsProcessor(KEY_STATISTICS_DETAIL_RANGE_CUSTOM_START, ::statisticsDetailRangeCustomStart),
            PrefsProcessor(KEY_STATISTICS_DETAIL_RANGE_CUSTOM_END, ::statisticsDetailRangeCustomEnd),
            PrefsProcessor(KEY_STATISTICS_DETAIL_RANGE_LAST_DAYS, ::statisticsDetailRangeLastDays),
            PrefsProcessor(KEY_FILE_EXPORT_RANGE, ::fileExportRange),
            PrefsProcessor(KEY_FILE_EXPORT_RANGE_CUSTOM_START, ::fileExportRangeCustomStart),
            PrefsProcessor(KEY_FILE_EXPORT_RANGE_CUSTOM_END, ::fileExportRangeCustomEnd),
            PrefsProcessor(KEY_FILE_EXPORT_RANGE_LAST_DAYS, ::fileExportRangeLastDays),
            PrefsProcessor(KEY_KEEP_STATISTICS_RANGE, ::keepStatisticsRange),
            PrefsProcessor(KEY_FIRST_DAY_OF_WEEK, ::firstDayOfWeek),
            PrefsProcessor(KEY_START_OF_DAY_SHIFT, ::startOfDayShift),
            PrefsProcessor(KEY_SHOW_UNTRACKED_IN_RECORDS, ::showUntrackedInRecords),
            PrefsProcessor(KEY_SHOW_UNTRACKED_IN_STATISTICS, ::showUntrackedInStatistics),
            PrefsProcessor(KEY_SHOW_RECORDS_CALENDAR, ::showRecordsCalendar),
            PrefsProcessor(KEY_SHOW_CALENDAR_BUTTON_ON_RECORDS_TAB, ::showCalendarButtonOnRecordsTab),
            PrefsProcessor(KEY_REVERSE_ORDER_IN_CALENDAR, ::reverseOrderInCalendar),
            PrefsProcessor(KEY_DAYS_IN_CALENDAR, ::daysInCalendar),
            PrefsProcessor(KEY_SHOW_ACTIVITY_FILTERS, ::showActivityFilters),
            PrefsProcessor(KEY_ENABLE_REPEAT_BUTTON, ::enableRepeatButton),
            PrefsProcessor(KEY_ENABLE_POMODORO_MODE, ::enablePomodoroMode),
            PrefsProcessor(KEY_POMODORO_FOCUS_TIME, ::pomodoroFocusTime),
            PrefsProcessor(KEY_POMODORO_BREAK_TIME, ::pomodoroBreakTime),
            PrefsProcessor(KEY_POMODORO_LONG_BREAK_TIME, ::pomodoroLongBreakTime),
            PrefsProcessor(KEY_POMODORO_PERIODS_UNTIL_LONG_BREAK, ::pomodoroPeriodsUntilLongBreak),
            PrefsProcessor(KEY_ALLOW_MULTIPLE_ACTIVITY_FILTERS, ::allowMultipleActivityFilters),
            PrefsProcessor(KEY_SHOW_GOALS_SEPARATELY, ::showGoalsSeparately),
            PrefsProcessor(KEY_ALLOW_MULTITASKING, ::allowMultitasking),
            PrefsProcessor(KEY_SHOW_NOTIFICATIONS, ::showNotifications),
            PrefsProcessor(KEY_SHOW_NOTIFICATIONS_CONTROLS, ::showNotificationsControls),
            PrefsProcessor(KEY_INACTIVITY_REMINDER_DURATION, ::inactivityReminderDuration),
            PrefsProcessor(KEY_INACTIVITY_REMINDER_RECURRENT, ::inactivityReminderRecurrent),
            PrefsProcessor(KEY_INACTIVITY_REMINDER_DND_START, ::inactivityReminderDoNotDisturbStart),
            PrefsProcessor(KEY_INACTIVITY_REMINDER_DND_END, ::inactivityReminderDoNotDisturbEnd),
            PrefsProcessor(KEY_ACTIVITY_REMINDER_DURATION, ::activityReminderDuration),
            PrefsProcessor(KEY_ACTIVITY_REMINDER_RECURRENT, ::activityReminderRecurrent),
            PrefsProcessor(KEY_ACTIVITY_REMINDER_DND_START, ::activityReminderDoNotDisturbStart),
            PrefsProcessor(KEY_ACTIVITY_REMINDER_DND_END, ::activityReminderDoNotDisturbEnd),
            PrefsProcessor(KEY_IGNORE_SHORT_RECORDS_DURATION, ::ignoreShortRecordsDuration),
            PrefsProcessor(KEY_IGNORE_SHORT_UNTRACKED_DURATION, ::ignoreShortUntrackedDuration),
            PrefsProcessor(KEY_UNTRACKED_RANGE_ENABLED, ::untrackedRangeEnabled),
            PrefsProcessor(KEY_UNTRACKED_RANGE_START, ::untrackedRangeStart),
            PrefsProcessor(KEY_UNTRACKED_RANGE_END, ::untrackedRangeEnd),
            PrefsProcessor(KEY_DARK_MODE_2, ::darkMode),
            PrefsProcessor(KEY_NUMBER_OF_CARDS, ::numberOfCards),
            PrefsProcessor(KEY_USE_MILITARY_TIME_FORMAT, ::useMilitaryTimeFormat),
            PrefsProcessor(KEY_USE_MONTH_DAY_TIME_FORMAT, ::useMonthDayTimeFormat),
            PrefsProcessor(KEY_USE_PROPORTIONAL_MINUTES, ::useProportionalMinutes),
            PrefsProcessor(KEY_SHOW_SECONDS, ::showSeconds),
            PrefsProcessor(KEY_KEEP_SCREEN_ON, ::keepScreenOn),
            PrefsProcessor(KEY_SHOW_RECORD_TAG_SELECTION, ::showRecordTagSelection),
            PrefsProcessor(KEY_RECORD_TAG_SELECTION_CLOSE_AFTER_ONE, ::recordTagSelectionCloseAfterOne),
            PrefsProcessor(KEY_SHOW_RECORD_TAG_SELECTION_EXCLUDE_ACTIVITIES, ::recordTagSelectionExcludeActivities),
            PrefsProcessor(KEY_AUTOSTART_POMODORO_ACTIVITIES, ::autostartPomodoroActivities),
            PrefsProcessor(KEY_AUTOMATED_TRACKING_SEND_EVENTS, ::automatedTrackingSendEvents),
            PrefsProcessor(KEY_REPEAT_BUTTON_TYPE, ::repeatButtonType),
            PrefsProcessor(KEY_WIDGET_TRANSPARENCY_PERCENT, ::widgetBackgroundTransparencyPercent),
            PrefsProcessor(KEY_DEFAULT_TYPES_HIDDEN, ::defaultTypesHidden),
            PrefsProcessor(KEY_IS_NAV_BAR_AT_THE_BOTTOM, ::isNavBarAtTheBottom),
        )
    }

    private data class PrefsProcessor<T : Any>(
        val key: String,
        private val property: KMutableProperty0<T>,
    ) {

        fun getter(): T {
            return property.get()
        }

        @Suppress("UNCHECKED_CAST")
        fun setter(value: Any) {
            (value as? T)?.let { property.set(it) }
        }
    }

    companion object {
        const val PREFS_KEY = "prefs"
    }
}