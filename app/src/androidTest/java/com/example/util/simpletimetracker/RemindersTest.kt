package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.domain.activityReminder.model.ActivityReminderOverride
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.feature_reminders.viewData.RemindersButtonViewData
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnVisibleView
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.SimpleTimeZone
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.feature_base_adapter.R as baseAdapterR
import com.example.util.simpletimetracker.feature_reminders.R as remindersR
import com.example.util.simpletimetracker.feature_change_reminder.R as changeReminderR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RemindersTest : BaseUiTest() {

    @Test
    fun change() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsNotifications()
        NavUtils.openRemindersScreen()

        clickOnView(withTag(RemindersButtonViewData.SCHEDULED))
        checkViewIsNotDisplayed(withId(changeReminderR.id.containerChangeReminderWeekdays))
        checkViewIsNotDisplayed(withId(changeReminderR.id.containerChangeReminderCondition))
        checkViewIsNotDisplayed(withId(changeReminderR.id.containerChangeReminderHourly))
        checkViewIsNotDisplayed(withId(changeReminderR.id.btnChangeReminderDelete))

        selectSpinnerItem(
            fieldId = changeReminderR.id.fieldChangeReminderSchedule,
            itemTextResId = R.string.reminders_schedule_weekly,
        )
        selectSpinnerItem(
            fieldId = changeReminderR.id.fieldChangeReminderCondition,
            itemTextResId = R.string.reminders_condition_activity_not_tracked,
        )
        checkViewIsDisplayed(withId(dialogsR.id.rvTypesSelectionContainer))
        pressBack()
        checkViewIsDisplayed(
            allOf(
                withId(changeReminderR.id.tvChangeReminderCondition),
                withText(R.string.change_reminder_condition_always),
            ),
        )
        checkViewIsNotDisplayed(withId(changeReminderR.id.btnChangeReminderActivity))

        selectSpinnerItem(
            fieldId = changeReminderR.id.fieldChangeReminderSchedule,
            itemTextResId = R.string.reminders_schedule_one_time,
        )
        checkViewIsDisplayed(withId(changeReminderR.id.tvChangeReminderDate))
        checkViewIsNotDisplayed(withId(changeReminderR.id.containerChangeReminderHourly))
        checkViewIsNotDisplayed(withId(changeReminderR.id.containerChangeReminderWeekdays))
        checkViewIsNotDisplayed(withId(changeReminderR.id.containerChangeReminderCondition))

        selectSpinnerItem(
            fieldId = changeReminderR.id.fieldChangeReminderSchedule,
            itemTextResId = R.string.reminders_schedule_hourly,
        )
        checkViewIsDisplayed(withId(changeReminderR.id.containerChangeReminderHourly))
        checkViewIsDisplayed(withId(changeReminderR.id.tvChangeReminderDate))
        checkViewIsDisplayed(withId(changeReminderR.id.containerChangeReminderWeekdays))
        checkViewIsNotDisplayed(withId(changeReminderR.id.containerChangeReminderDayOfMonth))
        checkViewIsNotDisplayed(withId(changeReminderR.id.containerChangeReminderCondition))

        selectSpinnerItem(
            fieldId = changeReminderR.id.fieldChangeReminderSchedule,
            itemTextResId = R.string.reminders_schedule_monthly,
        )
        checkViewIsDisplayed(withId(changeReminderR.id.containerChangeReminderDayOfMonth))
        checkViewIsNotDisplayed(withId(changeReminderR.id.containerChangeReminderHourly))
        checkViewIsNotDisplayed(withId(changeReminderR.id.tvChangeReminderDate))

        clickOnView(withId(changeReminderR.id.btnChangeReminderSave))
        checkViewIsDisplayed(withText(R.string.change_reminder_message_required))

        val message = "A monthly reminder"
        typeTextIntoView(changeReminderR.id.etChangeReminderMessage, message)
        clickOnView(withId(changeReminderR.id.btnChangeReminderSave))

        checkViewIsDisplayed(withText(message))
        val disableButton = allOf(
            withId(remindersR.id.btnReminderEnabled),
            isDescendantOfA(
                allOf(
                    withId(remindersR.id.containerReminder),
                    hasDescendant(withText(message)),
                ),
            ),
        )
        clickOnView(disableButton)
        checkViewIsDisplayed(
            allOf(
                withText(R.string.complex_rules_enable),
                isDescendantOfA(disableButton),
            ),
        )
        clickOnView(withText(message))
        checkViewIsDisplayed(withId(changeReminderR.id.btnChangeReminderDelete))
        checkViewIsDisplayed(allOf(withId(changeReminderR.id.etChangeReminderMessage), withText(message)))
        clickOnView(withId(changeReminderR.id.btnChangeReminderDelete))
        checkViewDoesNotExist(withText(message))
    }

    @Test
    fun empty() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsNotifications()
        NavUtils.openRemindersScreen()

        checkViewIsDisplayed(
            allOf(
                withId(remindersR.id.tvRemindersTitle),
                withText(R.string.settings_reminders_title),
            ),
        )
        checkViewIsDisplayed(withText(R.string.settings_reminders_hint))
        checkViewIsDisplayed(
            allOf(
                withId(baseAdapterR.id.tvHeaderItem),
                withText(R.string.notification_activity_title),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseAdapterR.id.tvHeaderItemHint),
                withText(R.string.activity_reminders_hint),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseAdapterR.id.tvHeaderItem),
                withText(R.string.settings_reminders_title),
            ),
        )
        checkViewDoesNotExist(withId(remindersR.id.containerReminder))
    }

    @Test
    fun activityChange() {
        runBlocking { prefsInteractor.setUseMilitaryTimeFormat(true) }
        val activityName = "Reading"
        testUtils.addActivity(name = activityName, icon = firstIcon, color = firstColor)
        val activityId = runBlocking {
            testUtils.recordTypeInteractor.getAll().first { it.name == activityName }.id
        }

        NavUtils.openSettingsScreen()
        NavUtils.openSettingsNotifications()
        NavUtils.openRemindersScreen()

        clickOnVisibleView(withTag(RemindersButtonViewData.ACTIVITY))
        checkViewIsDisplayed(withId(changeReminderR.id.containerActivityReminderCustom))
        checkViewIsNotDisplayed(withId(changeReminderR.id.btnActivityReminderDelete))
        checkViewIsDisplayed(
            allOf(
                withId(changeReminderR.id.tvActivityReminderMode),
                withText(R.string.activity_reminder_mode_custom),
            ),
        )

        clickOnView(withId(changeReminderR.id.fieldActivityReminderName))
        clickOnView(
            allOf(
                withText(activityName),
                isDescendantOfA(withId(dialogsR.id.rvTypesSelectionContainer)),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(changeReminderR.id.tvActivityReminderName),
                withText(activityName),
            ),
        )

        clickOnView(withId(changeReminderR.id.btnActivityReminderSave))
        checkViewIsDisplayed(withText(R.string.activity_reminder_positive_duration_required))

        clickOnView(withId(changeReminderR.id.fieldActivityReminderDuration))
        clickOnView(withId(dialogsR.id.tvNumberKeyboard1))
        clickOnView(withId(dialogsR.id.btnDurationPickerSave))
        checkViewIsDisplayed(
            allOf(
                withId(changeReminderR.id.tvActivityReminderDurationValue),
                withText(timeMapper.formatDuration(1)),
            ),
        )
        clickOnView(withId(changeReminderR.id.btnActivityReminderSave))

        val customMode = runBlocking {
            testUtils.activityReminderOverrideInteractor.get(activityId)?.mode
        } as? ActivityReminderOverride.Mode.Custom
        assertEquals(1L, customMode?.rule?.durationSeconds)
        assertEquals(false, customMode?.rule?.recurrent)
        assertEquals(DayOfWeek.entries.toSet(), customMode?.rule?.applicableDaysOfWeek)
        assertEquals(0L, customMode?.rule?.doNotDisturbStartMillis)
        assertEquals(hours(8), customMode?.rule?.doNotDisturbEndMillis)

        val customSummary = listOf(
            "1s",
            getString(R.string.reminders_schedule_one_time),
            "00:00-08:00",
        ).joinToString(separator = " · ")
        checkViewIsDisplayed(
            allOf(
                withId(remindersR.id.containerReminder),
                hasDescendant(withText(activityName)),
                hasDescendant(withText(R.string.activity_reminder_mode_custom)),
                hasDescendant(withText(customSummary)),
            ),
        )

        clickOnView(
            allOf(
                withId(remindersR.id.containerReminder),
                hasDescendant(withText(activityName)),
            ),
        )
        onView(withId(changeReminderR.id.fieldActivityReminderName))
            .check(matches(not(isEnabled())))
        checkViewIsDisplayed(withId(changeReminderR.id.btnActivityReminderDelete))

        selectSpinnerItem(
            fieldId = changeReminderR.id.fieldActivityReminderMode,
            itemTextResId = R.string.activity_reminder_mode_disabled,
        )
        checkViewIsNotDisplayed(withId(changeReminderR.id.containerActivityReminderCustom))
        clickOnView(withId(changeReminderR.id.btnActivityReminderSave))

        assertEquals(
            ActivityReminderOverride.Mode.Disabled,
            runBlocking { testUtils.activityReminderOverrideInteractor.get(activityId)?.mode },
        )
        checkViewIsDisplayed(
            allOf(
                withId(remindersR.id.containerReminder),
                hasDescendant(withText(activityName)),
                hasDescendant(withText(R.string.activity_reminder_mode_disabled)),
                hasDescendant(withText(R.string.activity_reminder_disabled_summary)),
            ),
        )

        clickOnView(
            allOf(
                withId(remindersR.id.containerReminder),
                hasDescendant(withText(activityName)),
            ),
        )
        clickOnView(withId(changeReminderR.id.btnActivityReminderDelete))
        checkViewDoesNotExist(withText(activityName))
        assertEquals(null, runBlocking { testUtils.activityReminderOverrideInteractor.get(activityId) })
    }

    @Test
    fun activityList() {
        runBlocking { prefsInteractor.setUseMilitaryTimeFormat(true) }
        testUtils.setFirstDayOfWeek(DayOfWeek.MONDAY)
        val disabledName = "Walking"
        val customName = "Reading"
        testUtils.addActivity(name = disabledName, icon = firstIcon, color = firstColor)
        testUtils.addActivity(name = customName, icon = lastIcon, color = lastColor)
        val activities = runBlocking {
            testUtils.recordTypeInteractor.getAll().associateBy { it.name }
        }
        testUtils.addActivityReminderOverride(
            ActivityReminderOverride(
                activityId = activities.getValue(disabledName).id,
                mode = ActivityReminderOverride.Mode.Disabled,
            ),
        )
        testUtils.addActivityReminderOverride(
            ActivityReminderOverride(
                activityId = activities.getValue(customName).id,
                mode = ActivityReminderOverride.Mode.Custom(
                    rule = ActivityReminderOverride.Rule(
                        id = 0L,
                        durationSeconds = 60L,
                        recurrent = true,
                        applicableDaysOfWeek = setOf(DayOfWeek.MONDAY),
                        doNotDisturbStartMillis = hours(22),
                        doNotDisturbEndMillis = hours(8),
                    ),
                ),
            ),
        )

        NavUtils.openSettingsScreen()
        NavUtils.openSettingsNotifications()
        NavUtils.openRemindersScreen()

        val disabledReminder = allOf(
            withId(remindersR.id.containerReminder),
            hasDescendant(withText(disabledName)),
            hasDescendant(withText(R.string.activity_reminder_mode_disabled)),
            hasDescendant(withText(R.string.activity_reminder_disabled_summary)),
            hasDescendant(withTag(firstIcon)),
        )
        checkViewIsDisplayed(disabledReminder)

        val customSummary = listOf(
            "1m",
            getString(R.string.settings_inactivity_reminder_recurrent),
            "Mon",
            "22:00-08:00",
        ).joinToString(separator = " · ")
        val customReminder = allOf(
            withId(remindersR.id.containerReminder),
            hasDescendant(withText(customName)),
            hasDescendant(withText(R.string.activity_reminder_mode_custom)),
            hasDescendant(withText(customSummary)),
            hasDescendant(withTag(lastIcon)),
        )
        scrollRecyclerToView(remindersR.id.rvRemindersList, customReminder)
        checkViewIsDisplayed(allOf(customReminder, isCompletelyDisplayed()))
        onView(withText(customName)).check(isCompletelyAbove(withText(disabledName)))
    }

    @Test
    fun list() {
        runBlocking { prefsInteractor.setUseMilitaryTimeFormat(true) }
        val activityName = "Walking"
        testUtils.addActivity(name = activityName, icon = firstIcon, color = firstColor)
        val activityId = runBlocking {
            testUtils.recordTypeInteractor.getAll().first { it.name == activityName }.id
        }
        val tomorrow = Calendar.getInstance().apply {
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

        onView(withText("Weekly walk")).check(isCompletelyAbove(withText("Earlier")))
        onView(withText("Earlier")).check(isCompletelyAbove(withText("Later")))
        checkViewIsNotDisplayed(
            allOf(
                withId(remindersR.id.tvReminderSummary),
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
                "${getString(R.string.reminders_schedule_one_time)} · " +
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
                "${timeMapper.toShortDayOfWeekName(DayOfWeek.MONDAY)} · ${formatTime(hours(10))}",
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
        testUtils.addScheduledReminder(
            reminder(
                text = "Hourly refresh",
                enabled = false,
                schedule = ScheduledReminder.Schedule.Hourly(
                    intervalSeconds = TimeUnit.HOURS.toSeconds(2),
                    startDate = tomorrowEpochDay,
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    doNotDisturbStartMillis = hours(22),
                    doNotDisturbEndMillis = hours(8),
                    timeOfDayMillis = hours(12),
                ),
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
            withText("${getString(R.string.reminders_schedule_monthly)} · 31 · ${formatTime(hours(11))}"),
        )
        scrollRecyclerToView(
            remindersR.id.rvRemindersList,
            hasDescendant(withText("Hourly refresh")),
        )
        checkViewIsDisplayed(withText("Hourly refresh"))
        val hourlyStartTimestamp = tomorrow.apply {
            set(Calendar.HOUR_OF_DAY, 12)
        }.timeInMillis
        val hourlyStart = getString(
            R.string.separator_template,
            getString(R.string.change_record_date_time_start),
            hourlyStartTimestamp.formatDateTime(),
        )
        val hourlySummary = listOf(
            getString(R.string.reminders_schedule_hourly),
            "2h",
            hourlyStart,
            timeMapper.toShortDayOfWeekName(DayOfWeek.MONDAY),
            "22:00-08:00",
        ).joinToString(separator = " · ")
        checkViewIsDisplayed(withText(hourlySummary))
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

    private fun selectSpinnerItem(fieldId: Int, itemTextResId: Int) {
        clickOnView(withId(fieldId))
        onData(equalTo(getString(itemTextResId))).perform(click())
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
