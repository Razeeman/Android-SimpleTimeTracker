package com.example.util.simpletimetracker

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.extension.setWeekToFirstDay
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnSpinnerWithId
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import com.example.util.simpletimetracker.utils.withPluralText
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsTest : BaseUiTest() {

    @Test
    fun showUntrackedSetting() {
        val name = "Test"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)

        // Untracked is not shown
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(
            allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())
        )

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(R.id.checkboxSettingsShowUntracked)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowUntracked))
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isChecked()))

        // Untracked is shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))

        // Add record
        NavUtils.addRecord(name)
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowUntracked)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowUntracked))
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isNotChecked()))

        // Untracked is not shown
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(
            allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
    }

    @Test
    fun allowMultitaskingSetting() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Test3"

        // Add activities
        NavUtils.openRecordsScreen()
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)
        NavUtils.openRunningRecordsScreen()

        // Start timers
        tryAction { clickOnViewWithText(name2) }
        clickOnViewWithText(name3)
        tryAction { checkViewIsDisplayed(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name2)))) }
        checkViewIsDisplayed(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name3))))

        // Click on already running
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name3), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsAllowMultitasking))
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isNotChecked()))

        // Click on one not running
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
        tryAction { checkViewIsDisplayed(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name1)))) }
        checkViewDoesNotExist(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name2))))
        checkViewDoesNotExist(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name3))))

        // Records added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name3), isCompletelyDisplayed()))

        // Click another
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
        tryAction { checkViewIsDisplayed(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name2)))) }
        checkViewDoesNotExist(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name1))))

        // Record added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))

        // Change setting back
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsAllowMultitasking))
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isChecked()))

        // Start another timer
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3)))
        tryAction { checkViewIsDisplayed(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name2)))) }
        checkViewIsDisplayed(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name3))))

        // No new records added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name3), isCompletelyDisplayed()))
    }

    @Test
    fun enableNotifications() {
        val name1 = "Test1"
        val name2 = "Test2"

        // Add activities
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)

        // Start one timer
        tryAction { clickOnViewWithText(name1) }

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowNotifications)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowNotifications)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowNotifications))
        onView(withId(R.id.checkboxSettingsShowNotifications)).check(matches(isChecked()))

        // Stop first timer
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name1))))

        // Start another timer
        clickOnViewWithText(name2)

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowNotifications)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowNotifications)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowNotifications))
        onView(withId(R.id.checkboxSettingsShowNotifications)).check(matches(isNotChecked()))
    }

    @Test
    fun enableEnableDarkMode() {
        val name1 = "Test1"
        val name2 = "Test2"

        // Add activities
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)

        // Start one timer
        tryAction { clickOnViewWithText(name1) }

        // Add record
        testUtils.addRecord(name1)
        testUtils.addRecord(name2)

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsDarkMode)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsDarkMode)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsDarkMode))
        onView(withId(R.id.checkboxSettingsDarkMode)).check(matches(isChecked()))

        // Check screens
        NavUtils.openRunningRecordsScreen()
        NavUtils.openRecordsScreen()
        NavUtils.openStatisticsScreen()

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsDarkMode)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsDarkMode)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsDarkMode))
        onView(withId(R.id.checkboxSettingsDarkMode)).check(matches(isNotChecked()))

        // Check screens
        NavUtils.openRunningRecordsScreen()
        NavUtils.openRecordsScreen()
        NavUtils.openStatisticsScreen()
        NavUtils.openSettingsScreen()
    }

    @Test
    fun inactivityReminder() {
        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(R.id.groupSettingsInactivityReminder)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(
                withId(R.id.tvSettingsInactivityReminderTime),
                withText(R.string.settings_inactivity_reminder_disabled)
            )
        )

        // 1s
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clickOnViewWithId(R.id.tvNumberKeyboard1)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$secondString"))

        // 1m
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$minuteString"))

        // 1h
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString"))

        // 1m 1s
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clickOnViewWithId(R.id.ivDurationPickerDelete)
        clickOnViewWithId(R.id.ivDurationPickerDelete)
        clickOnViewWithId(R.id.ivDurationPickerDelete)
        clickOnViewWithId(R.id.tvNumberKeyboard1)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$minuteString 01$secondString"))

        // 1h 1m 1s
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithId(R.id.tvNumberKeyboard1)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString 01$minuteString 01$secondString"))

        // 1h 30m
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clearDuration()
        clickOnViewWithId(R.id.tvNumberKeyboard9)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString 30$minuteString"))

        // 99h 99m 99s
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        repeat(10) { clickOnViewWithId(R.id.ivDurationPickerDelete) }
        repeat(6) { clickOnViewWithId(R.id.tvNumberKeyboard9) }
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("100$hourString 40$minuteString 39$secondString"))

        // Disable
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clickOnViewWithText(R.string.duration_dialog_disable)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.tvSettingsInactivityReminderTime),
                withText(R.string.settings_inactivity_reminder_disabled)
            )
        )
    }

    @Test
    fun ignoreShortRecords() {
        val name = "Test"

        // Add data
        testUtils.addActivity(name)

        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(R.id.groupSettingsIgnoreShortRecords)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(
                withId(R.id.tvSettingsIgnoreShortRecordsTime),
                withText(R.string.settings_inactivity_reminder_disabled)
            )
        )

        clickOnViewWithId(R.id.groupSettingsIgnoreShortRecords)
        clickOnViewWithId(R.id.tvNumberKeyboard3)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("3$secondString"))

        // Check record ignored
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))

        // Disable
        NavUtils.openSettingsScreen()
        onView(withId(R.id.groupSettingsIgnoreShortRecords)).perform(nestedScrollTo())
        clickOnViewWithId(R.id.groupSettingsIgnoreShortRecords)
        clickOnViewWithText(R.string.duration_dialog_disable)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.tvSettingsIgnoreShortRecordsTime),
                withText(R.string.settings_inactivity_reminder_disabled)
            )
        )

        // Check record not ignored
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
    }

    @Test
    fun militaryTime() {
        // Check settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(R.id.tvSettingsUseMilitaryTimeHint)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsUseMilitaryTime)).check(matches(isChecked()))
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsUseMilitaryTimeHint), withText("13:00")))

        // Change settings
        clickOnViewWithId(R.id.checkboxSettingsUseMilitaryTime)
        onView(withId(R.id.checkboxSettingsUseMilitaryTime)).check(matches(isNotChecked()))
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsUseMilitaryTimeHint), withSubstring("1:00")))

        // Change settings
        clickOnViewWithId(R.id.checkboxSettingsUseMilitaryTime)
        onView(withId(R.id.checkboxSettingsUseMilitaryTime)).check(matches(isChecked()))
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsUseMilitaryTimeHint), withText("13:00")))
    }

    @Test
    fun proportionalMinutes() {
        val name = "Test"

        fun checkView(id: Int, text: String) {
            checkViewIsDisplayed(allOf(withId(id), hasDescendant(withText(text)), isCompletelyDisplayed()))
        }

        fun checkFormat(timeString: String) {
            NavUtils.openRecordsScreen()
            checkView(R.id.viewRecordItem, timeString)
            NavUtils.openStatisticsScreen()
            checkView(R.id.viewStatisticsItem, timeString)
            tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }
            checkView(R.id.cardStatisticsDetailTotal, timeString)
            pressBack()
        }

        // Add data
        val timeEnded = System.currentTimeMillis()
        val timeStarted = timeEnded - TimeUnit.MINUTES.toMillis(75)
        val timeFormat1 = "1$hourString 15$minuteString"
        val timeFormat2 = "%.2f$hourString".format(1.25)
        testUtils.addActivity(name)
        testUtils.addRecord(name, timeStarted, timeEnded)

        // Check format
        checkFormat(timeFormat1)

        // Check settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(R.id.tvSettingsUseProportionalMinutesHint)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsUseProportionalMinutes)).check(matches(isNotChecked()))
        checkViewIsDisplayed(
            allOf(withId(R.id.tvSettingsUseProportionalMinutesHint), withText(timeFormat1))
        )

        // Change settings
        clickOnViewWithId(R.id.checkboxSettingsUseProportionalMinutes)
        onView(withId(R.id.checkboxSettingsUseProportionalMinutes)).check(matches(isChecked()))
        checkViewIsDisplayed(
            allOf(withId(R.id.tvSettingsUseProportionalMinutesHint), withSubstring(timeFormat2))
        )

        // Check format after setting change
        checkFormat(timeFormat2)

        // Change settings back
        NavUtils.openSettingsScreen()
        onView(withId(R.id.tvSettingsUseProportionalMinutesHint)).perform(nestedScrollTo())
        clickOnViewWithId(R.id.checkboxSettingsUseProportionalMinutes)
        onView(withId(R.id.checkboxSettingsUseProportionalMinutes)).check(matches(isNotChecked()))
        checkViewIsDisplayed(
            allOf(withId(R.id.tvSettingsUseProportionalMinutesHint), withText(timeFormat1))
        )

        // Check format again
        checkFormat(timeFormat1)
    }

    @Test
    fun keepScreenOn() {
        // Check settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(R.id.tvSettingsKeepScreenOn)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsKeepScreenOn)).check(matches(isNotChecked()))

        // Change settings
        clickOnViewWithId(R.id.checkboxSettingsKeepScreenOn)
        onView(withId(R.id.checkboxSettingsKeepScreenOn)).check(matches(isChecked()))

        // Change settings
        clickOnViewWithId(R.id.checkboxSettingsKeepScreenOn)
        onView(withId(R.id.checkboxSettingsKeepScreenOn)).check(matches(isNotChecked()))
    }

    @Test
    fun firstDayOfWeek() {
        // If today is sunday:
        // add record for previous monday,
        // then select first day monday - record will be present this week,
        // then select first day sunday - record will be prev week.
        // If today is not sunday:
        // add record for prev sunday,
        // then select first day sunday - record will be present this week,
        // then select first day monday - record will be prev week.

        val name = "Test"
        val isTodaySunday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

        // Add data
        testUtils.addActivity(name)
        val calendar = Calendar.getInstance()
            .apply {
                val recordDay = if (isTodaySunday) Calendar.MONDAY else Calendar.SUNDAY
                firstDayOfWeek = recordDay
                setWeekToFirstDay()
                set(Calendar.HOUR_OF_DAY, 15)
            }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        clickOnSpinnerWithId(R.id.spinnerSettingsFirstDayOfWeek)
        if (isTodaySunday) {
            clickOnViewWithText(R.string.day_of_week_monday)
        } else {
            clickOnViewWithText(R.string.day_of_week_sunday)
        }

        // Check statistics
        NavUtils.openStatisticsScreen()
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.range_week)
        clickOnView(
            allOf(
                withId(R.id.viewStatisticsItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )

        // Check detailed statistics
        clickOnViewWithId(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_week)
        checkViewIsDisplayed(
            allOf(
                withPluralText(R.plurals.statistics_detail_times_tracked, 1),
                hasSibling(withText("1")),
                isCompletelyDisplayed()
            )
        )

        // Check range titles
        var titlePrev = timeMapper.toWeekTitle(
            weeksFromToday = -1,
            startOfDayShift = 0,
            firstDayOfWeek = if (isTodaySunday) DayOfWeek.MONDAY else DayOfWeek.SUNDAY
        )
        longClickOnViewWithId(R.id.btnStatisticsDetailToday)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))
        pressBack()
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)

        // Change setting
        NavUtils.openSettingsScreen()
        clickOnSpinnerWithId(R.id.spinnerSettingsFirstDayOfWeek)
        if (isTodaySunday) {
            clickOnViewWithText(R.string.day_of_week_sunday)
        } else {
            clickOnViewWithText(R.string.day_of_week_monday)
        }

        // Check statistics
        NavUtils.openStatisticsScreen()
        checkViewDoesNotExist(
            allOf(
                withId(R.id.viewStatisticsItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnView(
            allOf(
                withId(R.id.viewStatisticsItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )

        // Check detailed statistics
        clickOnViewWithId(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_week)
        checkViewIsDisplayed(
            allOf(
                withPluralText(R.plurals.statistics_detail_times_tracked, 0),
                hasSibling(withText("0")),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkViewIsDisplayed(
            allOf(
                withPluralText(R.plurals.statistics_detail_times_tracked, 1),
                hasSibling(withText("1")),
                isCompletelyDisplayed()
            )
        )

        // Check range titles
        titlePrev = timeMapper.toWeekTitle(
            weeksFromToday = -1,
            startOfDayShift = 0,
            firstDayOfWeek = if (isTodaySunday) DayOfWeek.SUNDAY else DayOfWeek.MONDAY
        )
        longClickOnViewWithId(R.id.btnStatisticsDetailToday)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))
        pressBack()
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
    }

    @Test
    fun startOfDay() {
        fun Long.toTimePreview() = timeMapper.formatTime(time = this, useMilitaryTime = true, showSeconds = false)

        val name = "Test"

        // Add data
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        testUtils.addActivity(name)
        val calendar = Calendar.getInstance().apply {
            setToStartOfDay()
            add(Calendar.DATE, -2)
        }
        var startOfDayTimeStamp = calendar.timeInMillis
        val timeStartedTimeStamp = calendar.timeInMillis + TimeUnit.HOURS.toMillis(22)
        val timeEndedTimeStamp = calendar.timeInMillis + TimeUnit.HOURS.toMillis(26)
        var startOfDayPreview = startOfDayTimeStamp.toTimePreview()
        val timeStartedPreview = timeStartedTimeStamp.toTimePreview()
        val timeEndedPreview = timeEndedTimeStamp.toTimePreview()
        testUtils.addRecord(
            typeName = name,
            timeStarted = timeStartedTimeStamp,
            timeEnded = timeEndedTimeStamp
        )

        // Check records
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkRecord(
            nameResId = R.string.untracked_time_name, timeStart = startOfDayPreview, timeEnd = startOfDayPreview,
        )
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = startOfDayPreview)
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = startOfDayPreview, timeEnd = timeEndedPreview)

        // Check statistics
        NavUtils.openStatisticsScreen()
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 22)
        checkStatisticsItem(name = name, hours = 2)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 22)
        checkStatisticsItem(name = name, hours = 2)

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnView(allOf(withId(R.id.btnStatisticsDetailToday), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Check setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(R.id.tvSettingsStartOfDayTime)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsNotDisplayed(withId(R.id.btnSettingsStartOfDaySign))

        // Change setting to +1
        clickOnView(withId(R.id.groupSettingsStartOfDay))
        onView(withClassName(equalTo(TimePicker::class.java.name))).perform(setTime(1, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        startOfDayTimeStamp = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        // Check new setting
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsDisplayed(
            allOf(withId(R.id.btnSettingsStartOfDaySign), hasDescendant(withText(R.string.plus_sign)))
        )

        // Check records
        NavUtils.openRecordsScreen()
        longClickOnViewWithId(R.id.btnRecordsContainerToday)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkRecord(
            nameResId = R.string.untracked_time_name, timeStart = startOfDayPreview, timeEnd = startOfDayPreview,
        )
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = startOfDayPreview)
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = startOfDayPreview, timeEnd = timeEndedPreview)

        // Check statistics
        NavUtils.openStatisticsScreen()
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 21)
        checkStatisticsItem(name = name, hours = 3)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 23)
        checkStatisticsItem(name = name, hours = 1)

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnView(allOf(withId(R.id.btnStatisticsDetailToday), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Change setting to -1
        NavUtils.openSettingsScreen()
        onView(withId(R.id.btnSettingsStartOfDaySign)).perform(nestedScrollTo(), click())

        // Check new setting
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsDisplayed(
            allOf(withId(R.id.btnSettingsStartOfDaySign), hasDescendant(withText(R.string.minus_sign)))
        )

        startOfDayTimeStamp = calendar.timeInMillis - TimeUnit.HOURS.toMillis(1)
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        // Check records
        NavUtils.openRecordsScreen()
        longClickOnViewWithId(R.id.btnRecordsContainerToday)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkRecord(
            nameResId = R.string.untracked_time_name, timeStart = startOfDayPreview, timeEnd = startOfDayPreview,
        )
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = startOfDayPreview)
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = startOfDayPreview, timeEnd = timeEndedPreview)

        // Check statistics
        NavUtils.openStatisticsScreen()
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 23)
        checkStatisticsItem(name = name, hours = 1)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 21)
        checkStatisticsItem(name = name, hours = 3)

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnView(allOf(withId(R.id.btnStatisticsDetailToday), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Change setting to +2, record will be shifted out from one day
        startOfDayTimeStamp = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2)
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        NavUtils.openSettingsScreen()
        onView(withId(R.id.groupSettingsStartOfDay)).perform(nestedScrollTo(), click())
        onView(withClassName(equalTo(TimePicker::class.java.name))).perform(setTime(2, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsDisplayed(
            allOf(withId(R.id.btnSettingsStartOfDaySign), hasDescendant(withText(R.string.minus_sign)))
        )
        onView(withId(R.id.btnSettingsStartOfDaySign)).perform(nestedScrollTo(), click())
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))

        // Check records
        NavUtils.openRecordsScreen()
        longClickOnViewWithId(R.id.btnRecordsContainerToday)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkRecord(
            nameResId = R.string.untracked_time_name, timeStart = startOfDayPreview, timeEnd = startOfDayPreview,
        )
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = timeEndedPreview)
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(
            nameResId = R.string.untracked_time_name, timeStart = startOfDayPreview, timeEnd = startOfDayPreview,
        )
        // Check statistics
        NavUtils.openStatisticsScreen()
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 20)
        checkStatisticsItem(name = name, hours = 4)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 24)

        // Check detailed statistics
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnView(allOf(withId(R.id.btnStatisticsDetailToday), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Change setting to 0
        startOfDayTimeStamp = calendar.timeInMillis
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        NavUtils.openSettingsScreen()
        onView(withId(R.id.groupSettingsStartOfDay)).perform(nestedScrollTo(), click())
        onView(withClassName(equalTo(TimePicker::class.java.name))).perform(setTime(0, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsNotDisplayed(withId(R.id.btnSettingsStartOfDaySign))
    }

    @Test
    fun showRecordTagSelection() {
        val name = "TypeName"
        val tag = "TagName"
        val tagGeneral = "TagGeneral"
        val fullName = "$name - $tag"

        // Add data
        testUtils.addActivity(name)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name)))) }

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowRecordTagSelection))
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).check(matches(isChecked()))

        // No tags - started right away
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }

        // Add tag
        testUtils.addRecordTag(tag, name)
        testUtils.addRecordTag(tagGeneral)

        // Has a tag - show dialog
        clickOnViewWithText(name)
        tryAction { checkViewIsDisplayed(withText(R.string.change_record_untagged)) }
        checkViewIsDisplayed(withText(tag))
        pressBack()

        // Start untagged
        clickOnViewWithText(name)
        tryAction { clickOnView(withText(tag)) }
        clickOnView(withText(R.string.change_record_untagged))
        clickOnViewWithText(R.string.duration_dialog_save)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }

        // Start tagged
        clickOnViewWithText(name)
        tryAction { clickOnView(withText(tag)) }
        clickOnViewWithText(R.string.duration_dialog_save)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(fullName))) }

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowRecordTagSelection))
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).check(matches(isNotChecked()))

        // Start with tags - no dialog
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }
    }

    @Test
    fun recordTagSelectionClose() {
        val name = "TypeName"
        val tag = "TagName"
        val tagGeneral = "TagGeneral"
        val fullName = "$name - $tag"
        val fullName2 = "$name - $tagGeneral, $tag"

        // Add data
        testUtils.addActivity(name)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        checkViewIsNotDisplayed(withText(R.string.settings_show_record_tag_close_hint))
        checkViewIsNotDisplayed(withId(R.id.checkboxSettingsRecordTagSelectionClose))

        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowRecordTagSelection))
        onView(withId(R.id.checkboxSettingsRecordTagSelectionClose)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(R.string.settings_show_record_tag_close_hint))
        onView(withId(R.id.checkboxSettingsRecordTagSelectionClose)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsRecordTagSelectionClose))
        onView(withId(R.id.checkboxSettingsRecordTagSelectionClose)).check(matches(isChecked()))

        // No tags - started right away
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }

        // Add tag
        testUtils.addRecordTag(tag, name)
        testUtils.addRecordTag(tagGeneral)

        // Start after one tag selected
        clickOnViewWithText(name)
        clickOnView(withText(tag))
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(fullName))) }

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsRecordTagSelectionClose)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsRecordTagSelectionClose)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsRecordTagSelectionClose))
        onView(withId(R.id.checkboxSettingsRecordTagSelectionClose)).check(matches(isNotChecked()))

        // Start with several tags
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        clickOnView(withText(tag))
        clickOnView(withText(tagGeneral))
        clickOnViewWithText(R.string.duration_dialog_save)
        tryAction { clickOnView(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(fullName2)))) }

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(R.string.settings_show_record_tag_close_hint))
        checkViewIsDisplayed(withId(R.id.checkboxSettingsRecordTagSelectionClose))

        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowRecordTagSelection))
        checkViewIsNotDisplayed(withText(R.string.settings_show_record_tag_close_hint))
        checkViewIsNotDisplayed(withId(R.id.checkboxSettingsRecordTagSelectionClose))
    }

    @Test
    fun csvExportSettings() {
        NavUtils.openSettingsScreen()
        onView(withId(R.id.layoutSettingsExportCsv)).perform(nestedScrollTo(), click())

        // View is set up
        val currentTime = System.currentTimeMillis()
        var timeStarted = timeMapper.formatDateTime(
            time = currentTime - TimeUnit.DAYS.toMillis(7), useMilitaryTime = true, showSeconds = false
        )
        var timeEnded = timeMapper.formatDateTime(
            time = currentTime, useMilitaryTime = true, showSeconds = false
        )
        checkViewIsDisplayed(allOf(withId(R.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(R.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }
        val hourStarted = 15
        val minutesStarted = 16
        val hourEnded = 17
        val minutesEnded = 19
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Set time started
        clickOnViewWithId(R.id.tvCsvExportSettingsTimeStarted)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(setDate(year, month + 1, day))
        clickOnView(allOf(isDescendantOfA(withId(R.id.tabsDateTimeDialog)), withText(R.string.date_time_dialog_time)))
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        // Check time set
        val timeStartedTimestamp = Calendar.getInstance().run {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hourStarted)
            set(Calendar.MINUTE, minutesStarted)
            timeInMillis
        }
        timeStarted = timeStartedTimestamp
            .let { timeMapper.formatDateTime(time = it, useMilitaryTime = true, showSeconds = false) }

        checkViewIsDisplayed(allOf(withId(R.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))

        // Set time ended
        clickOnViewWithId(R.id.tvCsvExportSettingsTimeEnded)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(setDate(year, month + 1, day))
        clickOnView(allOf(isDescendantOfA(withId(R.id.tabsDateTimeDialog)), withText(R.string.date_time_dialog_time)))
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(setTime(hourEnded, minutesEnded))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        // Check time set
        val timeEndedTimestamp = Calendar.getInstance().run {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hourEnded)
            set(Calendar.MINUTE, minutesEnded)
            timeInMillis
        }
        timeEnded = timeEndedTimestamp
            .let { timeMapper.formatDateTime(time = it, useMilitaryTime = true, showSeconds = false) }

        checkViewIsDisplayed(allOf(withId(R.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))
    }

    @Test
    fun showRecordsCalendar() {
        val name = "Test"

        // Add activity
        testUtils.addActivity(name = name)
        testUtils.addRecord(name)

        // Record is shown
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withId(R.id.viewRecordsCalendar), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.tvRecordsCalendarHint), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.rvRecordsList), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(R.id.checkboxSettingsShowRecordsCalendar)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowRecordsCalendar)).check(matches(isNotChecked()))
        checkViewIsNotDisplayed(withText(R.string.settings_reverse_order_in_calendar))
        checkViewIsNotDisplayed(withId(R.id.checkboxSettingsReverseOrderInCalendar))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowRecordsCalendar))
        onView(withId(R.id.checkboxSettingsShowRecordsCalendar)).check(matches(isChecked()))

        // Record is not shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withId(R.id.viewRecordsCalendar), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvRecordsCalendarHint), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.rvRecordsList), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))

        // Check reverse order
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsReverseOrderInCalendar)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(R.string.settings_reverse_order_in_calendar))
        checkViewIsDisplayed(withId(R.id.checkboxSettingsReverseOrderInCalendar))
        onView(withId(R.id.checkboxSettingsReverseOrderInCalendar)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsReverseOrderInCalendar))
        onView(withId(R.id.checkboxSettingsReverseOrderInCalendar)).check(matches(isChecked()))
        NavUtils.openRecordsScreen()

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowRecordsCalendar)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowRecordsCalendar)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowRecordsCalendar))
        onView(withId(R.id.checkboxSettingsShowRecordsCalendar)).check(matches(isNotChecked()))
        checkViewIsNotDisplayed(withText(R.string.settings_reverse_order_in_calendar))
        checkViewIsNotDisplayed(withId(R.id.checkboxSettingsReverseOrderInCalendar))

        // Record is shown
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withId(R.id.viewRecordsCalendar), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.tvRecordsCalendarHint), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.rvRecordsList), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
    }

    @Test
    fun keepStatisticsRange() {
        val name = "Test"

        // Add activity
        testUtils.addActivity(name = name)
        testUtils.addRecord(name)

        // Check range not transferred
        NavUtils.openStatisticsScreen()
        clickOnViewWithIdOnPager(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.range_week)
        checkViewIsDisplayed(allOf(withText(R.string.title_this_week), isCompletelyDisplayed()))
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(R.string.title_today), isCompletelyDisplayed()))
        pressBack()

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(R.id.checkboxSettingsKeepStatisticsRange)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsKeepStatisticsRange)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsKeepStatisticsRange))
        onView(withId(R.id.checkboxSettingsKeepStatisticsRange)).check(matches(isChecked()))

        // Check range transfer
        NavUtils.openStatisticsScreen()
        checkViewIsDisplayed(allOf(withText(R.string.title_this_week), isCompletelyDisplayed()))
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(R.string.title_this_week), isCompletelyDisplayed()))
        pressBack()
    }

    @Test
    fun automatedTracking() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(R.id.btnSettingsAutomatedTracking)).perform(nestedScrollTo(), click())
        checkViewIsDisplayed(
            allOf(withId(R.id.tvHelpDialogTitle), withText(R.string.settings_automated_tracking))
        )
    }

    @Test
    fun showFiltersOnMain() {
        val name = "ActivityFilter"

        // Add filter
        testUtils.addActivityFilter(name, ActivityFilter.Type.Activity)

        // Filters not shown
        tryAction {
            checkViewIsDisplayed(withText(R.string.running_records_add_type))
            checkViewDoesNotExist(withText(R.string.running_records_add_filter))
            checkViewDoesNotExist(withText(name))
        }

        // Check settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(R.id.checkboxSettingsShowActivityFilters)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowActivityFilters)).check(matches(isNotChecked()))

        // Change setting
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowActivityFilters))
        onView(withId(R.id.checkboxSettingsShowActivityFilters)).check(matches(isChecked()))

        // Filters shown
        NavUtils.openRunningRecordsScreen()
        checkViewIsDisplayed(withText(R.string.running_records_add_filter))
        checkViewIsDisplayed(withText(name))
    }

    private fun clearDuration() {
        repeat(6) { clickOnViewWithId(R.id.ivDurationPickerDelete) }
    }

    private fun checkRecord(
        name: String = "",
        nameResId: Int? = null,
        timeStart: String,
        timeEnd: String,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(if (nameResId != null) withText(nameResId) else withText(name)),
                hasDescendant(allOf(withId(R.id.tvRecordItemTimeStarted), withText(timeStart))),
                hasDescendant(allOf(withId(R.id.tvRecordItemTimeFinished), withText(timeEnd))),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkStatisticsItem(
        name: String = "",
        nameResId: Int? = null,
        hours: Int,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                hasDescendant(if (nameResId != null) withText(nameResId) else withText(name)),
                hasDescendant(withSubstring("$hours$hourString 0$minuteString")),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkStatisticsDetailRecords(count: Int) {
        checkViewIsDisplayed(
            allOf(
                withPluralText(R.plurals.statistics_detail_times_tracked, count),
                hasSibling(withText(count.toString())),
                isCompletelyDisplayed()
            )
        )
    }
}
