package com.example.util.simpletimetracker

import android.widget.DatePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import dagger.hilt.android.testing.HiltAndroidTest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_records.R as recordsR

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

        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                isCompletelyDisplayed(),
            ),
        )
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                isCompletelyDisplayed(),
            ),
        )

        longClickOnViewWithId(recordsR.id.btnRecordsContainerToday)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
        checkViewIsDisplayed(allOf(withText(coreR.string.no_data), isCompletelyDisplayed()))
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
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
        clickOnViewWithId(recordsR.id.btnRecordsContainerToday)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                setDate(
                    calendarPrev.get(Calendar.YEAR),
                    calendarPrev.get(Calendar.MONTH) + 1,
                    calendarPrev.get(Calendar.DAY_OF_MONTH),
                ),
            )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(coreR.string.title_yesterday), isCompletelyDisplayed()))

        // Check tomorrow
        clickOnViewWithId(recordsR.id.btnRecordsContainerToday)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                setDate(
                    calendarNext.get(Calendar.YEAR),
                    calendarNext.get(Calendar.MONTH) + 1,
                    calendarNext.get(Calendar.DAY_OF_MONTH),
                ),
            )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(coreR.string.title_tomorrow), isCompletelyDisplayed()))
    }

    @Test
    fun selectFarDate() {
        NavUtils.openRecordsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1950)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val titlePrev = dayTitleFormat.format(calendarPrev.timeInMillis)
        val calendarNext = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2050)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val titleNext = dayTitleFormat.format(calendarNext.timeInMillis)

        // Check prev date
        clickOnViewWithId(recordsR.id.btnRecordsContainerToday)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                setDate(
                    calendarPrev.get(Calendar.YEAR),
                    calendarPrev.get(Calendar.MONTH) + 1,
                    calendarPrev.get(Calendar.DAY_OF_MONTH),
                ),
            )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))

        // Check next date
        clickOnViewWithId(recordsR.id.btnRecordsContainerToday)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                setDate(
                    calendarNext.get(Calendar.YEAR),
                    calendarNext.get(Calendar.MONTH) + 1,
                    calendarNext.get(Calendar.DAY_OF_MONTH),
                ),
            )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titleNext), isCompletelyDisplayed()))
    }

    companion object {
        private val dayTitleFormat = SimpleDateFormat("E, MMM d", Locale.getDefault())
    }
}
