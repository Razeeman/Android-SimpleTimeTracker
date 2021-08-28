package com.example.util.simpletimetracker

import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyRightOf
import androidx.test.espresso.assertion.PositionAssertions.isLeftAlignedWith
import androidx.test.espresso.assertion.PositionAssertions.isTopAlignedWith
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.util.simpletimetracker.core.extension.setWeekToFirstDay
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.Direction
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnSpinnerWithId
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.drag
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import com.example.util.simpletimetracker.utils.withPluralText
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
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
        val color = ColorMapper.getAvailableColors().first()
        val icon = iconImageMapper.availableIconsNames.values.first()

        // Add activity
        testUtils.addActivity(name, color, icon)

        // Untracked is shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))

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

        // Add record
        NavUtils.addRecord(name)
        checkViewDoesNotExist(
            allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowUntracked)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowUntracked))
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isChecked()))

        // Untracked is shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
    }

    @Test
    fun allowMultitaskingSetting() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Test3"

        // Add activities
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)

        // Start timers
        tryAction { clickOnViewWithText(name2) }
        clickOnViewWithText(name3)
        var startTime = System.currentTimeMillis()
            .let { timeMapper.formatTime(it, true) }
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRunningRecordItem),
                    hasDescendant(withText(name2)),
                    hasDescendant(withText(startTime))
                )
            )
        }
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name3)),
                hasDescendant(withText(startTime))
            )
        )

        // Click on already running
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))
        )
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
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1))
        )
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRunningRecordItem),
                    hasDescendant(withText(name1)),
                    hasDescendant(withText(startTime))
                )
            )
        }
        checkViewDoesNotExist(
            allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name2)))
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name3)))
        )

        // Records added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name3), isCompletelyDisplayed()))

        // Click another
        NavUtils.openRunningRecordsScreen()
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))
        )
        startTime = System.currentTimeMillis()
            .let { timeMapper.formatTime(it, true) }
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRunningRecordItem),
                    hasDescendant(withText(name2)),
                    hasDescendant(withText(startTime))
                )
            )
        }
        checkViewDoesNotExist(
            allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name1)))
        )

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
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))
        )
        val newStartTime = System.currentTimeMillis()
            .let { timeMapper.formatTime(it, true) }
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRunningRecordItem),
                    hasDescendant(withText(name2)),
                    hasDescendant(withText(startTime))
                )
            )
        }
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name3)),
                hasDescendant(withText(newStartTime))
            )
        )

        // No new records added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name3), isCompletelyDisplayed()))
    }

    @Test
    fun cardSizeTest() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Test3"

        // Add activities
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)

        tryAction { check(name1, name2) { matcher -> isCompletelyLeftOf(matcher) } }
        check(name2, name3) { matcher -> isCompletelyLeftOf(matcher) }

        // Open settings
        NavUtils.openSettingsScreen()
        NavUtils.openCardSizeScreen()
        Thread.sleep(1000)

        // Check order
        check(name1, name2) { matcher -> isCompletelyLeftOf(matcher) }
        check(name2, name3) { matcher -> isCompletelyLeftOf(matcher) }

        // Change setting
        clickOnViewWithText("6")
        clickOnViewWithText("5")
        clickOnViewWithText("4")
        clickOnViewWithText("3")
        clickOnViewWithText("2")
        clickOnViewWithText("1")

        // Check new order
        check(name1, name2) { matcher -> isCompletelyAbove(matcher) }
        check(name2, name3) { matcher -> isCompletelyAbove(matcher) }

        // Check order on main
        pressBack()
        NavUtils.openRunningRecordsScreen()
        check(name1, name2) { matcher -> isCompletelyAbove(matcher) }
        check(name2, name3) { matcher -> isCompletelyAbove(matcher) }

        // Change back
        NavUtils.openSettingsScreen()
        NavUtils.openCardSizeScreen()
        Thread.sleep(1000)
        check(name1, name2) { matcher -> isCompletelyAbove(matcher) }
        check(name2, name3) { matcher -> isCompletelyAbove(matcher) }
        clickOnViewWithText(R.string.card_size_default)

        // Check order
        check(name1, name2) { matcher -> isCompletelyLeftOf(matcher) }
        check(name2, name3) { matcher -> isCompletelyLeftOf(matcher) }
        pressBack()
        NavUtils.openRunningRecordsScreen()
        check(name1, name2) { matcher -> isCompletelyLeftOf(matcher) }
        check(name2, name3) { matcher -> isCompletelyLeftOf(matcher) }
    }

    @Test
    fun cardOrderByName() {
        val name1 = "Test1"
        val name2 = "Test2"
        val color1 = ColorMapper.getAvailableColors().first()
        val color2 = ColorMapper.getAvailableColors().last()

        // Add activities
        testUtils.addActivity(name1, color2)
        testUtils.addActivity(name2, color1)

        // Check order
        tryAction { check(name1, name2) { matcher -> isCompletelyLeftOf(matcher) } }

        // Check settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.spinnerSettingsRecordTypeSort)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(R.id.tvSettingsRecordTypeSortValue), withText(R.string.settings_sort_by_name))
        )
    }

    @Test
    fun cardOrderByColor() {
        val name1 = "Test1"
        val name2 = "Test2"
        val color1 = ColorMapper.getAvailableColors().first()
        val color2 = ColorMapper.getAvailableColors().last()

        // Add activities
        testUtils.addActivity(name1, color2)
        testUtils.addActivity(name2, color1)

        // Change settings
        NavUtils.openSettingsScreen()
        clickOnSpinnerWithId(R.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(R.string.settings_sort_by_color)
        checkViewIsDisplayed(
            allOf(withId(R.id.tvSettingsRecordTypeSortValue), withText(R.string.settings_sort_by_color))
        )

        // Check new order
        NavUtils.openRunningRecordsScreen()
        check(name2, name1) { matcher -> isCompletelyLeftOf(matcher) }
    }

    @Test
    fun cardOrderManual() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Test3"

        // Add activities
        testUtils.addActivity(name3)
        testUtils.addActivity(name2)
        testUtils.addActivity(name1)

        // Change settings
        NavUtils.openSettingsScreen()
        clickOnSpinnerWithId(R.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(R.string.settings_sort_manually)
        Thread.sleep(1000)

        // Check old order
        check(name1, name2) { matcher -> isCompletelyLeftOf(matcher) }
        check(name2, name3) { matcher -> isCompletelyLeftOf(matcher) }

        // Drag
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .perform(drag(Direction.LEFT, 300))

        // Check new order
        pressBack()
        checkViewIsDisplayed(
            allOf(withId(R.id.tvSettingsRecordTypeSortValue), withText(R.string.settings_sort_manually))
        )
        NavUtils.openRunningRecordsScreen()
        check(name2, name1) { matcher -> isCompletelyLeftOf(matcher) }
        check(name1, name3) { matcher -> isCompletelyLeftOf(matcher) }

        // Change order
        NavUtils.openSettingsScreen()
        onView(withId(R.id.btnCardOrderManual)).perform(nestedScrollTo())
        clickOnViewWithId(R.id.btnCardOrderManual)
        check(name2, name1) { matcher -> isCompletelyLeftOf(matcher) }
        check(name1, name3) { matcher -> isCompletelyLeftOf(matcher) }
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .perform(drag(Direction.RIGHT, 300))

        // Check new order
        pressBack()
        NavUtils.openRunningRecordsScreen()
        check(name2, name3) { matcher -> isCompletelyLeftOf(matcher) }
        check(name3, name1) { matcher -> isCompletelyLeftOf(matcher) }
    }

    @Test
    fun cardOrderManual2() {
        val name = "Test"

        // Add activities
        (1..15).forEach {
            testUtils.addActivity("$name$it")
        }

        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openCardSizeScreen()
        Thread.sleep(1000)
        clickOnViewWithText("4")
        pressBack()
        clickOnSpinnerWithId(R.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(R.string.settings_sort_by_color)
        clickOnSpinnerWithId(R.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(R.string.settings_sort_manually)
        Thread.sleep(1000)

        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        // Drag
        (1..15).forEach {
            onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText("$name$it")))
                .perform(
                    drag(Direction.RIGHT, screenWidth),
                    drag(Direction.DOWN, screenHeight),
                )
        }

        // Check order in settings
        checkManualOrder(name)

        // Check order on main
        pressBack()
        NavUtils.openRunningRecordsScreen()
        checkManualOrder(name)
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
        repeat(10) { clickOnViewWithId(R.id.tvNumberKeyboard9) }
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
    fun militaryTime() {
        // Check settings
        NavUtils.openSettingsScreen()
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
                ViewMatchers.hasSibling(withText("1")),
                isCompletelyDisplayed()
            )
        )

        // Check range titles
        var titlePrev = timeMapper.toWeekTitle(
            weeksFromToday = -1,
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
                ViewMatchers.hasSibling(withText("0")),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkViewIsDisplayed(
            allOf(
                withPluralText(R.plurals.statistics_detail_times_tracked, 1),
                ViewMatchers.hasSibling(withText("1")),
                isCompletelyDisplayed()
            )
        )

        // Check range titles
        titlePrev = timeMapper.toWeekTitle(
            weeksFromToday = -1,
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
    fun showRecordTagSelection() {
        val name = "TypeName"
        val tag = "TagName"
        val fullName = "$name - $tag"

        // Add data
        testUtils.addActivity(name)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowRecordTagSelection))
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).check(matches(isChecked()))

        // No tags - started right away
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }

        // Add tag
        testUtils.addRecordTag(name, tag)

        // Has a tag - show dialog
        clickOnViewWithText(name)
        checkViewIsDisplayed(withText(R.string.change_record_untagged))
        checkViewIsDisplayed(withText(tag))
        pressBack()
        checkViewDoesNotExist(isDescendantOfA(withId(R.id.viewRunningRecordItem)))

        // Start untagged
        clickOnViewWithText(name)
        clickOnView(withText(R.string.change_record_untagged))
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }

        // Start tagged
        clickOnViewWithText(name)
        clickOnView(withText(tag))
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
    fun csvExportSettings() {
        NavUtils.openSettingsScreen()
        onView(withId(R.id.layoutSettingsExportCsv)).perform(nestedScrollTo(), click())

        // View is set up
        val currentTime = System.currentTimeMillis()
        var timeStarted = timeMapper.formatDateTime(currentTime - TimeUnit.DAYS.toMillis(7), true)
        var timeEnded = timeMapper.formatDateTime(currentTime, true)
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
            .let { timeMapper.formatDateTime(it, true) }

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
            .let { timeMapper.formatDateTime(it, true) }

        checkViewIsDisplayed(allOf(withId(R.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))
    }

    private fun clearDuration() {
        repeat(6) { clickOnViewWithId(R.id.ivDurationPickerDelete) }
    }

    private fun check(first: String, second: String, matcher: (Matcher<View>) -> ViewAssertion) {
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(first))).check(
            matcher(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(second)))
        )
    }

    private fun checkManualOrder(name: String) {
        check(name + 2, name + 1) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 3, name + 2) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 4, name + 3) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }

        check(name + 5, name + 1) { matcher ->
            isCompletelyBelow(matcher)
            isLeftAlignedWith(matcher)
        }
        check(name + 6, name + 5) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 7, name + 6) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 8, name + 7) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }

        check(name + 9, name + 5) { matcher ->
            isCompletelyBelow(matcher)
            isLeftAlignedWith(matcher)
        }
        check(name + 10, name + 9) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 11, name + 10) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 12, name + 11) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }

        check(name + 13, name + 9) { matcher ->
            isCompletelyBelow(matcher)
        }
        check(name + 14, name + 13) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 15, name + 14) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
    }
}
