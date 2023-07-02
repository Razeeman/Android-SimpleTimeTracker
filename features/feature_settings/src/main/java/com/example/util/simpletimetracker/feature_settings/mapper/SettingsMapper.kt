package com.example.util.simpletimetracker.feature_settings.mapper

import com.example.util.simpletimetracker.core.interactor.LanguageInteractor
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.provider.ApplicationDataProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.ACTION_RESTART_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_START_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_STOP_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_STOP_ALL_ACTIVITIES
import com.example.util.simpletimetracker.core.utils.ACTION_STOP_LONGEST_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_STOP_SHORTEST_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EVENT_STARTED_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EVENT_STOPPED_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EXTRA_ACTIVITY_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_COMMENT
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TAG_NAME
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.DarkMode
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.DaysInCalendar
import com.example.util.simpletimetracker.domain.model.count
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.viewData.CardOrderViewData
import com.example.util.simpletimetracker.feature_settings.viewData.DarkModeViewData
import com.example.util.simpletimetracker.feature_settings.viewData.DaysInCalendarViewData
import com.example.util.simpletimetracker.feature_settings.viewData.FirstDayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.viewData.LanguageViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsDurationViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.navigation.params.screen.HelpDialogParams
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.absoluteValue

class SettingsMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val languageInteractor: LanguageInteractor,
    private val applicationDataProvider: ApplicationDataProvider,
) {

    private val cardOrderList: List<CardOrder> = listOf(
        CardOrder.NAME,
        CardOrder.COLOR,
        CardOrder.MANUAL
    )

    private val daysInCalendarList: List<DaysInCalendar> = listOf(
        DaysInCalendar.ONE,
        DaysInCalendar.THREE,
        DaysInCalendar.FIVE,
        DaysInCalendar.SEVEN,
    )

    private val dayOfWeekList: List<DayOfWeek> = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    private val darkModeList: List<DarkMode> = listOf(
        DarkMode.System,
        DarkMode.Enabled,
        DarkMode.Disabled,
    )

    private val languageList: List<String> = listOf(
        "",
        "en",
        "ca",
        "de",
        "es",
        "fa",
        "fr",
        "hi",
        "in",
        "it",
        "ja",
        "nl",
        "pt",
        "ru",
        "sv",
        "tr",
        "uk",
        "zh",
        "zh-TW",
    )

    fun toAutomatedTrackingHelpDialog(): HelpDialogParams {
        return HelpDialogParams(
            title = resourceRepo.getString(R.string.settings_automated_tracking),
            text = resourceRepo.getString(
                R.string.settings_automated_tracking_text
            ).format(
                ACTION_START_ACTIVITY,
                ACTION_STOP_ACTIVITY,
                EXTRA_ACTIVITY_NAME,
                applicationDataProvider.getPackageName(),
                EXTRA_RECORD_COMMENT,
                EXTRA_RECORD_TAG_NAME,
                ACTION_STOP_ALL_ACTIVITIES,
                ACTION_STOP_SHORTEST_ACTIVITY,
                ACTION_STOP_LONGEST_ACTIVITY,
                ACTION_RESTART_ACTIVITY,
            ) + "<br/>" + resourceRepo.getString(
                R.string.settings_automated_tracking_send_events_text
            ).format(
                resourceRepo.getString(R.string.settings_automated_tracking_send_events),
                EVENT_STARTED_ACTIVITY,
                EVENT_STOPPED_ACTIVITY,
                EXTRA_ACTIVITY_NAME,
                EXTRA_RECORD_COMMENT,
                EXTRA_RECORD_TAG_NAME,
            ),
        )
    }

    fun toCardOrderViewData(currentOrder: CardOrder): CardOrderViewData {
        return CardOrderViewData(
            items = cardOrderList
                .map(::toCardOrderName)
                .map(CustomSpinner::CustomSpinnerTextItem),
            selectedPosition = toPosition(currentOrder),
            isManualConfigButtonVisible = currentOrder == CardOrder.MANUAL
        )
    }

    fun toCardOrder(position: Int): CardOrder {
        return cardOrderList.getOrElse(position) { cardOrderList.first() }
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

    fun toFirstDayOfWeekViewData(currentOrder: DayOfWeek): FirstDayOfWeekViewData {
        return FirstDayOfWeekViewData(
            items = dayOfWeekList
                .map(timeMapper::toShortDayOfWeekName)
                .map(CustomSpinner::CustomSpinnerTextItem),
            selectedPosition = toPosition(currentOrder)
        )
    }

    fun toDayOfWeek(position: Int): DayOfWeek {
        return dayOfWeekList.getOrElse(position) { dayOfWeekList.first() }
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
            selectedPosition = toPosition(currentMode)
        )
    }

    fun toDarkMode(position: Int): DarkMode {
        return darkModeList.getOrNull(position) ?: darkModeList.first()
    }

    fun toLanguageViewData(currentLanguage: String): LanguageViewData {
        return LanguageViewData(
            currentLanguageName = currentLanguage,
            items = languageList
                .map {
                    if (it.isEmpty()) {
                        resourceRepo.getString(R.string.settings_dark_mode_system)
                    } else {
                        languageInteractor.getDisplayName(it)
                    }
                }
                .map(CustomSpinner::CustomSpinnerTextItem),
        )
    }

    fun toLanguage(position: Int): String {
        return languageList.getOrNull(position) ?: languageList.first()
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
        val maxValue = TimeUnit.HOURS.toMillis(24) - TimeUnit.MINUTES.toMillis(1)
        return (timestamp - timeMapper.getStartOfDayTimeStamp()).coerceIn(0..maxValue)
            .let { if (wasPositive) it else it * -1 }
    }

    fun startOfDayShiftToTimeStamp(
        startOfDayShift: Long,
    ): Long {
        return timeMapper.getStartOfDayTimeStamp() + startOfDayShift.absoluteValue
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

    fun toUseProportionalMinutesHint(useProportionalMinutes: Boolean): String {
        return timeMapper.formatInterval(
            interval = 4500000,
            forceSeconds = false,
            useProportionalMinutes = useProportionalMinutes
        )
    }

    private fun toPosition(cardOrder: CardOrder): Int {
        return cardOrderList.indexOf(cardOrder).takeUnless { it == -1 }.orZero()
    }

    private fun toCardOrderName(cardOrder: CardOrder): String {
        return when (cardOrder) {
            CardOrder.NAME -> R.string.settings_sort_by_name
            CardOrder.COLOR -> R.string.settings_sort_by_color
            CardOrder.MANUAL -> R.string.settings_sort_manually
        }.let(resourceRepo::getString)
    }

    private fun toPosition(daysInCalendar: DaysInCalendar): Int {
        return daysInCalendarList.indexOf(daysInCalendar).takeUnless { it == -1 }.orZero()
    }

    private fun toDaysInCalendarName(daysInCalendar: DaysInCalendar): String {
        return daysInCalendar.count.toString()
    }

    private fun toPosition(dayOfWeek: DayOfWeek): Int {
        return dayOfWeekList.indexOf(dayOfWeek).takeUnless { it == -1 }.orZero()
    }

    private fun toPosition(darkMode: DarkMode): Int {
        return darkModeList.indexOf(darkMode).takeUnless { it == -1 }.orZero()
    }
}