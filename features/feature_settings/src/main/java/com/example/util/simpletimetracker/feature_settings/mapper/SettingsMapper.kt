package com.example.util.simpletimetracker.feature_settings.mapper

import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.extension.shiftTimeStamp
import com.example.util.simpletimetracker.core.interactor.LanguageInteractor
import com.example.util.simpletimetracker.core.mapper.FileExportDateTimeFormatMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.fileExport.ExportDateTimeFormat
import com.example.util.simpletimetracker.domain.darkMode.model.DarkMode
import com.example.util.simpletimetracker.domain.daysOfWeek.mapper.DaysInCalendarMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DaysInCalendar
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.model.RepeatButtonType
import com.example.util.simpletimetracker.domain.recordTag.model.CardTagOrder
import com.example.util.simpletimetracker.domain.recordType.model.CardOrder
import com.example.util.simpletimetracker.domain.widget.model.WidgetTransparencyPercent
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.model.CustomizeOptionsMenuListItem
import com.example.util.simpletimetracker.feature_settings.viewData.CardOrderViewData
import com.example.util.simpletimetracker.feature_settings.viewData.DarkModeViewData
import com.example.util.simpletimetracker.feature_settings.viewData.DaysInCalendarViewData
import com.example.util.simpletimetracker.feature_settings.viewData.DurationFormatViewData
import com.example.util.simpletimetracker.feature_settings.viewData.ExportDateTimeFormatViewData
import com.example.util.simpletimetracker.feature_settings.viewData.FirstDayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.viewData.LanguageViewData
import com.example.util.simpletimetracker.feature_settings.viewData.RepeatButtonViewData
import com.example.util.simpletimetracker.feature_settings.viewData.WidgetTransparencyViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsDurationViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.absoluteValue

class SettingsMapper @Inject constructor(
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val languageInteractor: LanguageInteractor,
    private val daysInCalendarMapper: DaysInCalendarMapper,
    private val fileExportDateTimeFormatMapper: FileExportDateTimeFormatMapper,
) {

    private val cardOrderList: List<CardOrder> = listOf(
        CardOrder.NAME,
        CardOrder.COLOR,
        CardOrder.MANUAL,
    )

    private val cardTagOrderList: List<CardTagOrder> = listOf(
        CardTagOrder.NAME,
        CardTagOrder.COLOR,
        CardTagOrder.ACTIVITY,
        CardTagOrder.MANUAL,
    )

    private val durationFormatList: List<DurationFormat> = listOf(
        DurationFormat.MINUTES,
        DurationFormat.HOURS,
        DurationFormat.DAYS,
        DurationFormat.PROPORTIONAL_MINUTES,
    )

    private val daysInCalendarList: List<DaysInCalendar> = listOf(
        DaysInCalendar.ONE,
        DaysInCalendar.THREE,
        DaysInCalendar.FIVE,
        DaysInCalendar.SEVEN,
        DaysInCalendar.WEEK,
    )

    private val widgetTransparencyList: List<WidgetTransparencyPercent> = listOf(
        WidgetTransparencyPercent(100),
        WidgetTransparencyPercent(80),
        WidgetTransparencyPercent(60),
        WidgetTransparencyPercent(40),
        WidgetTransparencyPercent(20),
        WidgetTransparencyPercent(0),
    )

    private val dayOfWeekList: List<DayOfWeek> = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY,
    )

    private val repeatButtonList: List<RepeatButtonType> = listOf(
        RepeatButtonType.RepeatLast,
        RepeatButtonType.RepeatBeforeLast,
    )

    private val darkModeList: List<DarkMode> = listOf(
        DarkMode.System,
        DarkMode.Enabled,
        DarkMode.Disabled,
    )

    private val exportDateTimeFormatList: List<ExportDateTimeFormat> =
        fileExportDateTimeFormatMapper.getAvailableFormats()

    fun toCardOrderViewData(currentOrder: CardOrder): CardOrderViewData {
        return CardOrderViewData(
            items = cardOrderList
                .map(::toCardOrderName)
                .map(CustomSpinner::CustomSpinnerTextItem),
            selectedPosition = toPosition(currentOrder),
            isManualConfigButtonVisible = currentOrder == CardOrder.MANUAL,
        )
    }

    fun toCardTagOrderViewData(currentOrder: CardTagOrder): CardOrderViewData {
        return CardOrderViewData(
            items = cardTagOrderList
                .map(::toCardTagOrderName)
                .map(CustomSpinner::CustomSpinnerTextItem),
            selectedPosition = toPosition(currentOrder),
            isManualConfigButtonVisible = currentOrder == CardTagOrder.MANUAL,
        )
    }

    fun toCardOrder(position: Int): CardOrder {
        return cardOrderList.getOrElse(position) { cardOrderList.first() }
    }

    fun toCardTagOrder(position: Int): CardTagOrder {
        return cardTagOrderList.getOrElse(position) { cardTagOrderList.first() }
    }

    fun toDurationFormatViewData(currentValue: DurationFormat): DurationFormatViewData {
        return DurationFormatViewData(
            items = durationFormatList
                .map(::toDurationFormatName)
                .map(CustomSpinner::CustomSpinnerTextItem),
            selectedPosition = toPosition(currentValue),
        )
    }

    fun toDurationFormat(position: Int): DurationFormat {
        return durationFormatList.getOrElse(position) { durationFormatList.first() }
    }

    fun toDaysInCalendarViewData(currentValue: DaysInCalendar): DaysInCalendarViewData {
        return DaysInCalendarViewData(
            items = daysInCalendarList
                .map(::toDaysInCalendarName)
                .map(CustomSpinner::CustomSpinnerTextItem),
            selectedPosition = toPosition(currentValue),
        )
    }

    fun toDaysInCalendar(position: Int): DaysInCalendar {
        return daysInCalendarList.getOrElse(position) { daysInCalendarList.first() }
    }

    fun toWidgetTransparencyViewData(currentValue: WidgetTransparencyPercent): WidgetTransparencyViewData {
        return WidgetTransparencyViewData(
            items = widgetTransparencyList
                .map(::toWidgetTransparencyName)
                .map(CustomSpinner::CustomSpinnerTextItem),
            selectedPosition = toPosition(currentValue),
            selectedValue = toWidgetTransparencyName(currentValue),
        )
    }

    fun toWidgetTransparency(position: Int): WidgetTransparencyPercent {
        return widgetTransparencyList.getOrElse(position) { widgetTransparencyList.first() }
    }

    fun toFirstDayOfWeekViewData(currentOrder: DayOfWeek): FirstDayOfWeekViewData {
        return FirstDayOfWeekViewData(
            items = dayOfWeekList
                .map(timeMapper::toShortDayOfWeekName)
                .map(CustomSpinner::CustomSpinnerTextItem),
            selectedPosition = toPosition(currentOrder),
        )
    }

    fun toDayOfWeek(position: Int): DayOfWeek {
        return dayOfWeekList.getOrElse(position) { dayOfWeekList.first() }
    }

    fun toRepeatButtonViewData(currentType: RepeatButtonType): RepeatButtonViewData {
        return RepeatButtonViewData(
            items = repeatButtonList
                .map {
                    when (it) {
                        is RepeatButtonType.RepeatLast -> R.string.settings_repeat_last_record
                        is RepeatButtonType.RepeatBeforeLast -> R.string.settings_repeat_one_before_last
                    }.let(resourceRepo::getString)
                }
                .map(CustomSpinner::CustomSpinnerTextItem),
            selectedPosition = toPosition(currentType),
        )
    }

    fun toRepeatButtonType(position: Int): RepeatButtonType {
        return repeatButtonList.getOrElse(position) { repeatButtonList.first() }
    }

    fun toDarkModeViewData(currentMode: DarkMode): DarkModeViewData {
        return DarkModeViewData(
            items = darkModeList
                .map {
                    when (it) {
                        DarkMode.System -> R.string.settings_dark_mode_system
                        DarkMode.Enabled -> R.string.settings_dark_mode_enabled
                        DarkMode.Disabled -> R.string.settings_inactivity_reminder_disabled
                    }.let(resourceRepo::getString)
                }
                .map(CustomSpinner::CustomSpinnerTextItem),
            selectedPosition = toPosition(currentMode),
        )
    }

    fun toDarkMode(position: Int): DarkMode {
        return darkModeList.getOrNull(position) ?: darkModeList.first()
    }

    fun toLanguageViewData(currentLanguage: String): LanguageViewData {
        return LanguageViewData(
            currentLanguageName = currentLanguage,
            items = LanguageInteractor.languageList
                .map(languageInteractor::getDisplayName)
                .map(CustomSpinner::CustomSpinnerTextItem),
        )
    }

    fun toLanguage(position: Int): String {
        val languageList = LanguageInteractor.languageList
        val language = languageList.getOrNull(position) ?: languageList.first()
        return languageInteractor.getTag(language)
    }

    fun toCsvExportDateTimeFormat(current: ExportDateTimeFormat): ExportDateTimeFormatViewData {
        return ExportDateTimeFormatViewData(
            items = exportDateTimeFormatList
                .map {
                    when (it) {
                        ExportDateTimeFormat.Local -> R.string.settings_date_time_format_local
                        ExportDateTimeFormat.Utc -> R.string.settings_date_time_format_utc
                        ExportDateTimeFormat.TimeZone -> R.string.settings_date_time_format_timezone
                    }.let(resourceRepo::getString)
                }
                .map(CustomSpinner::CustomSpinnerTextItem),
            selectedPosition = toPosition(current),
        )
    }

    fun toCsvExportDateTimeFormat(position: Int): ExportDateTimeFormat {
        return exportDateTimeFormatList.getOrNull(position) ?: exportDateTimeFormatList.first()
    }

    fun toDurationViewData(duration: Long): SettingsDurationViewData {
        return if (duration > 0) {
            SettingsDurationViewData(
                text = timeMapper.formatDuration(duration),
                enabled = true,
            )
        } else {
            SettingsDurationViewData(
                text = resourceRepo.getString(R.string.settings_inactivity_reminder_disabled),
                enabled = false,
            )
        }
    }

    fun toStartOfDayShift(
        timestamp: Long,
        wasPositive: Boolean,
    ): Long {
        val maxValue = TimeUnit.HOURS.toMillis(24) -
            TimeUnit.MINUTES.toMillis(1)

        return Calendar.getInstance()
            .apply { timeInMillis = timestamp }
            .run {
                val hours = get(Calendar.HOUR_OF_DAY).toLong()
                val minutes = get(Calendar.MINUTE).toLong()
                val seconds = get(Calendar.SECOND).toLong()
                val millis = get(Calendar.MILLISECOND).toLong()

                TimeUnit.HOURS.toMillis(hours) +
                    TimeUnit.MINUTES.toMillis(minutes) +
                    TimeUnit.SECONDS.toMillis(seconds) +
                    TimeUnit.MILLISECONDS.toMillis(millis)
            }
            .coerceIn(0..maxValue)
            .let { if (wasPositive) it else it * -1 }
    }

    // TODO many places showing hints with shiftTimeStamp but work without it,
    //  need to add shift to logic also? Or need to leave shift only for startOfDayShift?
    fun startOfDayShiftToTimeStamp(
        startOfDayShift: Long,
    ): Long {
        return Calendar.getInstance().shiftTimeStamp(
            timestamp = timeMapper.getStartOfDayTimeStamp(),
            shift = startOfDayShift.absoluteValue,
        )
    }

    fun toStartOfDayText(
        startOfDayShift: Long,
        useMilitaryTime: Boolean,
    ): String {
        val hintTime = startOfDayShiftToTimeStamp(startOfDayShift)
        return timeMapper.formatTime(
            time = hintTime,
            useMilitaryTime = useMilitaryTime,
            showSeconds = false,
        )
    }

    fun toStartOfDaySign(shift: Long): String {
        return when {
            shift == 0L -> ""
            shift > 0 -> resourceRepo.getString(R.string.plus_sign)
            else -> resourceRepo.getString(R.string.minus_sign)
        }
    }

    fun toUseMilitaryTimeHint(useMilitaryTime: Boolean): String {
        val hintTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 13)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return timeMapper.formatTime(
            time = hintTime,
            useMilitaryTime = useMilitaryTime,
            showSeconds = false,
        )
    }

    fun toUseMonthDayTimeHint(useMonthDay: Boolean): String {
        val hintTime = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 13)
            set(Calendar.MONTH, 6)
        }.timeInMillis

        return timeMapper.formatShortDay(
            time = hintTime,
            useMonthDayTimeFormat = useMonthDay,
        )
    }

    fun toDurationFormatHint(durationFormat: DurationFormat): String {
        return when (durationFormat) {
            DurationFormat.DAYS -> timeMapper.formatInterval(
                interval = TimeUnit.DAYS.toMillis(1) +
                    TimeUnit.HOURS.toMillis(1) +
                    TimeUnit.MINUTES.toMillis(15),
                forceSeconds = false,
                durationFormat = durationFormat,
            )
            else -> timeMapper.formatInterval(
                interval = TimeUnit.HOURS.toMillis(1) +
                    TimeUnit.MINUTES.toMillis(15),
                forceSeconds = false,
                durationFormat = durationFormat,
            )
        }
    }

    fun toCsvExportDateTimeFormatHint(format: ExportDateTimeFormat): String {
        return fileExportDateTimeFormatMapper.mapDateTime(
            format = format,
            timestamp = Calendar.getInstance().apply { setToStartOfDay() }.timeInMillis,
        )
    }

    fun mapCustomizeOptionsMenuListItems(): List<OptionsListParams.Item> {
        return listOf(
            OptionsListParams.Item(
                id = CustomizeOptionsMenuListItem.Records,
                text = resourceRepo.getString(R.string.shortcut_navigation_records),
                icon = R.drawable.tab_records,
            ),
            OptionsListParams.Item(
                id = CustomizeOptionsMenuListItem.Statistics,
                text = resourceRepo.getString(R.string.shortcut_navigation_statistics),
                icon = R.drawable.tab_statistics,
            ),
            OptionsListParams.Item(
                id = CustomizeOptionsMenuListItem.DetailedStatistics,
                text = resourceRepo.getString(R.string.settings_detailed_statistics),
                icon = R.drawable.tab_statistics,
            ),
        )
    }

    private fun toPosition(cardOrder: CardOrder): Int {
        return cardOrderList.indexOf(cardOrder).takeUnless { it == -1 }.orZero()
    }

    private fun toPosition(cardOrder: CardTagOrder): Int {
        return cardTagOrderList.indexOf(cardOrder).takeUnless { it == -1 }.orZero()
    }

    private fun toCardOrderName(cardOrder: CardOrder): String {
        return when (cardOrder) {
            CardOrder.NAME -> R.string.settings_sort_by_name
            CardOrder.COLOR -> R.string.settings_sort_by_color
            CardOrder.MANUAL -> R.string.settings_sort_manually
        }.let(resourceRepo::getString)
    }

    private fun toCardTagOrderName(cardOrder: CardTagOrder): String {
        return when (cardOrder) {
            CardTagOrder.NAME -> R.string.settings_sort_by_name
            CardTagOrder.COLOR -> R.string.settings_sort_by_color
            CardTagOrder.MANUAL -> R.string.settings_sort_manually
            CardTagOrder.ACTIVITY -> R.string.settings_sort_activity
        }.let(resourceRepo::getString)
    }

    private fun toPosition(durationFormat: DurationFormat): Int {
        return durationFormatList.indexOf(durationFormat).takeUnless { it == -1 }.orZero()
    }

    private fun toDurationFormatName(durationFormat: DurationFormat): String {
        return when (durationFormat) {
            DurationFormat.MINUTES -> R.string.minutes
            DurationFormat.HOURS -> R.string.hours
            DurationFormat.DAYS -> R.string.days
            DurationFormat.PROPORTIONAL_MINUTES -> R.string.settings_duration_format_proportional
        }.let(resourceRepo::getString)
    }

    private fun toPosition(daysInCalendar: DaysInCalendar): Int {
        return daysInCalendarList.indexOf(daysInCalendar).takeUnless { it == -1 }.orZero()
    }

    private fun toDaysInCalendarName(daysInCalendar: DaysInCalendar): String {
        return if (daysInCalendar == DaysInCalendar.WEEK) {
            resourceRepo.getString(R.string.range_week)
        } else {
            daysInCalendarMapper.mapDaysCount(daysInCalendar).toString()
        }
    }

    private fun toPosition(widgetTransparency: WidgetTransparencyPercent): Int {
        return widgetTransparencyList.indexOf(widgetTransparency).takeUnless { it == -1 }.orZero()
    }

    private fun toWidgetTransparencyName(widgetTransparency: WidgetTransparencyPercent): String {
        return "${widgetTransparency.value}%"
    }

    private fun toPosition(dayOfWeek: DayOfWeek): Int {
        return dayOfWeekList.indexOf(dayOfWeek).takeUnless { it == -1 }.orZero()
    }

    private fun toPosition(repeatButtonType: RepeatButtonType): Int {
        return repeatButtonList.indexOf(repeatButtonType).takeUnless { it == -1 }.orZero()
    }

    private fun toPosition(darkMode: DarkMode): Int {
        return darkModeList.indexOf(darkMode).takeUnless { it == -1 }.orZero()
    }

    private fun toPosition(exportDateTimeFormat: ExportDateTimeFormat): Int {
        return exportDateTimeFormatList.indexOf(exportDateTimeFormat).takeUnless { it == -1 }.orZero()
    }
}