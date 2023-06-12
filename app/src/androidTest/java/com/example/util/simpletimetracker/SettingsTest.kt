package com.example.util.simpletimetracker

import android.widget.DatePicker
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
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomTimePicker
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
import com.example.util.simpletimetracker.utils.getMillis
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.recyclerItemCount
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import com.example.util.simpletimetracker.utils.withPluralText
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_records.R as recordsR
import com.example.util.simpletimetracker.feature_settings.R as settingsR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR
import com.example.util.simpletimetracker.feature_statistics_detail.R as statisticsDetailR

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
        val before = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        testUtils.addRecord(name, timeStarted = before, timeEnded = before)

        // Untracked is not shown
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(
            allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())
        )

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(settingsR.id.checkboxSettingsShowUntracked)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsShowUntracked)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowUntracked))
        onView(withId(settingsR.id.checkboxSettingsShowUntracked)).check(matches(isChecked()))

        // Untracked is shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))

        // Add record
        NavUtils.addRecord(name)
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.checkboxSettingsShowUntracked)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsShowUntracked)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowUntracked))
        onView(withId(settingsR.id.checkboxSettingsShowUntracked)).check(matches(isNotChecked()))

        // Untracked is not shown
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(
            allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
    }

    @Test
    fun ignoreShortUntracked() {
        fun getTime(
            hour: Int,
            minutes: Int,
            seconds: Int = 0,
        ): Long {
            return calendar.apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DATE, -1)
                setToStartOfDay()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minutes)
                set(Calendar.SECOND, seconds)
            }.timeInMillis
        }

        fun checkItemCount(
            count: Int,
        ) {
            onView(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
                .check(recyclerItemCount(count))
        }

        fun checkRecordDuration(
            interval: Long,
            displayed: Boolean,
        ) {
            val duration = timeMapper.formatInterval(
                interval = interval,
                forceSeconds = true,
                useProportionalMinutes = false
            )
            val matcher = allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(allOf(withId(changeRecordTypeR.id.tvRecordItemDuration), withText(duration))),
                isCompletelyDisplayed()
            )
            if (displayed) {
                checkViewIsDisplayed(matcher)
            } else {
                checkViewDoesNotExist(matcher)
            }
        }

        // Add data
        runBlocking {
            prefsInteractor.setShowSeconds(true)
            prefsInteractor.setShowUntrackedInRecords(true)
        }
        val name = "name"
        testUtils.addActivity(name)
        val before = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)
        testUtils.addRecord(name, timeStarted = before, timeEnded = before)

        // Add records
        testUtils.addRecord(name, timeStarted = getTime(0, 0), timeEnded = getTime(1, 0))
        testUtils.addRecord(name, timeStarted = getTime(1, 30), timeEnded = getTime(2, 0))
        testUtils.addRecord(name, timeStarted = getTime(2, 1), timeEnded = getTime(3, 0))
        testUtils.addRecord(name, timeStarted = getTime(3, 0, seconds = 1), timeEnded = getTime(4, 0))

        // Check disabled
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(settingsR.id.groupSettingsIgnoreShortUntracked)).perform(nestedScrollTo())
        clickOnViewWithId(settingsR.id.groupSettingsIgnoreShortUntracked)
        clickOnViewWithText(coreR.string.duration_dialog_disable)
        checkViewIsDisplayed(
            allOf(
                withId(settingsR.id.tvSettingsIgnoreShortUntrackedTime),
                withText(coreR.string.settings_inactivity_reminder_disabled)
            )
        )

        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        checkItemCount(9)
        checkRecordDuration(TimeUnit.HOURS.toMillis(20), displayed = true)
        checkRecordDuration(TimeUnit.MINUTES.toMillis(30), displayed = true)
        checkRecordDuration(TimeUnit.MINUTES.toMillis(1), displayed = true)
        checkRecordDuration(TimeUnit.SECONDS.toMillis(1), displayed = true)

        // Check 30 minutes
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.groupSettingsIgnoreShortUntracked)).perform(nestedScrollTo())
        clickOnViewWithId(settingsR.id.groupSettingsIgnoreShortUntracked)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard3)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)

        NavUtils.openRecordsScreen()
        checkItemCount(6)
        checkRecordDuration(TimeUnit.HOURS.toMillis(20), displayed = true)
        checkRecordDuration(TimeUnit.MINUTES.toMillis(30), displayed = false)
        checkRecordDuration(TimeUnit.MINUTES.toMillis(1), displayed = false)
        checkRecordDuration(TimeUnit.SECONDS.toMillis(1), displayed = false)

        // Check 1 minutes
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.groupSettingsIgnoreShortUntracked)).perform(nestedScrollTo())
        clickOnViewWithId(settingsR.id.groupSettingsIgnoreShortUntracked)
        repeat(4) { clickOnViewWithId(dialogsR.id.ivDurationPickerDelete) }
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)

        NavUtils.openRecordsScreen()
        checkItemCount(7)
        checkRecordDuration(TimeUnit.HOURS.toMillis(20), displayed = true)
        checkRecordDuration(TimeUnit.MINUTES.toMillis(30), displayed = true)
        checkRecordDuration(TimeUnit.MINUTES.toMillis(1), displayed = false)
        checkRecordDuration(TimeUnit.SECONDS.toMillis(1), displayed = false)
    }

    @Test
    fun untrackedRange() {
        val name = "name"
        val startOfDay = calendar.apply { setToStartOfDay() }.getMillis(0, 0)

        // Add data
        runBlocking {
            prefsInteractor.setShowUntrackedInRecords(true)
        }
        testUtils.addActivity(name)
        val before = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)
        testUtils.addRecord(name, timeStarted = before, timeEnded = before)

        // Check disabled
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        checkRecord(
            nameResId = coreR.string.untracked_time_name,
            timeStart = startOfDay.toTimePreview(),
            timeEnd = startOfDay.toTimePreview(),
        )

        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(settingsR.id.checkboxSettingsShowRecordsCalendar)).perform(nestedScrollTo())
        tryAction { onView(withId(settingsR.id.checkboxSettingsUntrackedRange)).check(matches(isNotChecked())) }
        checkViewIsNotDisplayed(withId(settingsR.id.tvSettingsUntrackedRangeStart))
        checkViewIsNotDisplayed(withId(settingsR.id.tvSettingsUntrackedRangeEnd))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsUntrackedRange))
        tryAction { onView(withId(settingsR.id.checkboxSettingsUntrackedRange)).check(matches(isChecked())) }
        checkViewIsDisplayed(withId(settingsR.id.tvSettingsUntrackedRangeStart))
        checkViewIsDisplayed(withId(settingsR.id.tvSettingsUntrackedRangeEnd))

        // Check range
        var startPreview = (startOfDay + TimeUnit.HOURS.toMillis(8)).toTimePreview()
        clickOnViewWithId(settingsR.id.tvSettingsUntrackedRangeStart)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(8, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(
            allOf(
                withId(settingsR.id.tvSettingsUntrackedRangeStart),
                withText(startPreview),
            )
        )
        var endPreview = (startOfDay + TimeUnit.HOURS.toMillis(17)).toTimePreview()
        clickOnViewWithId(settingsR.id.tvSettingsUntrackedRangeEnd)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(17, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(
            allOf(
                withId(settingsR.id.tvSettingsUntrackedRangeEnd),
                withText(endPreview),
            )
        )

        NavUtils.openRecordsScreen()
        checkRecord(
            nameResId = coreR.string.untracked_time_name,
            timeStart = startPreview,
            timeEnd = endPreview,
        )

        // Check other range
        NavUtils.openSettingsScreen()
        startPreview = (startOfDay + TimeUnit.HOURS.toMillis(17)).toTimePreview()
        clickOnViewWithId(settingsR.id.tvSettingsUntrackedRangeStart)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(17, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(
            allOf(
                withId(settingsR.id.tvSettingsUntrackedRangeStart),
                withText(startPreview),
            )
        )
        endPreview = (startOfDay + TimeUnit.HOURS.toMillis(8)).toTimePreview()
        clickOnViewWithId(settingsR.id.tvSettingsUntrackedRangeEnd)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(8, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(
            allOf(
                withId(settingsR.id.tvSettingsUntrackedRangeEnd),
                withText(endPreview),
            )
        )

        NavUtils.openRecordsScreen()
        checkRecord(
            nameResId = coreR.string.untracked_time_name,
            timeStart = startOfDay.toTimePreview(),
            timeEnd = endPreview,
        )
        checkRecord(
            nameResId = coreR.string.untracked_time_name,
            timeStart = startPreview,
            timeEnd = startOfDay.toTimePreview(),
        )
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
        tryAction {
            checkViewIsDisplayed(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name2))))
        }
        checkViewIsDisplayed(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name3))))

        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(
            allOf(withText(name1), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withText(name2), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withText(name3), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed())
        )

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.checkboxSettingsAllowMultitasking)).perform(nestedScrollTo())
        tryAction { onView(withId(settingsR.id.checkboxSettingsAllowMultitasking)).check(matches(isChecked())) }
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsAllowMultitasking))
        onView(withId(settingsR.id.checkboxSettingsAllowMultitasking)).check(matches(isNotChecked()))

        // Click on one not running
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name1)))
        tryAction {
            checkViewIsDisplayed(
                allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name1)), isCompletelyDisplayed())
            )
        }
        checkViewDoesNotExist(
            allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name2)), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name3)), isCompletelyDisplayed())
        )

        // Records added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(withText(name2), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withText(name3), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed())
        )

        // Click another
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name2)))
        tryAction {
            checkViewIsDisplayed(
                allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name2)), isCompletelyDisplayed())
            )
        }
        checkViewDoesNotExist(
            allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name1)), isCompletelyDisplayed())
        )

        // Record added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(withText(name1), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed())
        )

        // Change setting back
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.checkboxSettingsAllowMultitasking)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsAllowMultitasking)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsAllowMultitasking))
        onView(withId(settingsR.id.checkboxSettingsAllowMultitasking)).check(matches(isChecked()))

        // Start another timer
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name3)))
        tryAction {
            checkViewIsDisplayed(
                allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name2)), isCompletelyDisplayed())
            )
        }
        checkViewIsDisplayed(
            allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name3)), isCompletelyDisplayed())
        )

        // No new records added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(withText(name1), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withText(name2), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withText(name3), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed())
        )
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
        NavUtils.openSettingsNotifications()
        onView(withId(settingsR.id.checkboxSettingsShowNotifications)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsShowNotifications)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowNotifications))
        onView(withId(settingsR.id.checkboxSettingsShowNotifications)).check(matches(isChecked()))

        // Stop first timer
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name1))))

        // Start another timer
        clickOnViewWithText(name2)

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.checkboxSettingsShowNotifications)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsShowNotifications)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowNotifications))
        onView(withId(settingsR.id.checkboxSettingsShowNotifications)).check(matches(isNotChecked()))
    }

    @Test
    fun showNotificationsControls() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsNotifications()

        // Check settings
        onView(withId(settingsR.id.checkboxSettingsShowNotifications)).perform(nestedScrollTo())
        checkViewIsNotDisplayed(withText(coreR.string.settings_show_notifications_controls))
        checkViewIsNotDisplayed(withId(settingsR.id.checkboxSettingsShowNotificationsControls))

        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowNotifications))
        onView(withId(settingsR.id.checkboxSettingsShowNotificationsControls)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(coreR.string.settings_show_notifications_controls))
        onView(withId(settingsR.id.checkboxSettingsShowNotificationsControls)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowNotificationsControls))
        onView(withId(settingsR.id.checkboxSettingsShowNotificationsControls)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowNotificationsControls))
        onView(withId(settingsR.id.checkboxSettingsShowNotificationsControls)).check(matches(isChecked()))

        // Change settings
        onView(withId(settingsR.id.checkboxSettingsShowNotifications)).perform(nestedScrollTo())
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowNotifications))
        checkViewIsNotDisplayed(withText(coreR.string.settings_show_notifications_controls))
        checkViewIsNotDisplayed(withId(settingsR.id.checkboxSettingsShowNotificationsControls))
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
        onView(withId(settingsR.id.checkboxSettingsDarkMode)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsDarkMode)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsDarkMode))
        onView(withId(settingsR.id.checkboxSettingsDarkMode)).check(matches(isChecked()))

        // Check screens
        NavUtils.openRunningRecordsScreen()
        NavUtils.openRecordsScreen()
        NavUtils.openStatisticsScreen()

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.checkboxSettingsDarkMode)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsDarkMode)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsDarkMode))
        onView(withId(settingsR.id.checkboxSettingsDarkMode)).check(matches(isNotChecked()))

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
        NavUtils.openSettingsNotifications()
        onView(withId(settingsR.id.groupSettingsInactivityReminder)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(
                withId(settingsR.id.tvSettingsInactivityReminderTime),
                withText(coreR.string.settings_inactivity_reminder_disabled)
            )
        )
        checkViewIsNotDisplayed(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent))

        // 1s
        clickOnViewWithId(settingsR.id.groupSettingsInactivityReminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$secondString"))

        // Check recurrent
        onView(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent))
        onView(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent))
        onView(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent)).check(matches(isChecked()))

        // 1m
        clickOnViewWithId(settingsR.id.groupSettingsInactivityReminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(
            allOf(withText("1$minuteString"), withId(settingsR.id.tvSettingsInactivityReminderTime))
        )
        onView(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent))

        // 1h
        clickOnViewWithId(settingsR.id.groupSettingsInactivityReminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString"))
        onView(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent))

        // 1m 1s
        clickOnViewWithId(settingsR.id.groupSettingsInactivityReminder)
        clickOnViewWithId(dialogsR.id.ivDurationPickerDelete)
        clickOnViewWithId(dialogsR.id.ivDurationPickerDelete)
        clickOnViewWithId(dialogsR.id.ivDurationPickerDelete)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$minuteString 01$secondString"))
        onView(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent))

        // 1h 1m 1s
        clickOnViewWithId(settingsR.id.groupSettingsInactivityReminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString 01$minuteString 01$secondString"))
        onView(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent))

        // 1h 30m
        clickOnViewWithId(settingsR.id.groupSettingsInactivityReminder)
        clearDuration()
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard9)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString 30$minuteString"))
        onView(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent))

        // 99h 99m 99s
        clickOnViewWithId(settingsR.id.groupSettingsInactivityReminder)
        repeat(10) { clickOnViewWithId(dialogsR.id.ivDurationPickerDelete) }
        repeat(6) { clickOnViewWithId(dialogsR.id.tvNumberKeyboard9) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("100$hourString 40$minuteString 39$secondString"))
        onView(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent))

        // Disable
        clickOnViewWithId(settingsR.id.groupSettingsInactivityReminder)
        clickOnViewWithText(coreR.string.duration_dialog_disable)
        checkViewIsDisplayed(
            allOf(
                withId(settingsR.id.tvSettingsInactivityReminderTime),
                withText(coreR.string.settings_inactivity_reminder_disabled)
            )
        )
        checkViewIsNotDisplayed(withId(settingsR.id.checkboxSettingsInactivityReminderRecurrent))
    }

    @Test
    fun activityReminder() {
        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsNotifications()
        onView(withId(settingsR.id.groupSettingsActivityReminder)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(
                withId(settingsR.id.tvSettingsActivityReminderTime),
                withText(coreR.string.settings_inactivity_reminder_disabled)
            )
        )
        checkViewIsNotDisplayed(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent))

        // 1s
        clickOnViewWithId(settingsR.id.groupSettingsActivityReminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$secondString"))

        // Check recurrent
        onView(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent))
        onView(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent))
        onView(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent)).check(matches(isChecked()))

        // 1m
        clickOnViewWithId(settingsR.id.groupSettingsActivityReminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(
            allOf(withText("1$minuteString"), withId(settingsR.id.tvSettingsActivityReminderTime))
        )
        onView(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent))

        // 1h
        clickOnViewWithId(settingsR.id.groupSettingsActivityReminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString"))
        onView(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent))

        // 1m 1s
        clickOnViewWithId(settingsR.id.groupSettingsActivityReminder)
        clickOnViewWithId(dialogsR.id.ivDurationPickerDelete)
        clickOnViewWithId(dialogsR.id.ivDurationPickerDelete)
        clickOnViewWithId(dialogsR.id.ivDurationPickerDelete)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$minuteString 01$secondString"))
        onView(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent))

        // 1h 1m 1s
        clickOnViewWithId(settingsR.id.groupSettingsActivityReminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString 01$minuteString 01$secondString"))
        onView(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent))

        // 1h 30m
        clickOnViewWithId(settingsR.id.groupSettingsActivityReminder)
        clearDuration()
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard9)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString 30$minuteString"))
        onView(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent))

        // 99h 99m 99s
        clickOnViewWithId(settingsR.id.groupSettingsActivityReminder)
        repeat(10) { clickOnViewWithId(dialogsR.id.ivDurationPickerDelete) }
        repeat(6) { clickOnViewWithId(dialogsR.id.tvNumberKeyboard9) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("100$hourString 40$minuteString 39$secondString"))
        onView(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent)).perform(nestedScrollTo())
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent))

        // Disable
        clickOnViewWithId(settingsR.id.groupSettingsActivityReminder)
        clickOnViewWithText(coreR.string.duration_dialog_disable)
        checkViewIsDisplayed(
            allOf(
                withId(settingsR.id.tvSettingsActivityReminderTime),
                withText(coreR.string.settings_inactivity_reminder_disabled)
            )
        )
        checkViewIsNotDisplayed(withId(settingsR.id.checkboxSettingsActivityReminderRecurrent))
    }

    @Test
    fun ignoreShortRecords() {
        val name = "Test"

        // Add data
        testUtils.addActivity(name)

        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(settingsR.id.groupSettingsIgnoreShortRecords)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(
                withId(settingsR.id.tvSettingsIgnoreShortRecordsTime),
                withText(coreR.string.settings_inactivity_reminder_disabled)
            )
        )

        clickOnViewWithId(settingsR.id.groupSettingsIgnoreShortRecords)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard3)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("3$secondString"))

        // Check record ignored
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))

        // Disable
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.groupSettingsIgnoreShortRecords)).perform(nestedScrollTo())
        clickOnViewWithId(settingsR.id.groupSettingsIgnoreShortRecords)
        clickOnViewWithText(coreR.string.duration_dialog_disable)
        checkViewIsDisplayed(
            allOf(
                withId(settingsR.id.tvSettingsIgnoreShortRecordsTime),
                withText(coreR.string.settings_inactivity_reminder_disabled)
            )
        )

        // Check record not ignored
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
    }

    @Test
    fun militaryTime() {
        // Check settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(settingsR.id.tvSettingsUseMilitaryTimeHint)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsUseMilitaryTime)).check(matches(isChecked()))
        checkViewIsDisplayed(allOf(withId(settingsR.id.tvSettingsUseMilitaryTimeHint), withText("13:00")))

        // Change settings
        clickOnViewWithId(settingsR.id.checkboxSettingsUseMilitaryTime)
        onView(withId(settingsR.id.checkboxSettingsUseMilitaryTime)).check(matches(isNotChecked()))
        checkViewIsDisplayed(allOf(withId(settingsR.id.tvSettingsUseMilitaryTimeHint), withSubstring("1:00")))

        // Change settings
        clickOnViewWithId(settingsR.id.checkboxSettingsUseMilitaryTime)
        onView(withId(settingsR.id.checkboxSettingsUseMilitaryTime)).check(matches(isChecked()))
        checkViewIsDisplayed(allOf(withId(settingsR.id.tvSettingsUseMilitaryTimeHint), withText("13:00")))
    }

    @Test
    fun proportionalMinutes() {
        val name = "Test"

        fun checkView(id: Int, text: String) {
            checkViewIsDisplayed(allOf(withId(id), hasDescendant(withText(text)), isCompletelyDisplayed()))
        }

        fun checkFormat(timeString: String) {
            NavUtils.openRecordsScreen()
            checkView(baseR.id.viewRecordItem, timeString)
            NavUtils.openStatisticsScreen()
            checkView(baseR.id.viewStatisticsItem, timeString)
            tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }
            checkView(statisticsDetailR.id.cardStatisticsDetailTotal, timeString)
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
        onView(withId(settingsR.id.tvSettingsUseProportionalMinutesHint)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsUseProportionalMinutes)).check(matches(isNotChecked()))
        checkViewIsDisplayed(
            allOf(withId(settingsR.id.tvSettingsUseProportionalMinutesHint), withText(timeFormat1))
        )

        // Change settings
        clickOnViewWithId(settingsR.id.checkboxSettingsUseProportionalMinutes)
        onView(withId(settingsR.id.checkboxSettingsUseProportionalMinutes)).check(matches(isChecked()))
        checkViewIsDisplayed(
            allOf(withId(settingsR.id.tvSettingsUseProportionalMinutesHint), withSubstring(timeFormat2))
        )

        // Check format after setting change
        checkFormat(timeFormat2)

        // Change settings back
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.tvSettingsUseProportionalMinutesHint)).perform(nestedScrollTo())
        clickOnViewWithId(settingsR.id.checkboxSettingsUseProportionalMinutes)
        onView(withId(settingsR.id.checkboxSettingsUseProportionalMinutes)).check(matches(isNotChecked()))
        checkViewIsDisplayed(
            allOf(withId(settingsR.id.tvSettingsUseProportionalMinutesHint), withText(timeFormat1))
        )

        // Check format again
        checkFormat(timeFormat1)
    }

    @Test
    fun keepScreenOn() {
        // Check settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(settingsR.id.tvSettingsKeepScreenOn)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsKeepScreenOn)).check(matches(isNotChecked()))

        // Change settings
        clickOnViewWithId(settingsR.id.checkboxSettingsKeepScreenOn)
        onView(withId(settingsR.id.checkboxSettingsKeepScreenOn)).check(matches(isChecked()))

        // Change settings
        clickOnViewWithId(settingsR.id.checkboxSettingsKeepScreenOn)
        onView(withId(settingsR.id.checkboxSettingsKeepScreenOn)).check(matches(isNotChecked()))
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
        clickOnSpinnerWithId(settingsR.id.spinnerSettingsFirstDayOfWeek)
        if (isTodaySunday) {
            clickOnViewWithText(coreR.string.day_of_week_monday)
        } else {
            clickOnViewWithText(coreR.string.day_of_week_sunday)
        }

        // Check statistics
        NavUtils.openStatisticsScreen()
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_week)
        clickOnView(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )

        // Check detailed statistics
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_week)
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, 1),
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
        longClickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))
        pressBack()
        longClickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))
        longClickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)

        // Change setting
        NavUtils.openSettingsScreen()
        clickOnSpinnerWithId(settingsR.id.spinnerSettingsFirstDayOfWeek)
        if (isTodaySunday) {
            clickOnViewWithText(coreR.string.day_of_week_sunday)
        } else {
            clickOnViewWithText(coreR.string.day_of_week_monday)
        }

        // Check statistics
        NavUtils.openStatisticsScreen()
        checkViewDoesNotExist(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        clickOnView(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )

        // Check detailed statistics
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_week)
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, 0),
                hasSibling(withText("0")),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, 1),
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
        longClickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))
        pressBack()
        longClickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))
        longClickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
    }

    @Test
    fun startOfDay() {
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
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = startOfDayPreview)
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
        checkRecord(
            nameResId = coreR.string.untracked_time_name, timeStart = timeEndedPreview, timeEnd = startOfDayPreview,
        )
        checkRecord(name = name, timeStart = startOfDayPreview, timeEnd = timeEndedPreview)

        // Check statistics
        NavUtils.openStatisticsScreen()
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        checkStatisticsItem(name = name, hours = 2)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
        checkStatisticsItem(nameResId = coreR.string.untracked_time_name, hours = 22)
        checkStatisticsItem(name = name, hours = 2)

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnView(allOf(withId(statisticsDetailR.id.btnStatisticsDetailToday), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Check setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(settingsR.id.tvSettingsStartOfDayTime)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(settingsR.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsNotDisplayed(withId(settingsR.id.btnSettingsStartOfDaySign))

        // Change setting to +1
        clickOnView(withId(settingsR.id.groupSettingsStartOfDay))
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(1, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        startOfDayTimeStamp = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        // Check new setting
        checkViewIsDisplayed(allOf(withId(settingsR.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsDisplayed(
            allOf(withId(settingsR.id.btnSettingsStartOfDaySign), hasDescendant(withText(coreR.string.plus_sign)))
        )

        // Check records
        NavUtils.openRecordsScreen()
        longClickOnViewWithId(recordsR.id.btnRecordsContainerToday)
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = startOfDayPreview)
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
        checkRecord(
            nameResId = coreR.string.untracked_time_name, timeStart = timeEndedPreview, timeEnd = startOfDayPreview,
        )
        checkRecord(name = name, timeStart = startOfDayPreview, timeEnd = timeEndedPreview)

        // Check statistics
        NavUtils.openStatisticsScreen()
        longClickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        checkStatisticsItem(name = name, hours = 3)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
        checkStatisticsItem(nameResId = coreR.string.untracked_time_name, hours = 23)
        checkStatisticsItem(name = name, hours = 1)

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnView(allOf(withId(statisticsDetailR.id.btnStatisticsDetailToday), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Change setting to -1
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.btnSettingsStartOfDaySign)).perform(nestedScrollTo(), click())

        // Check new setting
        checkViewIsDisplayed(allOf(withId(settingsR.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsDisplayed(
            allOf(withId(settingsR.id.btnSettingsStartOfDaySign), hasDescendant(withText(coreR.string.minus_sign)))
        )

        startOfDayTimeStamp = calendar.timeInMillis - TimeUnit.HOURS.toMillis(1)
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        // Check records
        NavUtils.openRecordsScreen()
        longClickOnViewWithId(recordsR.id.btnRecordsContainerToday)
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = startOfDayPreview)
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
        checkRecord(
            nameResId = coreR.string.untracked_time_name, timeStart = timeEndedPreview, timeEnd = startOfDayPreview,
        )
        checkRecord(name = name, timeStart = startOfDayPreview, timeEnd = timeEndedPreview)

        // Check statistics
        NavUtils.openStatisticsScreen()
        longClickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        checkStatisticsItem(name = name, hours = 1)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
        checkStatisticsItem(nameResId = coreR.string.untracked_time_name, hours = 21)
        checkStatisticsItem(name = name, hours = 3)

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnView(allOf(withId(statisticsDetailR.id.btnStatisticsDetailToday), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Change setting to +2, record will be shifted out from one day
        startOfDayTimeStamp = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2)
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.groupSettingsStartOfDay)).perform(nestedScrollTo(), click())
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(2, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(allOf(withId(settingsR.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsDisplayed(
            allOf(withId(settingsR.id.btnSettingsStartOfDaySign), hasDescendant(withText(coreR.string.minus_sign)))
        )
        onView(withId(settingsR.id.btnSettingsStartOfDaySign)).perform(nestedScrollTo(), click())
        checkViewIsDisplayed(allOf(withId(settingsR.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))

        // Check records
        NavUtils.openRecordsScreen()
        longClickOnViewWithId(recordsR.id.btnRecordsContainerToday)
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = timeEndedPreview)
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
        checkRecord(
            nameResId = coreR.string.untracked_time_name, timeStart = startOfDayPreview, timeEnd = startOfDayPreview,
        )

        // Check statistics
        NavUtils.openStatisticsScreen()
        longClickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        checkStatisticsItem(name = name, hours = 4)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
        checkStatisticsItem(nameResId = coreR.string.untracked_time_name, hours = 24)

        // Check detailed statistics
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnView(allOf(withId(statisticsDetailR.id.btnStatisticsDetailToday), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Change setting to 0
        startOfDayTimeStamp = calendar.timeInMillis
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.groupSettingsStartOfDay)).perform(nestedScrollTo(), click())
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(0, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(allOf(withId(settingsR.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsNotDisplayed(withId(settingsR.id.btnSettingsStartOfDaySign))
    }

    @Test
    fun showRecordTagSelection() {
        val name = "TypeName"
        val tag = "TagName"
        val tagGeneral = "TagGeneral"
        val fullName = "$name - $tag"

        // Add data
        testUtils.addActivity(name)
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)))) }

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection))
        onView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection)).check(matches(isChecked()))

        // No tags - started right away
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }

        // Add tag
        testUtils.addRecordTag(tag, name)
        testUtils.addRecordTag(tagGeneral)

        // Has a tag - show dialog
        clickOnViewWithText(name)
        tryAction { checkViewIsDisplayed(withText(coreR.string.change_record_untagged)) }
        checkViewIsDisplayed(withText(tag))
        pressBack()

        // Start untagged
        clickOnViewWithText(name)
        tryAction { clickOnView(withText(tag)) }
        clickOnView(withText(coreR.string.change_record_untagged))
        clickOnViewWithText(coreR.string.duration_dialog_save)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }

        // Start tagged
        clickOnViewWithText(name)
        tryAction { clickOnView(withText(tag)) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(fullName))) }

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection))
        onView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection)).check(matches(isNotChecked()))

        // Start with tags - no dialog
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }
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
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        checkViewIsNotDisplayed(withText(coreR.string.settings_show_record_tag_close_hint))
        checkViewIsNotDisplayed(withId(settingsR.id.checkboxSettingsRecordTagSelectionClose))

        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection))
        onView(withId(settingsR.id.checkboxSettingsRecordTagSelectionClose)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(coreR.string.settings_show_record_tag_close_hint))
        onView(withId(settingsR.id.checkboxSettingsRecordTagSelectionClose)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsRecordTagSelectionClose))
        onView(withId(settingsR.id.checkboxSettingsRecordTagSelectionClose)).check(matches(isChecked()))

        // No tags - started right away
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }

        // Add tag
        testUtils.addRecordTag(tag, name)
        testUtils.addRecordTag(tagGeneral)

        // Start after one tag selected
        clickOnViewWithText(name)
        clickOnView(withText(tag))
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(fullName))) }

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.checkboxSettingsRecordTagSelectionClose)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsRecordTagSelectionClose)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsRecordTagSelectionClose))
        onView(withId(settingsR.id.checkboxSettingsRecordTagSelectionClose)).check(matches(isNotChecked()))

        // Start with several tags
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        clickOnView(withText(tag))
        clickOnView(withText(tagGeneral))
        clickOnViewWithText(coreR.string.duration_dialog_save)
        tryAction { clickOnView(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(fullName2)))) }

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(coreR.string.settings_show_record_tag_close_hint))
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsRecordTagSelectionClose))

        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection))
        checkViewIsNotDisplayed(withText(coreR.string.settings_show_record_tag_close_hint))
        checkViewIsNotDisplayed(withId(settingsR.id.checkboxSettingsRecordTagSelectionClose))
    }

    @Test
    fun recordTagSelectionWithOnlyGeneral() {
        val name = "TypeName"
        val tagGeneral = "TagGeneral"

        // Add data
        testUtils.addActivity(name)
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)))) }

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        checkViewIsNotDisplayed(withText(coreR.string.settings_show_record_tag_general_hint))
        checkViewIsNotDisplayed(withId(settingsR.id.checkboxSettingsRecordTagSelectionGeneral))

        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowRecordTagSelection))
        onView(withId(settingsR.id.checkboxSettingsRecordTagSelectionGeneral)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(coreR.string.settings_show_record_tag_general_hint))
        onView(withId(settingsR.id.checkboxSettingsRecordTagSelectionGeneral)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsRecordTagSelectionGeneral))
        onView(withId(settingsR.id.checkboxSettingsRecordTagSelectionGeneral)).check(matches(isChecked()))

        // No tags - started right away
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }

        // Add tag
        testUtils.addRecordTag(tagGeneral)

        // Has a tag - show dialog
        clickOnViewWithText(name)
        tryAction { checkViewIsDisplayed(withText(coreR.string.change_record_untagged)) }
        checkViewIsDisplayed(withText(tagGeneral))
        pressBack()

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.checkboxSettingsRecordTagSelectionGeneral)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsRecordTagSelectionGeneral)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsRecordTagSelectionGeneral))
        onView(withId(settingsR.id.checkboxSettingsRecordTagSelectionGeneral)).check(matches(isNotChecked()))

        // Start with tags - no dialog
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }
    }

    @Test
    fun csvExportSettings() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsBackup()
        onView(withId(settingsR.id.layoutSettingsExportCsv)).perform(nestedScrollTo(), click())

        // View is set up
        val currentTime = System.currentTimeMillis()
        var timeStarted = timeMapper.formatDateTime(
            time = currentTime - TimeUnit.DAYS.toMillis(7), useMilitaryTime = true, showSeconds = false
        )
        var timeEnded = timeMapper.formatDateTime(
            time = currentTime, useMilitaryTime = true, showSeconds = false
        )
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))

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
        clickOnViewWithId(dialogsR.id.tvCsvExportSettingsTimeStarted)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(setDate(year, month + 1, day))
        clickOnView(
            allOf(
                isDescendantOfA(withId(dialogsR.id.tabsDateTimeDialog)), withText(coreR.string.date_time_dialog_time)
            )
        )
        onView(withClassName(equalTo(CustomTimePicker::class.java.name)))
            .perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

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

        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))

        // Set time ended
        clickOnViewWithId(dialogsR.id.tvCsvExportSettingsTimeEnded)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(setDate(year, month + 1, day))
        clickOnView(
            allOf(
                isDescendantOfA(withId(dialogsR.id.tabsDateTimeDialog)), withText(coreR.string.date_time_dialog_time)
            )
        )
        onView(withClassName(equalTo(CustomTimePicker::class.java.name)))
            .perform(setTime(hourEnded, minutesEnded))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

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

        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))
    }

    @Test
    fun showRecordsCalendar() {
        val name = "Test"

        // Add activity
        testUtils.addActivity(name = name)
        testUtils.addRecord(name)

        // Record is shown
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withId(recordsR.id.viewRecordsCalendar), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(recordsR.id.tvRecordsCalendarHint), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(settingsR.id.checkboxSettingsShowRecordsCalendar)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsShowRecordsCalendar)).check(matches(isNotChecked()))
        checkViewIsNotDisplayed(withText(coreR.string.settings_reverse_order_in_calendar))
        checkViewIsNotDisplayed(withId(settingsR.id.checkboxSettingsReverseOrderInCalendar))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowRecordsCalendar))
        onView(withId(settingsR.id.checkboxSettingsShowRecordsCalendar)).check(matches(isChecked()))

        // Record is not shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withId(recordsR.id.viewRecordsCalendar), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(recordsR.id.tvRecordsCalendarHint), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))

        // Check reverse order
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.checkboxSettingsReverseOrderInCalendar)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(coreR.string.settings_reverse_order_in_calendar))
        checkViewIsDisplayed(withId(settingsR.id.checkboxSettingsReverseOrderInCalendar))
        onView(withId(settingsR.id.checkboxSettingsReverseOrderInCalendar)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsReverseOrderInCalendar))
        onView(withId(settingsR.id.checkboxSettingsReverseOrderInCalendar)).check(matches(isChecked()))
        NavUtils.openRecordsScreen()

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.checkboxSettingsShowRecordsCalendar)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsShowRecordsCalendar)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowRecordsCalendar))
        onView(withId(settingsR.id.checkboxSettingsShowRecordsCalendar)).check(matches(isNotChecked()))
        checkViewIsNotDisplayed(withText(coreR.string.settings_reverse_order_in_calendar))
        checkViewIsNotDisplayed(withId(settingsR.id.checkboxSettingsReverseOrderInCalendar))

        // Record is shown
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withId(recordsR.id.viewRecordsCalendar), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(recordsR.id.tvRecordsCalendarHint), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
    }

    @Test
    fun daysInCalendar() {
        fun mapTitle(
            shiftStart: Int,
            shiftEnd: Int,
        ): String {
            return timeMapper.toDayShortDateTitle(shiftStart, 0) +
                " - " +
                timeMapper.toDayShortDateTitle(shiftEnd, 0)
        }

        // Disabled
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(coreR.string.title_today), isCompletelyDisplayed()))

        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(settingsR.id.checkboxSettingsShowRecordsCalendar)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsShowRecordsCalendar)).check(matches(isNotChecked()))
        checkViewIsNotDisplayed(withText(coreR.string.settings_days_in_calendar))

        // Change setting
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowRecordsCalendar))
        onView(withText(coreR.string.settings_days_in_calendar)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(coreR.string.settings_days_in_calendar))

        // One day
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(coreR.string.title_today), isCompletelyDisplayed()))

        // Three days
        NavUtils.openSettingsScreen()
        clickOnSpinnerWithId(settingsR.id.spinnerSettingsDaysInCalendar)
        clickOnViewWithText("3")
        NavUtils.openRecordsScreen()

        checkViewIsDisplayed(withText(mapTitle(-2, 0)))
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        checkViewIsDisplayed(withText(mapTitle(-5, -3)))
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
        checkViewIsDisplayed(withText(mapTitle(1, 3)))
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)

        // Five days
        NavUtils.openSettingsScreen()
        clickOnSpinnerWithId(settingsR.id.spinnerSettingsDaysInCalendar)
        clickOnViewWithText("5")
        NavUtils.openRecordsScreen()

        checkViewIsDisplayed(withText(mapTitle(-4, 0)))
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        checkViewIsDisplayed(withText(mapTitle(-9, -5)))
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
        checkViewIsDisplayed(withText(mapTitle(1, 5)))
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)

        // Seven days
        NavUtils.openSettingsScreen()
        clickOnSpinnerWithId(settingsR.id.spinnerSettingsDaysInCalendar)
        clickOnViewWithText("7")
        NavUtils.openRecordsScreen()

        checkViewIsDisplayed(withText(mapTitle(-6, 0)))
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        checkViewIsDisplayed(withText(mapTitle(-13, -7)))
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
        checkViewIsDisplayed(withText(mapTitle(1, 7)))
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)

        // Disable
        NavUtils.openSettingsScreen()
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowRecordsCalendar))
        checkViewIsNotDisplayed(withText(coreR.string.settings_days_in_calendar))
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(coreR.string.title_today), isCompletelyDisplayed()))
    }

    @Test
    fun keepStatisticsRange() {
        val name = "Test"

        // Add activity
        testUtils.addActivity(name = name)
        testUtils.addRecord(name)

        // Check range not transferred
        NavUtils.openStatisticsScreen()
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_week)
        checkViewIsDisplayed(allOf(withText(coreR.string.title_this_week), isCompletelyDisplayed()))
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.title_today), isCompletelyDisplayed()))
        pressBack()

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(settingsR.id.checkboxSettingsKeepStatisticsRange)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsKeepStatisticsRange)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsKeepStatisticsRange))
        onView(withId(settingsR.id.checkboxSettingsKeepStatisticsRange)).check(matches(isChecked()))

        // Check range transfer
        NavUtils.openStatisticsScreen()
        checkViewIsDisplayed(allOf(withText(coreR.string.title_this_week), isCompletelyDisplayed()))
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.title_this_week), isCompletelyDisplayed()))
        pressBack()
    }

    @Test
    fun automatedTracking() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(settingsR.id.btnSettingsAutomatedTracking)).perform(nestedScrollTo(), click())
        checkViewIsDisplayed(
            allOf(withId(dialogsR.id.tvHelpDialogTitle), withText(coreR.string.settings_automated_tracking))
        )
    }

    @Test
    fun automatedTrackingSendEvents() {
        val name = "name"

        // Add data
        testUtils.addActivity(name)

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        onView(withId(settingsR.id.checkboxSettingsAutomatedTrackingSend)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsAutomatedTrackingSend)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsAutomatedTrackingSend))
        onView(withId(settingsR.id.checkboxSettingsAutomatedTrackingSend)).check(matches(isChecked()))

        // Start stop activity
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.checkboxSettingsAutomatedTrackingSend)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsAutomatedTrackingSend)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsAutomatedTrackingSend))
        onView(withId(settingsR.id.checkboxSettingsAutomatedTrackingSend)).check(matches(isNotChecked()))

        // Start stop activity
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name)))
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }
    }

    @Test
    fun showFiltersOnMain() {
        val name = "ActivityFilter"

        // Add filter
        testUtils.addActivityFilter(name, ActivityFilter.Type.Activity)

        // Filters not shown
        tryAction {
            checkViewIsDisplayed(withText(coreR.string.running_records_add_type))
            checkViewDoesNotExist(withText(coreR.string.running_records_add_filter))
            checkViewDoesNotExist(withText(name))
        }

        // Check settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(settingsR.id.checkboxSettingsShowActivityFilters)).perform(nestedScrollTo())
        onView(withId(settingsR.id.checkboxSettingsShowActivityFilters)).check(matches(isNotChecked()))

        // Change setting
        unconstrainedClickOnView(withId(settingsR.id.checkboxSettingsShowActivityFilters))
        onView(withId(settingsR.id.checkboxSettingsShowActivityFilters)).check(matches(isChecked()))

        // Filters shown
        NavUtils.openRunningRecordsScreen()
        checkViewIsDisplayed(withText(coreR.string.running_records_add_filter))
        checkViewIsDisplayed(withText(name))
    }

    private fun clearDuration() {
        repeat(6) { clickOnViewWithId(dialogsR.id.ivDurationPickerDelete) }
    }

    private fun checkRecord(
        name: String = "",
        nameResId: Int? = null,
        timeStart: String,
        timeEnd: String,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(if (nameResId != null) withText(nameResId) else withText(name)),
                hasDescendant(allOf(withId(changeRecordTypeR.id.tvRecordItemTimeStarted), withText(timeStart))),
                hasDescendant(allOf(withId(changeRecordTypeR.id.tvRecordItemTimeFinished), withText(timeEnd))),
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
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(if (nameResId != null) withText(nameResId) else withText(name)),
                hasDescendant(withSubstring("$hours$hourString 0$minuteString")),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkStatisticsDetailRecords(count: Int) {
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, count),
                hasSibling(withText(count.toString())),
                isCompletelyDisplayed()
            )
        )
    }

    private fun Long.toTimePreview(): String {
        return timeMapper.formatTime(time = this, useMilitaryTime = true, showSeconds = false)
    }
}
