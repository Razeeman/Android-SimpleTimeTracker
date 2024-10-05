package com.example.util.simpletimetracker

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
import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.extension.setWeekToFirstDay
import com.example.util.simpletimetracker.core.interactor.LanguageInteractor
import com.example.util.simpletimetracker.domain.interactor.AppLanguage
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomDatePicker
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomTimePicker
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkCheckboxIsChecked
import com.example.util.simpletimetracker.utils.checkCheckboxIsNotChecked
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.getMillis
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.recyclerItemCount
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withPluralText
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_records.R as recordsR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR
import com.example.util.simpletimetracker.feature_statistics_detail.R as statisticsDetailR

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
                useProportionalMinutes = false,
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
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
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
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
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
            checkViewIsDisplayed(
                allOf(
                    withId(statisticsDetailR.id.containerStatisticsDetailCard),
                    hasDescendant(withText(R.string.statistics_detail_total_duration)),
                    hasDescendant(withText(timeString)),
                ),
            )
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
        scrollSettingsRecyclerToText(coreR.string.settings_use_proportional_minutes)

        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_use_proportional_minutes))
        checkViewIsDisplayed(
            settingsSubtitleBesideText(coreR.string.settings_use_proportional_minutes, withText(timeFormat1)),
        )

        // Change settings
        clickOnSettingsCheckboxBesideText(coreR.string.settings_use_proportional_minutes)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_use_proportional_minutes))
        checkViewIsDisplayed(
            settingsSubtitleBesideText(coreR.string.settings_use_proportional_minutes, withText(timeFormat2)),
        )

        // Check format after setting change
        checkFormat(timeFormat2)

        // Change settings back
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_use_proportional_minutes)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_use_proportional_minutes)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_use_proportional_minutes))
        checkViewIsDisplayed(
            settingsSubtitleBesideText(coreR.string.settings_use_proportional_minutes, withText(timeFormat1)),
        )

        // Check format again
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
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_week)
        clickOnView(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed(),
            ),
        )

        // Check detailed statistics
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_week)
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, 1),
                hasSibling(withText("1")),
                isCompletelyDisplayed(),
            ),
        )

        // Check range titles
        var titlePrev = timeMapper.toWeekTitle(
            weeksFromToday = -1,
            startOfDayShift = 0,
            firstDayOfWeek = if (isTodaySunday) DayOfWeek.MONDAY else DayOfWeek.SUNDAY,
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
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        clickOnView(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed(),
            ),
        )

        // Check detailed statistics
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_week)
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, 0),
                hasSibling(withText("0")),
                isCompletelyDisplayed(),
            ),
        )
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, 1),
                hasSibling(withText("1")),
                isCompletelyDisplayed(),
            ),
        )

        // Check range titles
        titlePrev = timeMapper.toWeekTitle(
            weeksFromToday = -1,
            startOfDayShift = 0,
            firstDayOfWeek = if (isTodaySunday) DayOfWeek.SUNDAY else DayOfWeek.MONDAY,
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
            timeEnded = timeEndedTimeStamp,
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
        scrollSettingsRecyclerToText(coreR.string.settings_start_of_day)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(coreR.string.settings_start_of_day, withText(startOfDayPreview)),
        )
        checkViewIsNotDisplayed(settingsButtonBesideText(coreR.string.settings_start_of_day))

        // Change setting to +1
        clickOnSettingsSelectorBesideText(coreR.string.settings_start_of_day)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(1, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        startOfDayTimeStamp = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        // Check new setting
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(coreR.string.settings_start_of_day, withText(startOfDayPreview)),
        )
        checkViewIsDisplayed(
            settingsButtonBesideText(
                coreR.string.settings_start_of_day, hasDescendant(withText(coreR.string.plus_sign)),
            ),
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
        scrollSettingsRecyclerToText(coreR.string.settings_start_of_day)
        clickOnSettingsButtonBesideText(coreR.string.settings_start_of_day)

        // Check new setting
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(coreR.string.settings_start_of_day, withText(startOfDayPreview)),
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
        scrollSettingsRecyclerToText(coreR.string.settings_start_of_day)
        clickOnSettingsSelectorBesideText(coreR.string.settings_start_of_day)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(2, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(coreR.string.settings_start_of_day, withText(startOfDayPreview)),
        )
        checkViewIsDisplayed(
            settingsButtonBesideText(
                coreR.string.settings_start_of_day, hasDescendant(withText(coreR.string.minus_sign)),
            ),
        )
        clickOnSettingsButtonBesideText(coreR.string.settings_start_of_day)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(coreR.string.settings_start_of_day, withText(startOfDayPreview)),
        )

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
        scrollSettingsRecyclerToText(coreR.string.settings_start_of_day)
        clickOnSettingsSelectorBesideText(coreR.string.settings_start_of_day)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(0, 0))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(
            settingsSelectorValueBesideText(coreR.string.settings_start_of_day, withText(startOfDayPreview)),
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
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)))) }

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(coreR.string.settings_show_record_tag_selection)
        checkViewIsNotDisplayed(settingsButtonBesideText(coreR.string.settings_show_record_tag_selection))

        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_record_tag_selection)
        checkViewIsDisplayed(settingsButtonBesideText(coreR.string.settings_show_record_tag_selection))

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
        scrollSettingsRecyclerToText(coreR.string.settings_show_records_calendar)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_records_calendar))
        checkViewDoesNotExist(withText(coreR.string.settings_reverse_order_in_calendar))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_records_calendar)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_records_calendar))

        // Record is not shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withId(recordsR.id.viewRecordsCalendar), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(recordsR.id.tvRecordsCalendarHint), isCompletelyDisplayed()))
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
        scrollSettingsRecyclerToText(coreR.string.settings_show_records_calendar)
        checkViewDoesNotExist(withText(coreR.string.settings_days_in_calendar))

        // Change setting
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_records_calendar)
        scrollSettingsRecyclerToText(coreR.string.settings_days_in_calendar)
        checkViewIsDisplayed(withText(coreR.string.settings_days_in_calendar))

        // One day
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(coreR.string.title_today), isCompletelyDisplayed()))

        // Three days
        NavUtils.openSettingsScreen()
        clickOnSettingsSpinnerBesideText(coreR.string.settings_days_in_calendar)
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
        clickOnSettingsSpinnerBesideText(coreR.string.settings_days_in_calendar)
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
        clickOnSettingsSpinnerBesideText(coreR.string.settings_days_in_calendar)
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
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_records_calendar)
        checkViewDoesNotExist(withText(coreR.string.settings_days_in_calendar))
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(coreR.string.title_today), isCompletelyDisplayed()))
    }

    @Test
    fun showCalendarSwitchOnTheSameTab() {
        // Check setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_show_calendar_button_on_records_tab)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_calendar_button_on_records_tab))

        // Check not shown
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordsContainerOptions)
        checkViewIsNotDisplayed(withId(recordsR.id.btnRecordsContainerCalendarSwitch))

        // Change settings
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_calendar_button_on_records_tab)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_calendar_button_on_records_tab)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_calendar_button_on_records_tab))

        // Check shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(withId(recordsR.id.btnRecordsContainerCalendarSwitch))

        // Change back
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_show_calendar_button_on_records_tab)
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_calendar_button_on_records_tab)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_calendar_button_on_records_tab))

        // Check not shown
        NavUtils.openRecordsScreen()
        checkViewIsNotDisplayed(withId(recordsR.id.btnRecordsContainerCalendarSwitch))
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
        scrollSettingsRecyclerToText(coreR.string.settings_keep_statistics_range)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_keep_statistics_range))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_keep_statistics_range)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_keep_statistics_range))

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
            checkViewIsDisplayed(withText(coreR.string.running_records_add_type))
            checkViewDoesNotExist(withText(coreR.string.running_records_add_filter))
            checkViewDoesNotExist(withText(name))
        }

        // Check settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_show_activity_filters)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_show_activity_filters))
        checkViewDoesNotExist(withText(coreR.string.settings_allow_multiple_activity_filters))

        // Change setting
        clickOnSettingsCheckboxBesideText(coreR.string.settings_show_activity_filters)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_show_activity_filters))
        scrollSettingsRecyclerToText(coreR.string.settings_allow_multiple_activity_filters)
        checkViewIsDisplayed(withText(coreR.string.settings_allow_multiple_activity_filters))
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_allow_multiple_activity_filters))

        // Check allow multiple
        clickOnSettingsCheckboxBesideText(coreR.string.settings_allow_multiple_activity_filters)
        checkCheckboxIsNotChecked(settingsCheckboxBesideText(coreR.string.settings_allow_multiple_activity_filters))
        clickOnSettingsCheckboxBesideText(coreR.string.settings_allow_multiple_activity_filters)
        checkCheckboxIsChecked(settingsCheckboxBesideText(coreR.string.settings_allow_multiple_activity_filters))

        // Filters shown
        NavUtils.openRunningRecordsScreen()
        checkViewIsDisplayed(withText(coreR.string.running_records_add_filter))
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
}
