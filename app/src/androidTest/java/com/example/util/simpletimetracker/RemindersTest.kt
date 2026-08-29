package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.SimpleTimeZone
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.feature_reminders.R as remindersR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RemindersTest : BaseUiTest() {

    @Test
    fun settingsNavigationAndEmptyState() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsNotifications()
        NavUtils.openRemindersScreen()

        checkViewIsDisplayed(withText(R.string.settings_reminders_title))
        checkViewIsDisplayed(withText(R.string.settings_reminders_hint))
        checkViewIsDisplayed(withText(R.string.running_records_add_type))
    }

    @Test
    fun populatedStateSortingSummariesToggleAndRefresh() {
        runBlocking { prefsInteractor.setUseMilitaryTimeFormat(true) }
        val activityName = "Walking"
        testUtils.addActivity(name = activityName, icon = firstIcon, color = firstColor)
        val activityId = runBlocking {
            testUtils.recordTypeInteractor.getAll().first { it.name == activityName }.id
        }
        val tomorrow = calendar.apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_MONTH, 1)
            setToStartOfDay()
        }
        val tomorrowEpochDay = tomorrow.toEpochDay()
        val earlierId = testUtils.addScheduledReminder(
            reminder(
                text = "Earlier",
                enabled = true,
                schedule = ScheduledReminder.Schedule.OneTime(
                    oneTimeDate = tomorrowEpochDay,
                    timeOfDayMillis = hours(8),
                ),
            ),
        )
        testUtils.addScheduledReminder(
            reminder(
                text = "Later",
                enabled = true,
                schedule = ScheduledReminder.Schedule.OneTime(
                    oneTimeDate = tomorrowEpochDay,
                    timeOfDayMillis = hours(9),
                ),
            ),
        )
        val weeklyId = testUtils.addScheduledReminder(
            reminder(
                text = "Weekly walk",
                enabled = false,
                schedule = ScheduledReminder.Schedule.Weekly(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    timeOfDayMillis = hours(10),
                ),
                condition = ScheduledReminder.Condition.ActivityNotTrackedToday(activityId),
            ),
        )

        NavUtils.openSettingsScreen()
        NavUtils.openSettingsNotifications()
        NavUtils.openRemindersScreen()

        onView(withText("Earlier")).check(isCompletelyAbove(withText("Later")))
        onView(withText("Later")).check(isCompletelyAbove(withText("Weekly walk")))
        checkViewIsNotDisplayed(
            allOf(
                withId(remindersR.id.tvReminderCondition),
                isDescendantOfA(
                    allOf(
                        withId(remindersR.id.containerReminder),
                        hasDescendant(withText("Earlier")),
                    ),
                ),
            ),
        )
        val oneTimeTimestamp = tomorrow.apply {
            set(Calendar.HOUR_OF_DAY, 8)
        }.timeInMillis
        checkViewIsDisplayed(
            withText(
                "${getString(R.string.reminders_schedule_one_time)} " +
                    timeMapper.formatDateTimeYear(oneTimeTimestamp, useMilitaryTime = true),
            ),
        )
        checkViewIsDisplayed(
            withText(
                "${getString(R.string.reminders_condition_activity_not_tracked)} ($activityName)",
            ),
        )
        checkViewIsDisplayed(
            withText(
                "${timeMapper.toShortDayOfWeekName(DayOfWeek.MONDAY)} ${formatTime(hours(10))}",
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(remindersR.id.containerReminder),
                hasDescendant(withText("Weekly walk")),
                hasDescendant(withTag(firstIcon)),
                isCompletelyDisplayed(),
            ),
        )

        val weeklyEnableButton = allOf(
            withId(remindersR.id.btnReminderEnabled),
            isDescendantOfA(
                allOf(
                    withId(remindersR.id.containerReminder),
                    hasDescendant(withText("Weekly walk")),
                ),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withText(R.string.complex_rules_enable),
                isDescendantOfA(weeklyEnableButton),
            ),
        )
        clickOnView(weeklyEnableButton)
        checkViewIsDisplayed(
            allOf(
                withText(R.string.complex_rules_disable),
                isDescendantOfA(weeklyEnableButton),
            ),
        )
        assertEquals(true, testUtils.getScheduledReminder(weeklyId)?.enabled)
        assertEquals(true, testUtils.getScheduledReminder(earlierId)?.enabled)

        testUtils.addScheduledReminder(
            reminder(
                text = "Monthly refresh",
                enabled = false,
                schedule = ScheduledReminder.Schedule.Monthly(dayOfMonth = 31, timeOfDayMillis = hours(11)),
            ),
        )
        pressBack()
        NavUtils.openRemindersScreen()
        scrollRecyclerToView(
            remindersR.id.rvRemindersList,
            hasDescendant(withText("Monthly refresh")),
        )
        checkViewIsDisplayed(withText("Monthly refresh"))
        checkViewIsDisplayed(
            withText("${getString(R.string.reminders_schedule_monthly)} 31 ${formatTime(hours(11))}"),
        )
    }

    private fun reminder(
        text: String,
        enabled: Boolean,
        schedule: ScheduledReminder.Schedule,
        condition: ScheduledReminder.Condition = ScheduledReminder.Condition.Always,
    ): ScheduledReminder {
        return ScheduledReminder(
            id = 0L,
            enabled = enabled,
            text = text,
            schedule = schedule,
            condition = condition,
        )
    }

    private fun hours(value: Long): Long {
        return TimeUnit.HOURS.toMillis(value)
    }

    private fun formatTime(timeOfDayMillis: Long): String {
        val timestamp = calendar.apply {
            timeInMillis = System.currentTimeMillis()
            setToStartOfDay()
        }.timeInMillis + timeOfDayMillis
        return timestamp.formatTime()
    }

    private fun Calendar.toEpochDay(): Long {
        val localYear = get(Calendar.YEAR)
        val localMonth = get(Calendar.MONTH)
        val localDay = get(Calendar.DAY_OF_MONTH)
        val utcDate = Calendar.getInstance(SimpleTimeZone(0, "UTC")).apply {
            clear()
            set(localYear, localMonth, localDay)
        }
        return TimeUnit.MILLISECONDS.toDays(utcDate.timeInMillis)
    }
}
