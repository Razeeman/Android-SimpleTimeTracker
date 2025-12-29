package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.domain.extension.padDuration
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomDatePicker
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnCurrentDate
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.dateSelectorMatcher
import com.example.util.simpletimetracker.utils.longClickOnCurrentDate
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordsRangesTest : BaseUiTest() {

    @Test
    fun recordsRanges() {
        val name = "Test"

        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }

        // Add activity
        NavUtils.addActivity(name)
        val before = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        testUtils.addRecord(name, timeStarted = before, timeEnded = before)

        // Start timer
        clickOnViewWithText(name)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name)))

        // Records
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        clickOnCurrentDate(-1)
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                isCompletelyDisplayed(),
            ),
        )
        clickOnCurrentDate(-2)
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                isCompletelyDisplayed(),
            ),
        )

        longClickOnCurrentDate(-2)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        clickOnCurrentDate(1)
        checkViewIsDisplayed(allOf(withText(coreR.string.no_data), isCompletelyDisplayed()))
        clickOnCurrentDate(2)
        checkViewIsDisplayed(allOf(withText(coreR.string.no_data), isCompletelyDisplayed()))
    }

    @Test
    fun selectNearDate() {
        NavUtils.openRecordsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }
        val calendarNext = Calendar.getInstance().apply {
            add(Calendar.DATE, 1)
        }

        // Check yesterday
        clickOnCurrentDate()
        onView(withClassName(equalTo(CustomDatePicker::class.java.name)))
            .perform(
                setDate(
                    calendarPrev.get(Calendar.YEAR),
                    calendarPrev.get(Calendar.MONTH) + 1,
                    calendarPrev.get(Calendar.DAY_OF_MONTH),
                ),
            )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                dateSelectorMatcher(-1),
                hasDescendant(withText(calendarPrev.get(Calendar.DAY_OF_MONTH).toString().padDuration())),
            ),
        )

        // Check tomorrow
        clickOnCurrentDate(-1)
        onView(withClassName(equalTo(CustomDatePicker::class.java.name)))
            .perform(
                setDate(
                    calendarNext.get(Calendar.YEAR),
                    calendarNext.get(Calendar.MONTH) + 1,
                    calendarNext.get(Calendar.DAY_OF_MONTH),
                ),
            )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                dateSelectorMatcher(1),
                hasDescendant(withText(calendarNext.get(Calendar.DAY_OF_MONTH).toString().padDuration())),
            ),
        )
    }

    @Test
    fun selectFarDate() {
        NavUtils.openRecordsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1950)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val calendarNext = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2050)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        // Check prev date
        clickOnCurrentDate()
        onView(withClassName(equalTo(CustomDatePicker::class.java.name)))
            .perform(
                setDate(
                    calendarPrev.get(Calendar.YEAR),
                    calendarPrev.get(Calendar.MONTH) + 1,
                    calendarPrev.get(Calendar.DAY_OF_MONTH),
                ),
            )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withId(R.id.containerDateSelectorDay),
                hasDescendant(withText(calendarPrev.get(Calendar.DAY_OF_MONTH).toString())),
            ),
        )

        // Check next date
        NavUtils.openOptionsList()
        clickOnViewWithText(R.string.range_back_to_today)
        clickOnCurrentDate()
        onView(withClassName(equalTo(CustomDatePicker::class.java.name)))
            .perform(
                setDate(
                    calendarNext.get(Calendar.YEAR),
                    calendarNext.get(Calendar.MONTH) + 1,
                    calendarNext.get(Calendar.DAY_OF_MONTH),
                ),
            )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withId(R.id.containerDateSelectorDay),
                hasDescendant(withText(calendarNext.get(Calendar.DAY_OF_MONTH).toString())),
            ),
        )
    }
}
