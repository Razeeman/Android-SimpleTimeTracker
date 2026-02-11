package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.DateSelectorUtils.toWeekTitle
import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.extension.setWeekToFirstDay
import com.example.util.simpletimetracker.core.interactor.LanguageInteractor
import com.example.util.simpletimetracker.domain.activityFilter.model.ActivityFilter
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.padDuration
import com.example.util.simpletimetracker.domain.language.AppLanguage
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSuggestion.RecordTypeSuggestionViewData
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomDatePicker
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomTimePicker
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkCheckboxIsChecked
import com.example.util.simpletimetracker.utils.checkCheckboxIsNotChecked
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnCurrentDate
import com.example.util.simpletimetracker.utils.clickOnCurrentSelectedDate
import com.example.util.simpletimetracker.utils.clickOnPrevDate
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.clickOnVisibleView
import com.example.util.simpletimetracker.utils.dateSelectorMatcher
import com.example.util.simpletimetracker.utils.getMillis
import com.example.util.simpletimetracker.utils.longClickOnCurrentDate
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.longClickOnVisibleView
import com.example.util.simpletimetracker.utils.recyclerItemCount
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withPluralText
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_records.R as recordsR
import com.example.util.simpletimetracker.feature_statistics_detail.R as statisticsDetailR
import com.example.util.simpletimetracker.feature_tag_selection.R as tagSelectionR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsTest : BaseUiTest() {

    @Test
    fun showUntrackedInRecords() {
        val name = "Test"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)
        val before = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        testUtils.addRecord(name, timeStarted = before, timeEnded = before)

        // Untracked is shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_show_untracked_time)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_untracked_time))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_untracked_time)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_untracked_time))

        // Untracked is not shown
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(
            allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()),
        )

        // Change setting
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_untracked_time)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_untracked_time))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_untracked_time)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_untracked_time))

        // Untracked is shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
    }

    @Test
    fun showUntrackedInStatistics() {
        val name = "Test"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)
        val before = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        testUtils.addRecord(name, timeStarted = before, timeEnded = before)

        // Untracked is shown
        NavUtils.openStatisticsScreen()
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_show_untracked_time_statistics)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_untracked_time_statistics))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_untracked_time_statistics)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_untracked_time_statistics))

        // Untracked is not shown
        NavUtils.openStatisticsScreen()
        checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))

        // Add record
        NavUtils.openRecordsScreen()
        testUtils.addRecord(name)
        NavUtils.openStatisticsScreen()
        checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_untracked_time_statistics)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_untracked_time_statistics))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_untracked_time_statistics)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_untracked_time_statistics))

        // Untracked is shown
        NavUtils.openStatisticsScreen()
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
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
                durationFormat = DurationFormat.HOURS,
            )
            val matcher = allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(allOf(withId(changeRecordTypeR.id.tvRecordItemDuration), withText(duration))),
                isCompletelyDisplayed(),
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
        scrollSettingsRecyclerToText(coreR.string.settings_ignore_short_untracked)
        clickOnSettingsSelectorBesideText(coreR.string.settings_ignore_short_untracked)
        clickOnViewWithText(coreR.string.duration_dialog_disable)
        scrollSettingsRecyclerToText(coreR.string.settings_ignore_short_untracked)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(
                coreR.string.settings_ignore_short_untracked,
                withText(coreR.string.settings_inactivity_reminder_disabled),
            ),
        )

        NavUtils.openRecordsScreen()
        clickOnPrevDate()
        checkItemCount(9)
        checkRecordDuration(TimeUnit.HOURS.toMillis(20), displayed = true)
        checkRecordDuration(TimeUnit.MINUTES.toMillis(30), displayed = true)
        checkRecordDuration(TimeUnit.MINUTES.toMillis(1), displayed = true)
        checkRecordDuration(TimeUnit.SECONDS.toMillis(1), displayed = true)

        // Check 30 minutes
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_ignore_short_untracked)
        clickOnSettingsSelectorBesideText(coreR.string.settings_ignore_short_untracked)
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
        scrollSettingsRecyclerToText(coreR.string.settings_ignore_short_untracked)
        clickOnSettingsSelectorBesideText(coreR.string.settings_ignore_short_untracked)
        repeat(4) { clickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete) }
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
        clickOnCurrentDate(-1)
        checkRecord(
            nameResId = coreR.string.untracked_time_name,
            timeStart = startOfDay.toTimePreview(),
            timeEnd = startOfDay.toTimePreview(),
        )

        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_show_records_calendar)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_untracked_range))
        checkViewIsNotDisplayed(settingsRangeStartBesideText(coreR.string.settings_untracked_range))
        checkViewIsNotDisplayed(settingsRangeEndBesideText(coreR.string.settings_untracked_range))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_untracked_range)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_untracked_range))
        checkViewIsDisplayed(settingsRangeStartBesideText(coreR.string.settings_untracked_range))
        checkViewIsDisplayed(settingsRangeEndBesideText(coreR.string.settings_untracked_range))

        // Check range
        var startPreview = (startOfDay + TimeUnit.HOURS.toMillis(8)).toTimePreview()
        clickOnSettingsRangeStartBesideText(coreR.string.settings_untracked_range)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(8, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(
            settingsRangeStartBesideText(
                coreR.string.settings_untracked_range,
                withText(startPreview),
            ),
        )
        var endPreview = (startOfDay + TimeUnit.HOURS.toMillis(17)).toTimePreview()
        clickOnSettingsRangeEndBesideText(coreR.string.settings_untracked_range)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(17, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(
            settingsRangeEndBesideText(
                coreR.string.settings_untracked_range,
                withText(endPreview),
            ),
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
        clickOnSettingsRangeStartBesideText(coreR.string.settings_untracked_range)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(17, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(
            settingsRangeStartBesideText(
                coreR.string.settings_untracked_range,
                withText(startPreview),
            ),
        )
        endPreview = (startOfDay + TimeUnit.HOURS.toMillis(8)).toTimePreview()
        clickOnSettingsRangeEndBesideText(coreR.string.settings_untracked_range)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(8, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(
            settingsRangeEndBesideText(
                coreR.string.settings_untracked_range,
                withText(endPreview),
            ),
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
            allOf(withText(name1), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed()),
        )
        checkViewDoesNotExist(
            allOf(withText(name2), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed()),
        )
        checkViewDoesNotExist(
            allOf(withText(name3), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed()),
        )

        // Change setting
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_allow_multitasking)
        tryAction { checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_allow_multitasking)) }
        clickOnSettingsCheckboxBesideText(coreR.string.settings_allow_multitasking)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_allow_multitasking))

        // Click on one not running
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name1)))
        tryAction {
            checkViewIsDisplayed(
                allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name1)), isCompletelyDisplayed()),
            )
        }
        checkViewDoesNotExist(
            allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name2)), isCompletelyDisplayed()),
        )
        checkViewDoesNotExist(
            allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name3)), isCompletelyDisplayed()),
        )

        // Records added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(withText(name2), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed()),
        )
        checkViewIsDisplayed(
            allOf(withText(name3), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed()),
        )

        // Click another
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name2)))
        tryAction {
            checkViewIsDisplayed(
                allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name2)), isCompletelyDisplayed()),
            )
        }
        checkViewDoesNotExist(
            allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name1)), isCompletelyDisplayed()),
        )

        // Record added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(withText(name1), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed()),
        )

        // Change setting back
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_allow_multitasking)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_allow_multitasking))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_allow_multitasking)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_allow_multitasking))

        // Start another timer
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name3)))
        tryAction {
            checkViewIsDisplayed(
                allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name2)), isCompletelyDisplayed()),
            )
        }
        checkViewIsDisplayed(
            allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name3)), isCompletelyDisplayed()),
        )

        // No new records added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(withText(name1), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed()),
        )
        checkViewIsDisplayed(
            allOf(withText(name2), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed()),
        )
        checkViewIsDisplayed(
            allOf(withText(name3), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed()),
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
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name1) }

        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsNotifications()
        scrollSettingsRecyclerToText(coreR.string.settings_show_notifications)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_notifications))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_notifications)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_notifications))

        // Stop first timer
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name1))))

        // Start another timer
        clickOnViewWithText(name2)

        // Change settings
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_notifications)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_notifications))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_notifications)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_notifications))
    }

    @Test
    fun showNotificationsControls() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsNotifications()

        // Check settings
        scrollSettingsRecyclerToText(coreR.string.settings_show_notifications)
        checkViewDoesNotExist(withText(coreR.string.settings_show_notifications_controls))

        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_notifications)
        scrollSettingsRecyclerToText(coreR.string.settings_show_notifications_controls)
        checkViewIsDisplayed(withText(coreR.string.settings_show_notifications_controls))
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_notifications_controls))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_notifications_controls)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_notifications_controls))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_notifications_controls)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_notifications_controls))

        // Change settings
        scrollSettingsRecyclerToText(coreR.string.settings_show_notifications)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_notifications)
        checkViewDoesNotExist(withText(coreR.string.settings_show_notifications_controls))
    }

    @Test
    fun showNotificationEvenWithNoTimers() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsNotifications()

        // Check settings
        scrollSettingsRecyclerToText(coreR.string.settings_show_notifications)
        checkViewDoesNotExist(withText(coreR.string.settings_show_notification_even_with_no_timers))

        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_notifications)
        scrollSettingsRecyclerToText(coreR.string.settings_show_notification_even_with_no_timers)
        checkViewIsDisplayed(withText(coreR.string.settings_show_notification_even_with_no_timers))
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_notification_even_with_no_timers))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_notification_even_with_no_timers)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_notification_even_with_no_timers))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_notification_even_with_no_timers)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_notification_even_with_no_timers))

        // Change settings
        scrollSettingsRecyclerToText(coreR.string.settings_show_notifications)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_notifications)
        checkViewDoesNotExist(withText(coreR.string.settings_show_notification_even_with_no_timers))
    }

    @Test
    fun enableEnableDarkMode() {
        val name1 = "Test1"
        val name2 = "Test2"

        // Add activities
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        Thread.sleep(1000)

        // Start one timer
        tryAction { clickOnViewWithText(name1) }

        // Add record
        testUtils.addRecord(name1)
        testUtils.addRecord(name2)

        // Check settings
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_dark_mode)
        checkViewIsDisplayed(
            settingsSpinnerValueBesideText(
                coreR.string.settings_dark_mode,
                withText(coreR.string.settings_dark_mode_system),
            ),
        )
        clickOnSettingsSpinnerBesideText(coreR.string.settings_dark_mode)
        clickOnViewWithText(coreR.string.settings_dark_mode_enabled)
        NavUtils.openSettingsScreen()
        checkViewIsDisplayed(
            settingsSpinnerValueBesideText(
                coreR.string.settings_dark_mode,
                withText(coreR.string.settings_dark_mode_enabled),
            ),
        )

        // Check screens
        NavUtils.openRunningRecordsScreen()
        NavUtils.openRecordsScreen()
        NavUtils.openStatisticsScreen()

        // Change settings
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_dark_mode)
        checkViewIsDisplayed(
            settingsSpinnerValueBesideText(
                coreR.string.settings_dark_mode,
                withText(coreR.string.settings_dark_mode_enabled),
            ),
        )
        clickOnSettingsSpinnerBesideText(coreR.string.settings_dark_mode)
        clickOnViewWithText(coreR.string.settings_inactivity_reminder_disabled)
        NavUtils.openSettingsScreen()
        checkViewIsDisplayed(
            settingsSpinnerValueBesideText(
                coreR.string.settings_dark_mode,
                withText(coreR.string.settings_inactivity_reminder_disabled),
            ),
        )

        // Check screens
        NavUtils.openRunningRecordsScreen()
        NavUtils.openRecordsScreen()
        NavUtils.openStatisticsScreen()
        NavUtils.openSettingsScreen()
    }

    @Test
    fun changeLanguage() {
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_language)
        checkViewIsDisplayed(
            settingsSpinnerValueBesideText(
                coreR.string.settings_dark_mode,
                withText(coreR.string.settings_dark_mode_system),
            ),
        )
        clickOnSettingsSpinnerBesideText(coreR.string.settings_language)
        LanguageInteractor.languageList
            .map(languageInteractor::getDisplayName)
            .forEach {
                onData(allOf(`is`(instanceOf(String::class.java)), `is`(it))).perform(scrollTo())
                checkViewIsDisplayed(withText(it))
            }
    }

    @Test
    fun navigation() {
        NavUtils.openSettingsScreen()

        NavUtils.openCategoriesScreen()
        checkViewIsDisplayed(withText(coreR.string.categories_record_type_hint))
        pressBack()

        NavUtils.openArchiveScreen()
        checkViewIsDisplayed(withText(coreR.string.archive_empty))
        pressBack()

        NavUtils.openDataEditScreen()
        checkViewIsDisplayed(withText(coreR.string.data_edit_select_records))
    }

    @Test
    fun feedbackBlock() {
        NavUtils.openSettingsScreen()

        scrollSettingsRecyclerToText(coreR.string.settings_rate)
        scrollSettingsRecyclerToText(coreR.string.settings_feedback)
        scrollSettingsRecyclerToText(coreR.string.settings_version)
    }

    @Test
    fun translators() {
        NavUtils.openSettingsScreen()

        scrollSettingsRecyclerToText(coreR.string.settings_translators)
        LanguageInteractor.languageList
            .filter { it !in listOf(AppLanguage.System, AppLanguage.English) }
            .map(languageInteractor::getTranslators)
            .forEach {
                scrollSettingsRecyclerToText(it)
                checkViewIsDisplayed(withText(it))
            }
    }

    @Test
    fun inactivityReminder() {
        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsNotifications()
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(
                coreR.string.settings_inactivity_reminder,
                withText(coreR.string.settings_inactivity_reminder_disabled),
            ),
        )
        checkViewDoesNotExist(withText(coreR.string.settings_inactivity_reminder_recurrent))

        // 1s
        clickOnSettingsSelectorBesideText(coreR.string.settings_inactivity_reminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$secondString"))

        // Check recurrent
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // 1m
        clickOnSettingsSelectorBesideText(coreR.string.settings_inactivity_reminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(
                coreR.string.settings_inactivity_reminder,
                withText("1$minuteString"),
            ),
        )
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // 1h
        clickOnSettingsSelectorBesideText(coreR.string.settings_inactivity_reminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString"))
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // 1m 1s
        clickOnSettingsSelectorBesideText(coreR.string.settings_inactivity_reminder)
        repeat(3) { clickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete) }
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$minuteString 01$secondString"))
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // 1h 1m 1s
        clickOnSettingsSelectorBesideText(coreR.string.settings_inactivity_reminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString 01$minuteString 01$secondString"))
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // 1h 30m
        clickOnSettingsSelectorBesideText(coreR.string.settings_inactivity_reminder)
        clearDuration()
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard9)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString 30$minuteString"))
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // 99h 99m 99s
        clickOnSettingsSelectorBesideText(coreR.string.settings_inactivity_reminder)
        repeat(10) { clickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete) }
        repeat(6) { clickOnViewWithId(dialogsR.id.tvNumberKeyboard9) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("100$hourString 40$minuteString 39$secondString"))
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // Disable
        clickOnSettingsSelectorBesideText(coreR.string.settings_inactivity_reminder)
        clickOnViewWithText(coreR.string.duration_dialog_disable)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(
                coreR.string.settings_inactivity_reminder,
                withText(coreR.string.settings_inactivity_reminder_disabled),
            ),
        )
        checkViewDoesNotExist(withText(coreR.string.settings_inactivity_reminder_recurrent))
    }

    @Test
    fun activityReminder() {
        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsNotifications()
        scrollSettingsRecyclerToText(coreR.string.settings_activity_reminder)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(
                coreR.string.settings_activity_reminder,
                withText(coreR.string.settings_inactivity_reminder_disabled),
            ),
        )
        checkViewDoesNotExist(withText(coreR.string.settings_inactivity_reminder_recurrent))

        // 1s
        clickOnSettingsSelectorBesideText(coreR.string.settings_activity_reminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$secondString"))

        // Check recurrent
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // 1m
        clickOnSettingsSelectorBesideText(coreR.string.settings_activity_reminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(
                coreR.string.settings_activity_reminder,
                withText("1$minuteString"),
            ),
        )
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // 1h
        clickOnSettingsSelectorBesideText(coreR.string.settings_activity_reminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString"))
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // 1m 1s
        clickOnSettingsSelectorBesideText(coreR.string.settings_activity_reminder)
        repeat(3) { clickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete) }
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$minuteString 01$secondString"))
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // 1h 1m 1s
        clickOnSettingsSelectorBesideText(coreR.string.settings_activity_reminder)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString 01$minuteString 01$secondString"))
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // 1h 30m
        clickOnSettingsSelectorBesideText(coreR.string.settings_activity_reminder)
        clearDuration()
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard9)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString 30$minuteString"))
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // 99h 99m 99s
        clickOnSettingsSelectorBesideText(coreR.string.settings_activity_reminder)
        repeat(10) { clickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete) }
        repeat(6) { clickOnViewWithId(dialogsR.id.tvNumberKeyboard9) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("100$hourString 40$minuteString 39$secondString"))
        scrollSettingsRecyclerToText(coreR.string.settings_inactivity_reminder_recurrent)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_inactivity_reminder_recurrent))

        // Disable
        clickOnSettingsSelectorBesideText(coreR.string.settings_activity_reminder)
        clickOnViewWithText(coreR.string.duration_dialog_disable)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(
                coreR.string.settings_activity_reminder,
                withText(coreR.string.settings_inactivity_reminder_disabled),
            ),
        )
        checkViewDoesNotExist(withText(coreR.string.settings_inactivity_reminder_recurrent))
    }

    @Test
    fun ignoreShortRecords() {
        val name = "Test"

        // Add data
        testUtils.addActivity(name)

        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_ignore_short_records)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(
                coreR.string.settings_ignore_short_records,
                withText(coreR.string.settings_inactivity_reminder_disabled),
            ),
        )

        clickOnSettingsSelectorBesideText(coreR.string.settings_ignore_short_records)
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
        scrollSettingsRecyclerToText(coreR.string.settings_ignore_short_records)
        clickOnSettingsSelectorBesideText(coreR.string.settings_ignore_short_records)
        clickOnViewWithText(coreR.string.duration_dialog_disable)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(
                coreR.string.settings_ignore_short_records,
                withText(coreR.string.settings_inactivity_reminder_disabled),
            ),
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
        scrollSettingsRecyclerToText(coreR.string.settings_use_military_time)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_use_military_time))
        checkViewIsDisplayed(
            settingsSubtitleBesideText(coreR.string.settings_use_military_time, withText("13:00")),
        )

        // Change settings
        clickOnSettingsCheckboxBesideText(coreR.string.settings_use_military_time)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_use_military_time))
        checkViewIsDisplayed(
            settingsSubtitleBesideText(coreR.string.settings_use_military_time, withSubstring("1:00")),
        )

        // Change settings
        clickOnSettingsCheckboxBesideText(coreR.string.settings_use_military_time)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_use_military_time))
        checkViewIsDisplayed(
            settingsSubtitleBesideText(coreR.string.settings_use_military_time, withText("13:00")),
        )
    }

    @Test
    fun durationFormat() {
        val name = "Test"

        fun checkView(matchers: Matcher<View>, text: String) {
            checkViewIsDisplayed(allOf(matchers, hasDescendant(withText(text)), isCompletelyDisplayed()))
        }

        fun checkFormat(timeString: String) {
            NavUtils.openRecordsScreen()
            checkView(allOf(withId(baseR.id.viewRecordItem)), timeString)
            NavUtils.openStatisticsScreen()
            checkView(
                allOf(
                    hasDescendant(withText(name)),
                    withId(baseR.id.viewStatisticsItem),
                ),
                timeString,
            )
            tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }
            NavUtils.fixToCurrentDate()
            tryAction {
                checkViewIsDisplayed(
                    allOf(
                        withId(statisticsDetailR.id.containerStatisticsDetailCard),
                        hasDescendant(withText(R.string.statistics_detail_total_duration)),
                        hasDescendant(withText(timeString)),
                    ),
                )
            }
            pressBack()
        }

        // Add data
        val timeEnded = System.currentTimeMillis()
        val timeStarted = timeEnded - TimeUnit.MINUTES.toMillis(75)
        val timeFormat1 = "1$hourString 15$minuteString"
        val timeFormat2 = "%.2f$hourString".format(1.25)
        val timeFormat3 = "75$minuteString"
        testUtils.addActivity(name)
        testUtils.addRecord(name, timeStarted, timeEnded)

        // Check format
        checkFormat(timeFormat1)

        // Check settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(timeFormat1)

        // Change settings
        clickOnSettingsSpinnerBesideText(coreR.string.settings_duration_format)
        clickOnViewWithText(coreR.string.settings_duration_format_proportional)
        settingsSpinnerValueBesideText(
            coreR.string.settings_duration_format_proportional,
            withText(coreR.string.settings_duration_format),
        )
        checkViewIsDisplayed(withText(timeFormat2))
        checkFormat(timeFormat2)

        // Change setting
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(timeFormat2)
        clickOnSettingsSpinnerBesideText(coreR.string.settings_duration_format)
        clickOnViewWithText(coreR.string.minutes)
        settingsSpinnerValueBesideText(
            coreR.string.settings_duration_format_proportional,
            withText(coreR.string.minutes),
        )
        checkViewIsDisplayed(withText(timeFormat3))
        checkFormat(timeFormat3)

        // Change settings back
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(timeFormat3)
        clickOnSettingsSpinnerBesideText(coreR.string.settings_duration_format)
        clickOnViewWithText(coreR.string.hours)
        settingsSpinnerValueBesideText(
            coreR.string.settings_duration_format_proportional,
            withText(coreR.string.hours),
        )
        checkViewIsDisplayed(withText(timeFormat1))
        checkFormat(timeFormat1)
    }

    @Test
    fun keepScreenOn() {
        // Check settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_keep_screen_on)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_keep_screen_on))

        // Change settings
        clickOnSettingsCheckboxBesideText(coreR.string.settings_keep_screen_on)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_keep_screen_on))

        // Change settings
        clickOnSettingsCheckboxBesideText(coreR.string.settings_keep_screen_on)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_keep_screen_on))
    }

    @Test
    fun startTimersByLongClick() {
        val name = "Test"

        // Add data
        runBlocking { prefsInteractor.setAllowMultitasking(false) }
        testUtils.addActivity(name)
        testUtils.addShortcut(name)
        testUtils.addRecord(
            typeName = name,
            timeStarted = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1),
            timeEnded = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1),
        )

        // Check disabled
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_start_timer_by_long_click)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_start_timer_by_long_click))
        // Type
        NavUtils.openRunningRecordsScreen()
        longClickOnVisibleView(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText(name)),
            ),
        )
        checkViewIsDisplayed(withText(R.string.duration_dialog_save))
        pressBack()
        clickOnVisibleView(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText(name)),
            ),
        )
        // Running record
        longClickOnView(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name)),
            ),
        )
        checkViewIsDisplayed(withText(R.string.duration_dialog_save))
        pressBack()
        clickOnView(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name)),
            ),
        )
        checkViewDoesNotExist(withId(R.id.viewRunningRecordItem))
        // Shortcut
        longClickOnView(
            allOf(
                withId(R.id.viewRecordShortcutItem),
                hasDescendant(withText(name)),
            ),
        )
        clickOnView(withText(R.string.cancel))
        clickOnView(
            allOf(
                withId(R.id.viewRecordShortcutItem),
                hasDescendant(withText(name)),
            ),
        )
        clickOnView(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name)),
            ),
        )
        checkViewDoesNotExist(withId(R.id.viewRunningRecordItem))
        // Retroactive
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_retroactive_tracking_mode)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_retroactive_tracking_mode)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_retroactive_tracking_mode))
        NavUtils.openRunningRecordsScreen()
        // Untracked
        clickOnView(withText(R.string.untracked_time_name))
        checkViewDoesNotExist(withText(R.string.duration_dialog_save))
        longClickOnView(withText(R.string.untracked_time_name))
        checkViewIsDisplayed(withText(R.string.duration_dialog_save))
        pressBack()
        // Last record
        clickOnView(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed(),
            ),
        )
        checkViewDoesNotExist(withText(R.string.duration_dialog_save))
        longClickOnView(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(withText(R.string.duration_dialog_save))
        pressBack()
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_retroactive_tracking_mode)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_retroactive_tracking_mode)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_retroactive_tracking_mode))

        // Check enabled
        scrollSettingsRecyclerToText(coreR.string.settings_start_timer_by_long_click)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_start_timer_by_long_click)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_start_timer_by_long_click))
        // Type
        NavUtils.openRunningRecordsScreen()
        clickOnVisibleView(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText(name)),
            ),
        )
        checkViewIsDisplayed(withText(R.string.duration_dialog_save))
        pressBack()
        longClickOnVisibleView(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText(name)),
            ),
        )
        // Running record
        clickOnView(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name)),
            ),
        )
        checkViewIsDisplayed(withText(R.string.duration_dialog_save))
        pressBack()
        longClickOnView(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name)),
            ),
        )
        checkViewDoesNotExist(withId(R.id.viewRunningRecordItem))
        // Shortcut
        clickOnView(
            allOf(
                withId(R.id.viewRecordShortcutItem),
                hasDescendant(withText(name)),
            ),
        )
        clickOnView(withText(R.string.cancel))
        longClickOnView(
            allOf(
                withId(R.id.viewRecordShortcutItem),
                hasDescendant(withText(name)),
            ),
        )
        longClickOnView(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name)),
            ),
        )
        checkViewDoesNotExist(withId(R.id.viewRunningRecordItem))
        // Retroactive
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_retroactive_tracking_mode)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_retroactive_tracking_mode)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_retroactive_tracking_mode))
        NavUtils.openRunningRecordsScreen()
        // Untracked
        longClickOnView(withText(R.string.untracked_time_name))
        checkViewDoesNotExist(withText(R.string.duration_dialog_save))
        clickOnView(withText(R.string.untracked_time_name))
        checkViewIsDisplayed(withText(R.string.duration_dialog_save))
        pressBack()
        // Last record
        longClickOnView(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed(),
            ),
        )
        checkViewDoesNotExist(withText(R.string.duration_dialog_save))
        Thread.sleep(500)
        clickOnView(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(withText(R.string.duration_dialog_save))
        pressBack()
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_retroactive_tracking_mode)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_retroactive_tracking_mode)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_retroactive_tracking_mode))
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
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1),
        )

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_first_day_of_week)
        clickOnSettingsSpinnerBesideText(coreR.string.settings_first_day_of_week)
        if (isTodaySunday) {
            clickOnViewWithText(coreR.string.day_of_week_monday)
        } else {
            clickOnViewWithText(coreR.string.day_of_week_sunday)
        }

        // Check statistics
        NavUtils.openStatisticsScreen()
        clickOnCurrentDate()
        clickOnViewWithText(coreR.string.range_week)
        clickOnView(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed(),
            ),
        )

        // Check detailed statistics
        NavUtils.fixToCurrentDate()
        clickOnCurrentDate()
        clickOnViewWithText(coreR.string.range_week)
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, 1),
                hasSibling(withText("1")),
                isCompletelyDisplayed(),
            ),
        )

        // Check range titles
        var titlePrev = toWeekTitle(
            shift = -1,
            firstDayOfWeek = if (isTodaySunday) DayOfWeek.MONDAY else DayOfWeek.SUNDAY,
        )
        longClickOnCurrentDate()
        clickOnCurrentDate(-1)
        checkWeekTitle(titlePrev)
        pressBack()
        longClickOnCurrentDate()
        clickOnCurrentDate(-1)
        checkWeekTitle(titlePrev)
        longClickOnCurrentDate()

        // Change setting
        NavUtils.openSettingsScreen()
        clickOnSettingsSpinnerBesideText(coreR.string.settings_first_day_of_week)
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
                isCompletelyDisplayed(),
            ),
        )
        clickOnCurrentDate(-1)
        clickOnView(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed(),
            ),
        )

        // Check detailed statistics
        tryAction { clickOnCurrentDate() }
        clickOnViewWithText(coreR.string.range_week)
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, 0),
                hasSibling(withText("0")),
                isCompletelyDisplayed(),
            ),
        )
        clickOnCurrentDate(-1)
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, 1),
                hasSibling(withText("1")),
                isCompletelyDisplayed(),
            ),
        )

        // Check range titles
        titlePrev = toWeekTitle(
            shift = -1,
            firstDayOfWeek = if (isTodaySunday) DayOfWeek.SUNDAY else DayOfWeek.MONDAY,
        )
        longClickOnCurrentDate()
        clickOnCurrentDate(-1)
        checkWeekTitle(titlePrev)
        pressBack()
        longClickOnCurrentDate()
        clickOnCurrentDate(-1)
        checkWeekTitle(titlePrev)
        longClickOnCurrentDate()
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
            timeEnded = timeEndedTimeStamp,
        )

        // Check records
        NavUtils.openRecordsScreen()
        clickOnCurrentDate(-1)
        clickOnCurrentDate(-2)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = startOfDayPreview)
        clickOnCurrentDate(-1)
        checkRecord(
            nameResId = coreR.string.untracked_time_name, timeStart = timeEndedPreview, timeEnd = startOfDayPreview,
        )
        checkRecord(name = name, timeStart = startOfDayPreview, timeEnd = timeEndedPreview)

        // Check statistics
        NavUtils.openStatisticsScreen()
        clickOnCurrentDate(-1)
        clickOnCurrentDate(-2)
        checkStatisticsItem(name = name, hours = 2)
        clickOnCurrentDate(-1)
        checkStatisticsItem(nameResId = coreR.string.untracked_time_name, hours = 22)
        checkStatisticsItem(name = name, hours = 2)

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        NavUtils.fixToCurrentDate()
        clickOnCurrentDate()
        clickOnViewWithText(coreR.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnCurrentDate(-1)
        checkStatisticsDetailRecords(1)
        clickOnCurrentDate(-2)
        checkStatisticsDetailRecords(1)
        clickOnCurrentDate(-3)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Check setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_start_of_day)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(
                coreR.string.settings_start_of_day,
                withText(R.string.settings_inactivity_reminder_disabled),
            ),
        )
        checkViewIsNotDisplayed(settingsButtonBesideText(coreR.string.settings_start_of_day))

        // Change setting to +1
        clickOnSettingsSelectorBesideText(coreR.string.settings_start_of_day)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        startOfDayTimeStamp = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()
        var startOfDayDuration = startOfDayTimeStamp.toDurationPreview()

        // Check new setting
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(coreR.string.settings_start_of_day, withText(startOfDayDuration)),
        )
        checkViewIsDisplayed(
            settingsButtonBesideText(
                coreR.string.settings_start_of_day, hasDescendant(withText(coreR.string.plus_sign)),
            ),
        )

        // Check records
        NavUtils.openRecordsScreen()
        longClickOnCurrentDate()
        clickOnCurrentDate(-1)
        clickOnCurrentDate(-2)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = startOfDayPreview)
        clickOnCurrentDate(-1)
        checkRecord(
            nameResId = coreR.string.untracked_time_name, timeStart = timeEndedPreview, timeEnd = startOfDayPreview,
        )
        checkRecord(name = name, timeStart = startOfDayPreview, timeEnd = timeEndedPreview)

        // Check statistics
        NavUtils.openStatisticsScreen()
        longClickOnCurrentDate()
        clickOnCurrentDate(-1)
        clickOnCurrentDate(-2)
        checkStatisticsItem(name = name, hours = 3)
        clickOnCurrentDate(-1)
        checkStatisticsItem(nameResId = coreR.string.untracked_time_name, hours = 23)
        checkStatisticsItem(name = name, hours = 1)

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnCurrentSelectedDate()
        clickOnViewWithText(coreR.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnCurrentDate(-1)
        checkStatisticsDetailRecords(1)
        clickOnCurrentDate(-2)
        checkStatisticsDetailRecords(1)
        clickOnCurrentDate(-3)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Change setting to -1
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_start_of_day)
        clickOnSettingsButtonBesideText(coreR.string.settings_start_of_day)

        // Check new setting
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(coreR.string.settings_start_of_day, withText(startOfDayDuration)),
        )
        checkViewIsDisplayed(
            settingsButtonBesideText(
                coreR.string.settings_start_of_day, hasDescendant(withText(coreR.string.minus_sign)),
            ),
        )

        startOfDayTimeStamp = calendar.timeInMillis - TimeUnit.HOURS.toMillis(1)
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        // Check records
        NavUtils.openRecordsScreen()
        longClickOnCurrentDate()
        clickOnCurrentDate(-1)
        clickOnCurrentDate(-2)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = startOfDayPreview)
        clickOnCurrentDate(-1)
        checkRecord(
            nameResId = coreR.string.untracked_time_name, timeStart = timeEndedPreview, timeEnd = startOfDayPreview,
        )
        checkRecord(name = name, timeStart = startOfDayPreview, timeEnd = timeEndedPreview)

        // Check statistics
        NavUtils.openStatisticsScreen()
        longClickOnCurrentDate()
        clickOnCurrentDate(-1)
        clickOnCurrentDate(-2)
        checkStatisticsItem(name = name, hours = 1)
        clickOnCurrentDate(-1)
        checkStatisticsItem(nameResId = coreR.string.untracked_time_name, hours = 21)
        checkStatisticsItem(name = name, hours = 3)

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        NavUtils.fixToCurrentDate()
        clickOnCurrentDate()
        clickOnViewWithText(coreR.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnCurrentDate(-1)
        checkStatisticsDetailRecords(1)
        clickOnCurrentDate(-2)
        checkStatisticsDetailRecords(1)
        clickOnCurrentDate(-3)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Change setting to +2, record will be shifted out from one day
        startOfDayTimeStamp = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2)
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()
        startOfDayDuration = startOfDayTimeStamp.toDurationPreview()
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_start_of_day)
        clickOnSettingsSelectorBesideText(coreR.string.settings_start_of_day)
        longClickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard2)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(coreR.string.settings_start_of_day, withText(startOfDayDuration)),
        )
        checkViewIsDisplayed(
            settingsButtonBesideText(
                coreR.string.settings_start_of_day, hasDescendant(withText(coreR.string.minus_sign)),
            ),
        )
        clickOnSettingsButtonBesideText(coreR.string.settings_start_of_day)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(coreR.string.settings_start_of_day, withText(startOfDayDuration)),
        )

        // Check records
        NavUtils.openRecordsScreen()
        longClickOnCurrentDate()
        clickOnCurrentDate(-1)
        clickOnCurrentDate(-2)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = timeEndedPreview)
        clickOnCurrentDate(-1)
        checkRecord(
            nameResId = coreR.string.untracked_time_name, timeStart = startOfDayPreview, timeEnd = startOfDayPreview,
        )

        // Check statistics
        NavUtils.openStatisticsScreen()
        longClickOnCurrentDate()
        clickOnCurrentDate(-1)
        clickOnCurrentDate(-2)
        checkStatisticsItem(name = name, hours = 4)
        clickOnCurrentDate(-1)
        checkStatisticsItem(nameResId = coreR.string.untracked_time_name, hours = 24)

        // Check detailed statistics
        clickOnCurrentDate(-2)
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        tryAction { clickOnCurrentDate() }
        clickOnViewWithText(coreR.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnCurrentDate(-1)
        checkStatisticsDetailRecords(0)
        clickOnCurrentDate(-2)
        checkStatisticsDetailRecords(1)
        clickOnCurrentDate(-3)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Change setting to 0
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_start_of_day)
        clickOnSettingsSelectorBesideText(coreR.string.settings_start_of_day)
        clickOnViewWithText(coreR.string.duration_dialog_disable)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(
                coreR.string.settings_start_of_day,
                withText(R.string.settings_inactivity_reminder_disabled),
            ),
        )
        checkViewIsNotDisplayed(settingsButtonBesideText(coreR.string.settings_start_of_day))
    }

    @Test
    fun showRecordTagSelection() {
        val name = "TypeName"
        val tag = "TagName"
        val tagGeneral = "TagGeneral"
        val fullName = "$name - $tag"

        // Add data
        testUtils.addActivity(name)

        // Started right away
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)))) }

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_selection)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection))

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
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_selection)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection))

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
        val fullName2 = "$name - $tag, $tagGeneral"

        // Add data
        testUtils.addActivity(name)

        // Started right away
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_selection)
        checkViewDoesNotExist(withText(coreR.string.settings_show_record_tag_close_hint))

        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection)
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_close_hint)
        checkViewIsDisplayed(withText(coreR.string.settings_show_record_tag_close_hint))
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_record_tag_close_hint))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_record_tag_close_hint)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_record_tag_close_hint))

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
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_close_hint)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_record_tag_close_hint))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_record_tag_close_hint)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_record_tag_close_hint))

        // Start with several tags
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        clickOnView(withText(tag))
        clickOnView(withText(tagGeneral))
        clickOnViewWithText(coreR.string.duration_dialog_save)
        tryAction { clickOnView(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(fullName2)))) }

        // Change setting
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_selection)
        checkViewIsDisplayed(withText(coreR.string.settings_show_record_tag_close_hint))

        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection)
        checkViewDoesNotExist(withText(coreR.string.settings_show_record_tag_close_hint))
    }

    @Test
    fun recordTagSelectionExcludeActivities() {
        val name = "TypeName"
        val tagGeneral = "TagGeneral"

        // Add data
        testUtils.addActivity(name)

        // Started right away
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)))) }

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_selection)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection)

        // No tags - started right away
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }

        // Add tag
        testUtils.addRecordTag(tagGeneral)

        // Has a tag but not excluded - show dialog
        clickOnViewWithText(name)
        tryAction { checkViewIsDisplayed(withText(coreR.string.change_record_untagged)) }
        checkViewIsDisplayed(withText(tagGeneral))
        pressBack()

        // Change setting
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_selection)
        clickOnSettingsButtonBesideText(coreR.string.settings_show_record_tag_selection)
        Thread.sleep(1000)
        clickOnViewWithText(name)
        clickOnViewWithText(coreR.string.duration_dialog_save)

        // Has a tag but excluded - no dialog
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }
    }

    @Test
    fun recordTagSelectionCloseExcludeActivities() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val tag1 = "TagName1"
        val tag2 = "TagName2"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addRecordTag(tag1)
        testUtils.addRecordTag(tag2)

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_selection)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection))

        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_close_hint)
        checkViewIsDisplayed(withText(coreR.string.settings_show_record_tag_close_hint))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_record_tag_close_hint)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_record_tag_close_hint))

        // Exclude one
        clickOnSettingsButtonBesideText(coreR.string.settings_show_record_tag_close_hint)
        Thread.sleep(1000)
        clickOnViewWithText(name1)
        clickOnViewWithText(coreR.string.duration_dialog_save)

        // Excluded entry should not close after one tag
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name1)
        clickOnView(withText(tag1))
        tryAction { checkViewIsDisplayed(withText(tag2)) }
        pressBack()

        // Not excluded should close after one tag
        clickOnViewWithText(name2)
        checkViewIsNotDisplayed(withText(R.string.duration_dialog_save))
        clickOnView(withText(tag1))
        tryAction { checkViewDoesNotExist(withText(tag2)) }
    }

    @Test
    fun recordTagSelectionFromOtherActivity() {
        val type1 = "Type1"
        val type2 = "Type2"
        val tag1 = "Tag1"
        val tag2 = "Tag2"

        // Add activities
        runBlocking { prefsInteractor.setShowRecordTagSelection(true) }
        testUtils.addActivity(type1)
        testUtils.addActivity(type2)
        testUtils.addRecordTag(tag1, type1)
        Thread.sleep(1000)

        // Not other tags - not shown
        tryAction { clickOnViewWithText(type1) }
        checkViewDoesNotExist(withText(R.string.types_filter_show_all))
        pressBack()

        // Add other tag
        testUtils.addRecordTag(tag2, type2)

        // Check
        clickOnViewWithText(type1)
        checkViewIsDisplayed(withText(tag1))
        checkViewDoesNotExist(withText(tag2))
        checkViewIsDisplayed(withText(R.string.types_filter_show_all))

        // Show all
        clickOnViewWithText(R.string.types_filter_show_all)
        checkViewIsDisplayed(withText(tag1))
        checkViewIsDisplayed(withText(tag2))

        // Select
        clickOnViewWithText(tag2)
        clickOnViewWithText(R.string.change_record_save)

        // Check
        checkViewIsDisplayed(withText("$type1 - $tag2"))

        // Check that is now assignable
        longClickOnView(withText("$type1 - $tag2"))
        clickOnViewWithText(coreR.string.change_record_tag_field)
        checkViewIsDisplayed(withText(tag1))
        checkViewIsDisplayed(withText(tag2))
        checkViewDoesNotExist(withText(R.string.types_filter_show_all))
    }

    @Test
    fun commentSelection() {
        val name = "TypeName"
        val comment = "comment"

        // Add data
        testUtils.addActivity(name)

        // Started right away
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)))) }

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_show_comment_input)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_comment_input))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_comment_input)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_comment_input))

        // Dialog shown
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { checkViewIsDisplayed(withId(tagSelectionR.id.inputCommentField)) }
        pressBack()

        // Start without comment
        clickOnViewWithText(name)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        tryAction { checkViewIsNotDisplayed(withId(R.id.tvRunningRecordItemComment)) }
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }

        // Start with comment
        clickOnViewWithText(name)
        tryAction { typeTextIntoView(tagSelectionR.id.etCommentItemField, comment) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(comment))) }

        // Change setting
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_comment_input)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_comment_input))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_comment_input)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_comment_input))

        // No dialog
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }
    }

    @Test
    fun commentSelectionExcludeActivities() {
        val name = "TypeName"

        // Add data
        testUtils.addActivity(name)

        // Started right away
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)))) }

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_show_comment_input)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_comment_input)

        // Not excluded - show dialog
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { checkViewIsDisplayed(withId(tagSelectionR.id.inputCommentField)) }
        pressBack()

        // Change setting
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_comment_input)
        clickOnSettingsButtonBesideText(coreR.string.settings_show_comment_input)
        Thread.sleep(1000)
        clickOnViewWithText(name)
        clickOnViewWithText(coreR.string.duration_dialog_save)

        // Excluded - no dialog
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }
    }

    @Test
    fun commentSelectionSuggestions() {
        fun checkHint(textResId: Int) {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.tvHintItemText),
                    withText(textResId),
                ),
            )
        }

        val nameNoComments = "Name1"
        val nameComment = "Name2"
        val nameComments = "Name3"
        val comment1 = "Comment1"
        val comment2 = "Comment2"
        val comment3 = "Comment3"
        val favComment1 = "favourite comment1"

        // Add data
        testUtils.addActivity(nameNoComments)
        testUtils.addActivity(nameComment)
        testUtils.addActivity(nameComments)
        testUtils.addRecord(nameNoComments)
        testUtils.addRecord(nameComment, comment = comment1)
        testUtils.addRecord(nameComments, comment = comment2)
        testUtils.addRecord(nameComments, comment = comment3)

        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_show_comment_input)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_comment_input)

        // Has no data
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(nameNoComments)
        tryAction { checkViewIsDisplayed(withId(tagSelectionR.id.inputCommentField)) }
        checkViewDoesNotExist(withText(coreR.string.change_record_similar_comments_hint))
        checkViewDoesNotExist(withText(coreR.string.change_record_last_comments_hint))
        checkViewDoesNotExist(withText(coreR.string.change_record_favourite_comments_hint))
        pressBack()

        // Has data
        clickOnViewWithText(nameComment)
        checkViewDoesNotExist(withText(coreR.string.change_record_similar_comments_hint))
        checkViewIsDisplayed(withText(coreR.string.change_record_last_comments_hint))
        checkViewDoesNotExist(withText(coreR.string.change_record_favourite_comments_hint))
        checkViewIsDisplayed(withText(comment1))
        checkViewDoesNotExist(withText(comment2))
        checkViewDoesNotExist(withText(comment3))
        pressBack()

        clickOnViewWithText(nameComments)
        checkViewDoesNotExist(withText(coreR.string.change_record_similar_comments_hint))
        checkViewIsDisplayed(withText(coreR.string.change_record_last_comments_hint))
        checkViewDoesNotExist(withText(coreR.string.change_record_favourite_comments_hint))
        checkViewDoesNotExist(withText(comment1))
        checkViewIsDisplayed(withText(comment2))
        checkViewIsDisplayed(withText(comment3))

        // Select last comment
        clickOnViewWithText(comment2)
        checkViewIsDisplayed(allOf(withId(tagSelectionR.id.etCommentItemField), withText(comment2)))
        pressBack()

        // Has favourite comments
        testUtils.addFavouriteComment(favComment1)
        clickOnViewWithText(nameComments)
        checkViewDoesNotExist(withText(coreR.string.change_record_similar_comments_hint))
        checkHint(coreR.string.change_record_last_comments_hint)
        checkHint(coreR.string.change_record_favourite_comments_hint)
        checkViewDoesNotExist(withText(comment1))
        checkViewIsDisplayed(withText(comment2))
        checkViewIsDisplayed(withText(comment3))
        checkViewIsDisplayed(withText(favComment1))

        // Select fav comment
        clickOnViewWithText(favComment1)
        checkViewIsDisplayed(allOf(withId(tagSelectionR.id.etCommentItemField), withText(favComment1)))
        pressBack()

        // Search
        clickOnViewWithText(nameNoComments)
        typeTextIntoView(tagSelectionR.id.etCommentItemField, "comm")
        Thread.sleep(1000)
        closeSoftKeyboard()
        checkHint(coreR.string.change_record_similar_comments_hint)
        checkViewDoesNotExist(withText(coreR.string.change_record_last_comments_hint))
        checkHint(coreR.string.change_record_favourite_comments_hint)
        checkViewIsDisplayed(withText(comment1))
        checkViewIsDisplayed(withText(comment2))
        checkViewIsDisplayed(withText(comment3))
        checkViewIsDisplayed(withText(favComment1))

        typeTextIntoView(changeRecordR.id.etCommentItemField, "1")
        Thread.sleep(1000)
        closeSoftKeyboard()
        checkHint(coreR.string.change_record_similar_comments_hint)
        checkViewDoesNotExist(withText(coreR.string.change_record_last_comments_hint))
        checkHint(coreR.string.change_record_favourite_comments_hint)
        checkViewIsDisplayed(withText(comment1))
        checkViewDoesNotExist(withText(comment2))
        checkViewDoesNotExist(withText(comment3))
        checkViewIsDisplayed(withText(favComment1))
        pressBack()
    }

    @Test
    fun tagAndCommentSelection() {
        val name = "TypeName"
        val tag = "TagGeneral"
        val comment = "comment"
        val fullName = "$name - $tag"

        // Add data
        testUtils.addActivity(name)
        testUtils.addRecordTag(tag)

        // Started right away
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)))) }

        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_selection)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection)
        scrollSettingsRecyclerToText(coreR.string.settings_show_comment_input)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_comment_input)

        // Dialog shown
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { checkViewIsDisplayed(withText(tag)) }
        tryAction { checkViewIsDisplayed(withId(tagSelectionR.id.inputCommentField)) }
        pressBack()

        // Start untagged, no comment
        clickOnViewWithText(name)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        tryAction { checkViewIsNotDisplayed(withId(R.id.tvRunningRecordItemComment)) }
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }

        // Start untagged, with comment
        clickOnViewWithText(name)
        tryAction { typeTextIntoView(tagSelectionR.id.etCommentItemField, comment) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        tryAction { checkViewIsDisplayed(allOf(withId(R.id.tvRunningRecordItemComment), withText(comment))) }
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }

        // Start tagged, without comment
        clickOnViewWithText(name)
        clickOnViewWithText(tag)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        tryAction { checkViewIsNotDisplayed(withId(R.id.tvRunningRecordItemComment)) }
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(fullName))) }

        // Start tagged, with comment
        clickOnViewWithText(name)
        tryAction { typeTextIntoView(tagSelectionR.id.etCommentItemField, comment) }
        clickOnViewWithText(tag)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        tryAction { checkViewIsDisplayed(allOf(withId(R.id.tvRunningRecordItemComment), withText(comment))) }
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(fullName))) }

        // Exclude for tags
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_selection)
        clickOnSettingsButtonBesideText(coreR.string.settings_show_record_tag_selection)
        Thread.sleep(1000)
        clickOnViewWithText(name)
        clickOnViewWithText(coreR.string.duration_dialog_save)

        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { checkViewDoesNotExist(withText(tag)) }
        tryAction { checkViewIsDisplayed(withId(tagSelectionR.id.inputCommentField)) }
        pressBack()

        // Exclude for comments
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_selection)
        clickOnSettingsButtonBesideText(coreR.string.settings_show_record_tag_selection)
        Thread.sleep(1000)
        clickOnViewWithText(name)
        clickOnViewWithText(coreR.string.duration_dialog_save)

        scrollSettingsRecyclerToText(coreR.string.settings_show_comment_input)
        clickOnSettingsButtonBesideText(coreR.string.settings_show_comment_input)
        Thread.sleep(1000)
        clickOnViewWithText(name)
        clickOnViewWithText(coreR.string.duration_dialog_save)

        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { checkViewIsDisplayed(withText(tag)) }
        tryAction { checkViewDoesNotExist(withId(tagSelectionR.id.inputCommentField)) }
        pressBack()

        // Exclude both
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_selection)
        clickOnSettingsButtonBesideText(coreR.string.settings_show_record_tag_selection)
        Thread.sleep(1000)
        clickOnViewWithText(name)
        clickOnViewWithText(coreR.string.duration_dialog_save)

        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)))) }

        // Change setting
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_selection)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection)
        scrollSettingsRecyclerToText(coreR.string.settings_show_comment_input)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_comment_input)

        // No dialog
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }
    }

    @Test
    fun csvExportSettings() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsExportImport()
        scrollSettingsRecyclerToText(coreR.string.settings_export_csv)
        clickOnSettingsRecyclerText(coreR.string.settings_export_csv)

        // View is set up
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.etCsvExportSettingsFileName),
                withText("stt_records_{date}.csv"),
            ),
        )
        val currentTime = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            setToStartOfDay()
        }.timeInMillis
        var timeStarted = currentTime.formatDateTimeYear()
        var timeEnded = (currentTime + TimeUnit.DAYS.toMillis(1)).formatDateTimeYear()
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))

        var calendar = Calendar.getInstance().apply {
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
        onView(withClassName(equalTo(CustomDatePicker::class.java.name)))
            .perform(setDate(year, month + 1, day))
        clickOnView(
            allOf(
                isDescendantOfA(withId(dialogsR.id.tabsDateTimeDialog)), withText(coreR.string.date_time_dialog_time),
            ),
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
        timeStarted = timeStartedTimestamp.formatDateTimeYear()

        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))

        // Set time ended
        clickOnViewWithId(dialogsR.id.tvCsvExportSettingsTimeEnded)
        onView(withClassName(equalTo(CustomDatePicker::class.java.name)))
            .perform(setDate(year, month + 1, day))
        clickOnView(
            allOf(
                isDescendantOfA(withId(dialogsR.id.tabsDateTimeDialog)), withText(coreR.string.date_time_dialog_time),
            ),
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
        timeEnded = timeEndedTimestamp.formatDateTimeYear()

        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))

        // Check ranges
        clickOnViewWithText(R.string.title_today)
        calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            setToStartOfDay()
        }
        timeStarted = calendar.timeInMillis.formatDateTimeYear()
        timeEnded = (calendar.timeInMillis + TimeUnit.DAYS.toMillis(1)).formatDateTimeYear()
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))

        clickOnViewWithText(R.string.title_this_week)
        calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            setToStartOfDay()
            setWeekToFirstDay()
        }
        timeStarted = calendar.timeInMillis.formatDateTimeYear()
        timeEnded = (calendar.timeInMillis + TimeUnit.DAYS.toMillis(7)).formatDateTimeYear()
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))

        clickOnViewWithText(R.string.title_this_month)
        calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            setToStartOfDay()
            set(Calendar.DAY_OF_MONTH, 1)
        }
        timeStarted = calendar.timeInMillis.formatDateTimeYear()
        calendar.apply {
            add(Calendar.MONTH, 1)
        }
        timeEnded = calendar.timeInMillis.formatDateTimeYear()
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))

        clickOnViewWithText(R.string.title_this_year)
        calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            setToStartOfDay()
            set(Calendar.DAY_OF_YEAR, 1)
        }
        timeStarted = calendar.timeInMillis.formatDateTimeYear()
        calendar.apply {
            add(Calendar.YEAR, 1)
        }
        timeEnded = calendar.timeInMillis.formatDateTimeYear()
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))

        clickOnViewWithText(R.string.range_overall)
        calendar = Calendar.getInstance().apply {
            timeInMillis = 0
        }
        timeStarted = calendar.timeInMillis.formatDateTimeYear()
        calendar.apply {
            timeInMillis = System.currentTimeMillis()
        }
        timeEnded = calendar.timeInMillis.formatDateTimeYear()
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))

        clickOnViewWithText(getQuantityString(R.plurals.range_last, 7, 7))
        calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            setToStartOfDay()
        }
        timeStarted = (calendar.timeInMillis - TimeUnit.DAYS.toMillis(6)).formatDateTimeYear()
        timeEnded = (calendar.timeInMillis + TimeUnit.DAYS.toMillis(1)).formatDateTimeYear()
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))

        clickOnViewWithText(R.string.range_custom)
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))

        // Selected filter saved
        pressBack()
        clickOnSettingsRecyclerText(coreR.string.settings_export_csv)
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))
    }

    @Test
    fun icsExportSettings() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsExportImport()
        scrollSettingsRecyclerToText(coreR.string.settings_backup_options)
        clickOnSettingsRecyclerText(coreR.string.settings_backup_options)
        clickOnViewWithText(coreR.string.settings_export_ics)

        // View is set up
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.etCsvExportSettingsFileName),
                withText("stt_events_{date}.ics"),
            ),
        )
        val currentTime = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            setToStartOfDay()
        }.timeInMillis
        val timeStarted = currentTime.formatDateTimeYear()
        val timeEnded = (currentTime + TimeUnit.DAYS.toMillis(1)).formatDateTimeYear()
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))
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
        checkViewIsDisplayed(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_show_records_calendar)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_records_calendar))
        checkViewDoesNotExist(withText(coreR.string.settings_reverse_order_in_calendar))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_records_calendar)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_records_calendar))

        // Record is not shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withId(recordsR.id.viewRecordsCalendar), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))

        // Check reverse order
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_reverse_order_in_calendar)
        checkViewIsDisplayed(settingsCheckboxBesideText(coreR.string.settings_reverse_order_in_calendar))
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_reverse_order_in_calendar))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_reverse_order_in_calendar)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_reverse_order_in_calendar))
        NavUtils.openRecordsScreen()

        // Change setting
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_records_calendar)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_records_calendar))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_records_calendar)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_records_calendar))
        checkViewDoesNotExist(withText(coreR.string.settings_reverse_order_in_calendar))

        // Record is shown
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withId(recordsR.id.viewRecordsCalendar), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
    }

    @Test
    fun daysInCalendar() {
        fun mapTitle(
            shiftStart: Int,
            shiftEnd: Int,
        ): Pair<String, String> {
            val start = timeMapper.toDayDateTimestamp(shiftStart, 0).let {
                calendar.timeInMillis = it
                calendar.get(Calendar.DAY_OF_MONTH).toString().padDuration()
            }
            val end = timeMapper.toDayDateTimestamp(shiftEnd, 0).let {
                calendar.timeInMillis = it
                calendar.get(Calendar.DAY_OF_MONTH).toString().padDuration()
            }
            return start to end
        }

        fun checkCalendarTitle(
            position: Int,
            shiftStart: Int,
            shiftEnd: Int,
        ) {
            val title = mapTitle(shiftStart, shiftEnd)
            checkViewIsDisplayed(
                allOf(
                    dateSelectorMatcher(position),
                    withId(R.id.containerDateSelectorRange),
                    hasDescendant(withText(title.first)),
                    hasDescendant(withText(title.second)),
                ),
            )
        }

        // Disabled
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                dateSelectorMatcher(0),
                withId(R.id.containerDateSelectorDay),
            ),
        )

        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_show_records_calendar)
        checkViewDoesNotExist(withText(coreR.string.settings_days_in_calendar))

        // Change setting
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_records_calendar)
        scrollSettingsRecyclerToText(coreR.string.settings_days_in_calendar)
        checkViewIsDisplayed(withText(coreR.string.settings_days_in_calendar))

        // One day
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                dateSelectorMatcher(0),
                withId(R.id.containerDateSelectorDay),
            ),
        )

        // Three days
        NavUtils.openSettingsScreen()
        clickOnSettingsSpinnerBesideText(coreR.string.settings_days_in_calendar)
        clickOnViewWithText("3")
        NavUtils.openRecordsScreen()

        checkCalendarTitle(0, -2, 0)
        clickOnCurrentDate(-1)
        checkCalendarTitle(-1, -5, -3)
        clickOnCurrentDate(0)
        clickOnCurrentDate(1)
        checkCalendarTitle(1, 1, 3)
        clickOnCurrentDate(0)

        // Five days
        NavUtils.openSettingsScreen()
        clickOnSettingsSpinnerBesideText(coreR.string.settings_days_in_calendar)
        clickOnViewWithText("5")
        NavUtils.openRecordsScreen()

        checkCalendarTitle(0, -4, 0)
        clickOnCurrentDate(-1)
        checkCalendarTitle(-1, -9, -5)
        clickOnCurrentDate(0)
        clickOnCurrentDate(1)
        checkCalendarTitle(1, 1, 5)
        clickOnCurrentDate(0)

        // Seven days
        NavUtils.openSettingsScreen()
        clickOnSettingsSpinnerBesideText(coreR.string.settings_days_in_calendar)
        clickOnViewWithText("7")
        NavUtils.openRecordsScreen()

        checkCalendarTitle(0, -6, 0)
        clickOnCurrentDate(-1)
        checkCalendarTitle(-1, -13, -7)
        clickOnCurrentDate(0)
        clickOnCurrentDate(1)
        checkCalendarTitle(1, 1, 7)
        clickOnCurrentDate(0)

        // Disable
        NavUtils.openSettingsScreen()
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_records_calendar)
        checkViewDoesNotExist(withText(coreR.string.settings_days_in_calendar))
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                dateSelectorMatcher(0),
                withId(R.id.containerDateSelectorDay),
            ),
        )
    }

    @Test
    fun keepStatisticsRange() {
        val name = "Test"

        // Add activity
        testUtils.addActivity(name = name)
        testUtils.addRecord(name)

        // Check range not transferred
        NavUtils.openStatisticsScreen()
        clickOnCurrentDate()
        clickOnViewWithText(coreR.string.range_week)
        checkViewIsDisplayed(
            allOf(
                dateSelectorMatcher(0),
                withId(R.id.containerDateSelectorRange),
            ),
        )
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(
                dateSelectorMatcher(0),
                withId(R.id.containerDateSelectorDay),
            ),
        )
        pressBack()

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_keep_statistics_range)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_keep_statistics_range))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_keep_statistics_range)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_keep_statistics_range))

        // Check range transfer
        NavUtils.openStatisticsScreen()
        checkViewIsDisplayed(
            allOf(
                dateSelectorMatcher(0),
                withId(R.id.containerDateSelectorRange),
            ),
        )
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(
                dateSelectorMatcher(0),
                withId(R.id.containerDateSelectorRange),
            ),
        )
        pressBack()
    }

    @Test
    fun automatedTracking() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_automated_tracking)
        clickOnSettingsButtonBesideText(coreR.string.settings_automated_tracking)
        checkViewIsDisplayed(
            allOf(withId(dialogsR.id.tvHelpDialogTitle), withText(coreR.string.settings_automated_tracking)),
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
        scrollSettingsRecyclerToText(coreR.string.settings_automated_tracking_send_events)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_automated_tracking_send_events))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_automated_tracking_send_events)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_automated_tracking_send_events))

        // Start stop activity
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }

        // Change setting
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_automated_tracking_send_events)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_automated_tracking_send_events))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_automated_tracking_send_events)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_automated_tracking_send_events))

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
            checkViewIsDisplayed(withText(R.string.running_records_add_type))
            checkViewDoesNotExist(withText(R.string.running_records_add_filter))
            checkViewDoesNotExist(withText(name))
        }

        // Check settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(R.string.settings_show_activity_filters)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(R.string.settings_show_activity_filters))
        checkViewDoesNotExist(withText(R.string.settings_allow_multiple_activity_filters))
        checkViewDoesNotExist(withText(R.string.settings_show_categories_as_predefined_filters))

        // Change setting
        clickOnSettingsCheckboxBesideText(R.string.settings_show_activity_filters)
        checkCheckboxIsChecked(settingsCheckboxBesideText(R.string.settings_show_activity_filters))
        scrollSettingsRecyclerToText(R.string.settings_allow_multiple_activity_filters)
        checkViewIsDisplayed(withText(R.string.settings_allow_multiple_activity_filters))
        checkCheckboxIsChecked(settingsCheckboxBesideText(R.string.settings_allow_multiple_activity_filters))
        scrollSettingsRecyclerToText(R.string.settings_show_categories_as_predefined_filters)
        checkViewIsDisplayed(withText(R.string.settings_show_categories_as_predefined_filters))
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(R.string.settings_show_categories_as_predefined_filters))

        // Check allow multiple
        clickOnSettingsCheckboxBesideText(R.string.settings_allow_multiple_activity_filters)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(R.string.settings_allow_multiple_activity_filters))
        clickOnSettingsCheckboxBesideText(R.string.settings_allow_multiple_activity_filters)
        checkCheckboxIsChecked(settingsCheckboxBesideText(R.string.settings_allow_multiple_activity_filters))

        // Check categories as filters
        clickOnSettingsCheckboxBesideText(R.string.settings_show_categories_as_predefined_filters)
        checkCheckboxIsChecked(settingsCheckboxBesideText(R.string.settings_show_categories_as_predefined_filters))
        clickOnSettingsCheckboxBesideText(R.string.settings_show_categories_as_predefined_filters)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(R.string.settings_show_categories_as_predefined_filters))

        // Filters shown
        NavUtils.openRunningRecordsScreen()
        checkViewIsDisplayed(withText(R.string.running_records_add_filter))
        checkViewIsDisplayed(withText(name))
    }

    @Test
    fun showRepeat() {
        val name = "ActivityFilter"

        // Add data
        testUtils.addActivity(name)

        // Check settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_show_repeat_button)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_repeat_button))
        checkViewDoesNotExist(withText(coreR.string.settings_repeat_button_type))

        // Check not visible
        NavUtils.openRunningRecordsScreen()
        tryAction {
            checkViewDoesNotExist(
                allOf(
                    withId(R.id.viewRecordTypeItem),
                    hasDescendant(withText(R.string.running_records_repeat)),
                ),
            )
        }

        // Change setting
        NavUtils.openSettingsScreen()
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_repeat_button)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_repeat_button))
        scrollSettingsRecyclerToText(coreR.string.settings_repeat_button_type)
        checkViewIsDisplayed(withText(coreR.string.settings_repeat_button_type))

        // Check not visible
        NavUtils.openRunningRecordsScreen()
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRecordTypeItem),
                    hasDescendant(withText(R.string.running_records_repeat)),
                ),
            )
        }
    }

    @Test
    fun repeatType() {
        val type1 = "type1"
        val type2 = "type2"
        val current = System.currentTimeMillis()

        // Add data
        testUtils.addActivity(type1)
        testUtils.addActivity(type2)
        testUtils.addRecord(
            typeName = type1,
            timeStarted = current,
            timeEnded = current,
        )
        testUtils.addRecord(
            typeName = type2,
            timeStarted = current - TimeUnit.HOURS.toMillis(1),
            timeEnded = current - TimeUnit.HOURS.toMillis(1),
        )

        // Check
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_show_repeat_button)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_repeat_button)
        scrollSettingsRecyclerToText(coreR.string.settings_repeat_button_type)
        checkViewIsDisplayed(
            settingsSpinnerValueBesideText(
                coreR.string.settings_repeat_button_type,
                withText(coreR.string.settings_repeat_last_record),
            ),
        )
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(withText(type1), isCompletelyDisplayed()))
        tryAction {
            checkViewIsDisplayed(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(type1))))
        }

        // Change
        NavUtils.openSettingsScreen()
        clickOnSettingsSpinnerBesideText(coreR.string.settings_repeat_button_type)
        clickOnViewWithText(coreR.string.settings_repeat_one_before_last)
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(withText(type2), isCompletelyDisplayed()))
        tryAction {
            checkViewIsDisplayed(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(type2))))
        }
    }

    @Test
    fun pomodoroMode() {
        val name = "ActivityFilter"

        // Add data
        testUtils.addActivity(name)

        // Check setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_enable_pomodoro_mode)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_enable_pomodoro_mode))

        // Visible
        NavUtils.openRunningRecordsScreen()
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRecordTypeItem),
                    hasDescendant(withText(R.string.running_records_pomodoro)),
                ),
            )
        }

        // Change
        NavUtils.openSettingsScreen()
        clickOnSettingsCheckboxBesideText(coreR.string.settings_enable_pomodoro_mode)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_enable_pomodoro_mode))
        NavUtils.openRunningRecordsScreen()
        tryAction {
            checkViewDoesNotExist(
                allOf(
                    withId(R.id.viewRecordTypeItem),
                    hasDescendant(withText(R.string.running_records_pomodoro)),
                ),
            )
        }

        // Change
        NavUtils.openSettingsScreen()
        clickOnSettingsCheckboxBesideText(coreR.string.settings_enable_pomodoro_mode)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_enable_pomodoro_mode))
        NavUtils.openRunningRecordsScreen()
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRecordTypeItem),
                    hasDescendant(withText(R.string.running_records_pomodoro)),
                ),
            )
        }
    }

    @Test
    fun pomodoroAutostartActivities() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val name3 = "TypeName3"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_enable_pomodoro_mode)
        clickOnSettingsButtonBesideText(coreR.string.settings_enable_pomodoro_mode)
        Thread.sleep(1000)
        clickOnViewWithText(name1)
        clickOnViewWithText(name2)
        clickOnViewWithText(coreR.string.duration_dialog_save)

        fun checkRunningMark(isVisible: Boolean) {
            val name = getString(R.string.running_records_pomodoro)
            tryAction {
                if (isVisible) {
                    GoalsTestUtils.checkTypeMark(name, isVisible = false)
                } else {
                    GoalsTestUtils.checkNoTypeMark(name)
                }
            }
        }

        // Not running
        NavUtils.openRunningRecordsScreen()
        checkRunningMark(false)

        // Start not selected - not running
        clickOnViewWithText(name3)
        checkRunningMark(false)

        // Start selected - running
        clickOnViewWithText(name1)
        checkRunningMark(true)
        clickOnViewWithText(name2)
        checkRunningMark(true)

        // Stop one - still running
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name3)))
        checkRunningMark(true)

        // Stop auto started
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name1)))
        checkRunningMark(true)

        // Stop last auto started - not running
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name2)))
        checkRunningMark(false)

        // Delete auto started - not running
        clickOnView(allOf(withText(name1), isCompletelyDisplayed()))
        checkRunningMark(true)
        longClickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name1)))
        clickOnViewWithText(R.string.archive_dialog_delete)
        checkRunningMark(false)

        // Change to not auto started - not running
        clickOnView(allOf(withText(name1), isCompletelyDisplayed()))
        checkRunningMark(true)
        longClickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name1)))
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name3))
        clickOnViewWithText(R.string.change_record_save)
        checkRunningMark(false)
    }

    @Test
    fun searchOnMain() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val name3 = "TypeName3"
        val name4 = "LoWeRcAsE"
        val filter1 = "Filter1"
        val filter2 = "Filter2"

        // Add data
        runBlocking { prefsInteractor.setShowActivityFilters(true) }
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)
        testUtils.addActivity(name4)
        testUtils.addActivityFilter(filter1)
        testUtils.addActivityFilter(filter2)
        testUtils.addSuggestion(name1, listOf(name2, name3))
        testUtils.addShortcut(name1)
        testUtils.addShortcut(name2)
        testUtils.addRecord(name1) // for suggests
        Thread.sleep(1000)

        fun checkType(name: String, isVisible: Boolean) {
            val matcher = allOf(
                withId(R.id.viewRecordTypeItem),
                withTag(null),
                hasDescendant(withText(name)),
            )
            if (isVisible) checkViewIsDisplayed(matcher) else checkViewDoesNotExist(matcher)
        }

        fun checkFilter(name: String, isVisible: Boolean) {
            val matcher = allOf(
                withId(R.id.viewActivityFilterItem),
                hasDescendant(withText(name)),
            )
            if (isVisible) checkViewIsDisplayed(matcher) else checkViewDoesNotExist(matcher)
        }

        fun checkSuggestion(name: String, isVisible: Boolean) {
            val matcher = allOf(
                withId(R.id.viewRecordTypeItem),
                withTag(RecordTypeSuggestionViewData.TEST_TAG),
                hasDescendant(withText(name)),
            )
            if (isVisible) checkViewIsDisplayed(matcher) else checkViewDoesNotExist(matcher)
        }

        // Setting disabled
        checkType(name1, true)
        checkType(name2, true)
        checkType(name3, true)
        checkFilter(filter1, true)
        checkFilter(filter2, true)
        checkSuggestion(name2, true)
        checkSuggestion(name3, true)
        checkViewDoesNotExist(withId(R.id.etCommentItemField))

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_enable_search_on_main)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_enable_search_on_main))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_enable_search_on_main)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_enable_search_on_main))

        NavUtils.openRunningRecordsScreen()
        checkType(name1, true)
        checkType(name2, true)
        checkType(name3, true)
        checkFilter(filter1, true)
        checkFilter(filter2, true)
        checkSuggestion(name2, true)
        checkSuggestion(name3, true)
        checkViewIsDisplayed(withId(R.id.etCommentItemField))

        // Check search
        typeTextIntoView(R.id.etCommentItemField, "2")
        Thread.sleep(1000)
        checkType(name1, false)
        checkType(name2, true)
        checkType(name3, false)
        checkFilter(filter1, false)
        checkFilter(filter2, true)
        checkSuggestion(name2, true)
        checkSuggestion(name3, false)

        // Check when search entered but setting disabled
        NavUtils.openSettingsScreen()
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_enable_search_on_main))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_enable_search_on_main)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_enable_search_on_main))

        NavUtils.openRunningRecordsScreen()
        checkType(name1, true)
        checkType(name2, true)
        checkType(name3, true)
        checkFilter(filter1, true)
        checkFilter(filter2, true)
        checkSuggestion(name2, true)
        checkSuggestion(name3, true)
        checkViewDoesNotExist(withId(R.id.etCommentItemField))

        // Check lowercase
        NavUtils.openSettingsScreen()
        clickOnSettingsCheckboxBesideText(coreR.string.settings_enable_search_on_main)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_enable_search_on_main))

        NavUtils.openRunningRecordsScreen()
        typeTextIntoView(R.id.etCommentItemField, "lowercase")
        Thread.sleep(1000)
        checkType(name1, false)
        checkType(name2, false)
        checkType(name3, false)
        checkType(name4, true)
    }

    @Test
    fun retroactiveMode() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_retroactive_tracking_mode)

        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_retroactive_tracking_mode))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_retroactive_tracking_mode)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_retroactive_tracking_mode))

        clickOnSettingsCheckboxBesideText(coreR.string.settings_retroactive_tracking_mode)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_retroactive_tracking_mode))
    }

    private fun clearDuration() {
        repeat(6) { clickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete) }
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
                isCompletelyDisplayed(),
            ),
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
                isCompletelyDisplayed(),
            ),
        )
    }

    private fun checkStatisticsDetailRecords(count: Int) {
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, count),
                hasSibling(withText(count.toString())),
                isCompletelyDisplayed(),
            ),
        )
    }

    private fun Long.toTimePreview(): String {
        return timeMapper.formatTime(time = this, useMilitaryTime = true, showSeconds = false)
    }

    fun checkWeekTitle(title: Pair<String, String>) {
        checkViewIsDisplayed(
            allOf(
                dateSelectorMatcher(-1),
                withId(R.id.containerDateSelectorRange),
                hasDescendant(withText(title.first)),
                hasDescendant(withText(title.second)),
            ),
        )
    }

    private fun Long.toDurationPreview(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this
        calendar.setToStartOfDay()
        val interval = this - calendar.timeInMillis
        return timeMapper.formatDuration(interval / 1000)
    }
}
