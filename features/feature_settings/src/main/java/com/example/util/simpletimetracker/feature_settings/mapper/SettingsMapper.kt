package com.example.util.simpletimetracker.feature_settings.mapper

import android.text.SpannableString
import android.text.SpannableStringBuilder
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import com.example.util.simpletimetracker.core.extension.shiftTimeStamp
import com.example.util.simpletimetracker.core.interactor.LanguageInteractor
import com.example.util.simpletimetracker.core.manager.ClipboardManager
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.provider.ApplicationDataProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.ACTION_ADD_RECORD
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
import com.example.util.simpletimetracker.core.utils.EXTRA_TIME_ENDED
import com.example.util.simpletimetracker.core.utils.EXTRA_TIME_STARTED
import com.example.util.simpletimetracker.domain.extension.indexesOf
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.DarkMode
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.CardTagOrder
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.DaysInCalendar
import com.example.util.simpletimetracker.domain.model.RepeatButtonType
import com.example.util.simpletimetracker.domain.model.WidgetTransparencyPercent
import com.example.util.simpletimetracker.domain.model.count
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.viewData.CardOrderViewData
import com.example.util.simpletimetracker.feature_settings.viewData.DarkModeViewData
import com.example.util.simpletimetracker.feature_settings.viewData.DaysInCalendarViewData
import com.example.util.simpletimetracker.feature_settings.viewData.FirstDayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.viewData.LanguageViewData
import com.example.util.simpletimetracker.feature_settings.viewData.RepeatButtonViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsDurationViewData
import com.example.util.simpletimetracker.feature_settings.viewData.WidgetTransparencyViewData
import com.example.util.simpletimetracker.feature_views.extension.setClickableSpan
import com.example.util.simpletimetracker.feature_views.extension.setImageSpan
import com.example.util.simpletimetracker.feature_views.extension.toSpannableString
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.HelpDialogParams
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.absoluteValue

class SettingsMapper @Inject constructor(
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val clipboardManager: ClipboardManager,
    private val languageInteractor: LanguageInteractor,
    private val applicationDataProvider: ApplicationDataProvider,
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

    private val daysInCalendarList: List<DaysInCalendar> = listOf(
        DaysInCalendar.ONE,
        DaysInCalendar.THREE,
        DaysInCalendar.FIVE,
        DaysInCalendar.SEVEN,
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

    fun toAutomatedTrackingHelpDialog(
        isDarkTheme: Boolean,
    ): HelpDialogParams {
        val mainText = formatHelpText(
            R.string.settings_automated_tracking_text,
            isDarkTheme,
            listOf(
                HelpText(ACTION_START_ACTIVITY, canCopy = true),
                HelpText(ACTION_STOP_ACTIVITY, canCopy = true),
                HelpText(EXTRA_ACTIVITY_NAME, canCopy = true),
                HelpText(applicationDataProvider.getPackageName(), canCopy = true),
                HelpText(EXTRA_RECORD_COMMENT, canCopy = true),
                HelpText(EXTRA_RECORD_TAG_NAME, canCopy = true),
                HelpText(ACTION_STOP_ALL_ACTIVITIES, canCopy = true),
                HelpText(ACTION_STOP_SHORTEST_ACTIVITY, canCopy = true),
                HelpText(ACTION_STOP_LONGEST_ACTIVITY, canCopy = true),
                HelpText(ACTION_RESTART_ACTIVITY, canCopy = true),
                HelpText(ACTION_ADD_RECORD, canCopy = true),
                HelpText(EXTRA_ACTIVITY_NAME, canCopy = true),
                HelpText(EXTRA_RECORD_COMMENT, canCopy = true),
                HelpText(EXTRA_RECORD_TAG_NAME, canCopy = true),
                HelpText(EXTRA_TIME_STARTED, canCopy = true),
                HelpText(EXTRA_TIME_ENDED, canCopy = true),
            ),
        )

        val sendEventsText = formatHelpText(
            R.string.settings_automated_tracking_send_events_text,
            isDarkTheme,
            listOf(
                HelpText(resourceRepo.getString(R.string.settings_automated_tracking_send_events), canCopy = false),
                HelpText(EVENT_STARTED_ACTIVITY, canCopy = true),
                HelpText(EVENT_STOPPED_ACTIVITY, canCopy = true),
                HelpText(EXTRA_ACTIVITY_NAME, canCopy = true),
                HelpText(EXTRA_RECORD_COMMENT, canCopy = true),
                HelpText(EXTRA_RECORD_TAG_NAME, canCopy = true),
            ),
        )

        return HelpDialogParams(
            title = resourceRepo.getString(R.string.settings_automated_tracking),
            text = SpannableStringBuilder()
                .append(mainText)
                .append("\n")
                .append(sendEventsText),
        )
    }

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

    fun toUseProportionalMinutesHint(useProportionalMinutes: Boolean): String {
        return timeMapper.formatInterval(
            interval = 4500000,
            forceSeconds = false,
            useProportionalMinutes = useProportionalMinutes,
        )
    }

    private fun formatHelpText(
        @StringRes stringResId: Int,
        isDarkTheme: Boolean,
        arguments: List<HelpText>,
    ): SpannableString = runCatching {
        val imageTag = "IMAGE_TAG"
        val theme = if (isDarkTheme) R.style.AppThemeDark else R.style.AppTheme
        val copyableArguments = arguments.filter(HelpText::canCopy)

        fun insertImageTags(arguments: List<HelpText>): List<String> {
            return arguments.mapIndexed { index, arg ->
                if (arg.canCopy) {
                    "${arg.text} $imageTag"
                } else {
                    arg.text
                }
            }
        }

        fun insertClickableSpans(string: SpannableString): SpannableString {
            copyableArguments.map { it.text }.toSet().forEach { argument ->
                string.indexesOf(argument).forEach { index ->
                    string.setClickableSpan(
                        start = index,
                        // 1 is for space in between.
                        length = argument.length + imageTag.length + 1,
                        onClick = { copyToClipboard(argument) },
                    )
                }
            }
            return string
        }

        fun insertImageSpans(string: SpannableString): SpannableString {
            val icon = resourceRepo.getDrawable(R.drawable.action_copy)
                ?.mutate()
                ?.apply {
                    setTint(resourceRepo.getThemedAttr(R.attr.appTextHintColor, theme))
                }
                ?: return SpannableString("")

            string.indexesOf(imageTag).forEach { index ->
                string.setImageSpan(
                    start = index,
                    length = imageTag.length,
                    drawable = icon,
                    sizeDp = 16,
                )
            }
            return string
        }

        return insertImageTags(arguments)
            .let { resourceRepo.getString(stringResId).format(*it.toTypedArray()) }
            .let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY) }
            .toSpannableString()
            .let(::insertClickableSpans)
            .let(::insertImageSpans)
    }.getOrNull() ?: runCatching {
        // Fallback in case of spans error.
        resourceRepo.getString(stringResId)
            .format(*arguments.toTypedArray())
            .toSpannableString()
    }.getOrNull() ?: SpannableString("")

    private fun copyToClipboard(text: String) {
        clipboardManager.send(text)
        SnackBarParams(
            message = "Copied to clipboard\n\n$text",
            duration = SnackBarParams.Duration.ExtraShort,
            inDialog = true,
        ).let(router::show)
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

    private fun toPosition(daysInCalendar: DaysInCalendar): Int {
        return daysInCalendarList.indexOf(daysInCalendar).takeUnless { it == -1 }.orZero()
    }

    private fun toDaysInCalendarName(daysInCalendar: DaysInCalendar): String {
        return daysInCalendar.count.toString()
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

    private data class HelpText(
        val text: String,
        val canCopy: Boolean,
    )
}